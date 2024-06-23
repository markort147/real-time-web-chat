package chat.controllers.websocket;

import chat.models.ChatMessage;
import chat.services.ChatMessagesService;
import chat.services.ChatUsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
public class ChatWSController {

    Logger logger = LoggerFactory.getLogger(ChatWSController.class);

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
        logger.info("sendMessage() message: {}", message.getContent());
        chatMessagesService.addMessage(message);
        return message;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String username = message.getSender();
        logger.info("addUser() user: {}", username);
            chatUsersService.getByUsername(username)
                            .ifPresentOrElse(
                                    user -> chatUsersService.setOnline(username),
                                    () -> chatUsersService.registerByUsername(username));
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", username);
        return message;
    }

}
