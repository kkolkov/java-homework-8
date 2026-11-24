package com.example.hellospring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
public class MessageController {
    public record CreateMessageRequest(
            @NotBlank @Size(max = 255) String content,
            @Size(max = 100) String username) {}

    private final MessageService service;

    private final Counter messagesCreated;

    public MessageController(MessageService service, MeterRegistry registry) {
        this.service = service;
        this.messagesCreated = registry.counter("messages_created_total");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody CreateMessageRequest request) {
        MessageResponse message = service.saveMessage(request.content(), request.username());
        messagesCreated.increment();
        return message;
    }

    @GetMapping({"", "/optimized"})
    public List<MessageResponse> optimized() {
        return service.getMessagesOptimized();
    }

    @GetMapping("/nplus1")
    public List<MessageResponse> nplus1() {
        return service.getMessagesNPlus1();
    }

    @GetMapping("/fail/{id}")
    public MessageResponse fail(@PathVariable Long id) {
        Message message = service.getMessageToFail(id);
        return MessageResponse.from(message);
    }

    @GetMapping("/fixed/transaction/{id}")
    public MessageResponse transaction(@PathVariable Long id) {
        return service.getMessageTransactional(id);
    }

    @GetMapping("/fixed/dto/{id}")
    public MessageResponse dto(@PathVariable Long id) {
        return service.getMessageDto(id);
    }
}
