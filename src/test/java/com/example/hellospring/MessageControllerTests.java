package com.example.hellospring;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageControllerTests {
    @Test
    void doesNotCountFailedSave() {
        MessageService service = mock(MessageService.class);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        try {
            MessageController controller = new MessageController(service, registry);
            when(service.saveMessage("hello", "user"))
                .thenThrow(new DataIntegrityViolationException("Save failed"));

            assertThrows(DataIntegrityViolationException.class, () -> controller.create(
                new MessageController.CreateMessageRequest("hello", "user")));
            assertEquals(0, registry.get("messages_created_total").counter().count());
        } finally {
            registry.close();
        }
    }
}
