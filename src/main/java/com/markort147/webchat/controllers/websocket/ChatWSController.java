package com.markort147.webchat.controllers.websocket;

import com.markort147.webchat.models.ChatMessage;
import com.markort147.webchat.services.ChatMessagesService;
import com.markort147.webchat.services.ChatUsersService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@Log
public class ChatWSController {

    private final ChatUsersService chatUsersService;
    private final ChatMessagesService chatMessagesService;

    @Autowired
    public ChatWSController(ChatUsersService chatUsersService, ChatMessagesService chatMessagesService) {
        this.chatUsersService = chatUsersService;
        this.chatMessagesService = chatMessagesService;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage message) {
        log.info("sendMessage() message: %s".formatted(message.getContent()));
        chatMessagesService.addMessage(message);
        return message;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String username = message.getSender();
        log.info("addUser() user: %s".formatted(username));
            chatUsersService.getByUsername(username)
                            .ifPresentOrElse(
                                    user -> chatUsersService.setOnline(username),
                                    () -> chatUsersService.registerByUsername(username));
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", username);
        return message;
    }

}
