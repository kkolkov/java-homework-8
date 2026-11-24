package com.example.hellospring;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.LazyInitializationException;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc
@org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
class MessageApplicationTests {
    @Autowired io.micrometer.core.instrument.MeterRegistry registry;
    @Autowired MessageService service;
    @Autowired MessageRepository messages;
    @Autowired UserRepository users;
    @Autowired EntityManagerFactory factory;
    @Autowired MockMvc mvc;
    @BeforeEach void seed() {
        messages.deleteAll(); users.deleteAll();
        for (int i=0;i<35;i++) service.saveMessage("Message " + i,"user-" + i);
    }
    @Test void queryCountsAndResponses() {
        Statistics stats=factory.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        var slow=service.getMessagesNPlus1();
        assertEquals(31,stats.getPrepareStatementCount());
        stats.clear();
        var fast=service.getMessagesOptimized();
        assertEquals(1,stats.getPrepareStatementCount());
        assertEquals(30,fast.size()); assertEquals(slow,fast);
        assertTrue(fast.get(0).id()>fast.get(29).id());
    }
    @Test void detachedProxyFailsAndBothFixesWork() {
        long id=service.getMessagesOptimized().get(0).id();
        Message detached=service.getMessageToFail(id);
        assertThrows(LazyInitializationException.class,() -> detached.getAuthor().getUsername());
        assertEquals(service.getMessageTransactional(id),service.getMessageDto(id));
    }
    @Test void httpEndpointsAndErrors() throws Exception {
        long id=service.getMessagesOptimized().get(0).id();
        mvc.perform(get("/messages/fail/"+id)).andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("LazyInitializationException"));
        mvc.perform(get("/messages/fixed/transaction/"+id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.authorUsername").isString());
        mvc.perform(get("/messages/fixed/dto/"+id)).andExpect(status().isOk());
        mvc.perform(get("/messages/fail/0")).andExpect(status().isNotFound());
        mvc.perform(get("/messages/optimized")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(30));
        mvc.perform(get("/messages/nplus1")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(30));
    }
    @Test void validatesInputAndDefaultsAuthor() throws Exception {
        mvc.perform(post("/messages").contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"hello\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.authorUsername").value("Anonymous"));
        mvc.perform(post("/messages").contentType(MediaType.APPLICATION_JSON).content("{\"content\":\" \"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/messages").contentType(MediaType.APPLICATION_JSON).content("{\"content\":\""+"a".repeat(256)+"\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test void countsOnlyCreatedMessages() throws Exception {
        double before = registry.get("messages_created_total").counter().count();
        long count = messages.count();
        assertTrue(((io.micrometer.prometheus.PrometheusMeterRegistry) registry).scrape()
            .contains("messages_created_total " + before));
        mvc.perform(post("/messages").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"new message\"}"))
            .andExpect(status().isCreated());
        assertEquals(count + 1, messages.count());
        assertEquals(before + 1, registry.get("messages_created_total").counter().count());
        mvc.perform(post("/messages").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\" \"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/messages")).andExpect(status().isOk());
        assertEquals(before + 1, registry.get("messages_created_total").counter().count());
    }
}
