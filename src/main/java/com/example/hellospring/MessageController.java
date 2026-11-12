package com.example.hellospring;

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

    public MessageController(MessageService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse create(@Valid @RequestBody CreateMessageRequest request) {
        return service.saveMessage(request.content(), request.username());
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
