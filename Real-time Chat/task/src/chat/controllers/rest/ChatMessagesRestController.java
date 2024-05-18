package chat.controllers.rest;

import chat.models.ChatMessage;
import chat.services.ChatMessagesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class ChatMessagesRestController {

    Logger logger = LoggerFactory.getLogger(ChatMessagesRestController.class);

    private final ChatMessagesService chatMessagesService;

    @Autowired
    public ChatMessagesRestController(ChatMessagesService chatMessagesService) {
        this.chatMessagesService = chatMessagesService;
    }

    @GetMapping("/get-public-chat")
    public ResponseEntity<List<ChatMessage>> getAllMessages() {
        List<ChatMessage> messages = chatMessagesService.getPublicMessage();
        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(messages);
        }
    }

    @GetMapping("/get-private-chat")
    public ResponseEntity<List<ChatMessage>> getPrivateChat(
            @RequestParam String sender,
            @RequestParam String receiver) {
        try {
            return ResponseEntity.ok(chatMessagesService.getPrivateChat(sender, receiver));
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().build();
        }
    }
}
