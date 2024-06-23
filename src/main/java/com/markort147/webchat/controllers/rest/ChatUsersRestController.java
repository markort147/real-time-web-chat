package com.markort147.webchat.controllers.rest;

import com.markort147.webchat.models.ChatUser;
import com.markort147.webchat.services.ChatUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class ChatUsersRestController {

    private final ChatUsersService chatUsersService;

    @Autowired
    public ChatUsersRestController(ChatUsersService chatUsersService) {
        this.chatUsersService = chatUsersService;
    }

    @GetMapping("/validate-username")
    public ResponseEntity<?> validateUsername(@RequestParam String username) {
        boolean isValid = chatUsersService.getByUsername(username)
                .map(user -> !user.isOnline())
                .orElse(true);
        if (isValid) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("Username is already in use");
        }
    }

    @GetMapping("/online-users")
    public ResponseEntity<List<ChatUser>> getOnlineUsers() {
        return ResponseEntity.ok(chatUsersService.getOnlineUsers());
    }
}
