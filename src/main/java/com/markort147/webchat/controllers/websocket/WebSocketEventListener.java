package com.markort147.webchat.controllers.websocket;

import com.markort147.webchat.models.ChatMessage;
import com.markort147.webchat.services.ChatUsersService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Component
@Log
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatUsersService chatUsersService;

    @Autowired
    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate, ChatUsersService chatUsersService) {
        this.messagingTemplate = messagingTemplate;
        this.chatUsersService = chatUsersService;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("WebSocket connected");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");
        if (username != null) {
            log.info("User disconnected: %s".formatted(username));

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);

            messagingTemplate.convertAndSend("/topic/public", chatMessage);

            chatUsersService.setOffline(username);
        }

    }

}
