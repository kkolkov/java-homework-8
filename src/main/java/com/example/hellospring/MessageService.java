package com.example.hellospring;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MessageService {
    private final MessageRepository messages;
    private final UserRepository users;

    public MessageService(MessageRepository messages, UserRepository users) {
        this.messages = messages;
        this.users = users;
    }

    @Transactional
    public MessageResponse saveMessage(String content, String username) {
        if (username == null || username.isBlank()) {
            username = "Anonymous";
        } else {
            username = username.trim();
        }

        User author = users.findByUsername(username).orElse(null);
        if (author == null) {
            author = users.save(new User(username));
        }

        Message message = messages.save(new Message(content, author));
        return MessageResponse.from(message);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesNPlus1() {
        List<Message> loaded = messages.findTop30ByOrderByCreatedAtDescIdDesc();
        List<MessageResponse> result = new ArrayList<>();
        for (Message message : loaded) {
            result.add(MessageResponse.from(message));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesOptimized() {
        List<Message> loaded = messages.findFirst30ByOrderByCreatedAtDescIdDesc();
        List<MessageResponse> result = new ArrayList<>();
        for (Message message : loaded) {
            result.add(MessageResponse.from(message));
        }
        return result;
    }

    public Message getMessageToFail(Long id) {
        Message message = messages.findById(id).orElse(null);
        if (message == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found");
        }
        return message;
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessageTransactional(Long id) {
        Message message = messages.findById(id).orElse(null);
        if (message == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found");
        }
        return MessageResponse.from(message);
    }

    public MessageResponse getMessageDto(Long id) {
        Message message = messages.findWithAuthorById(id).orElse(null);
        if (message == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found");
        }
        return MessageResponse.from(message);
    }
}
