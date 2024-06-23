package com.markort147.webchat.services;

import com.markort147.webchat.models.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ChatMessagesService {

    private static final List<ChatMessage> messages = new CopyOnWriteArrayList<>();

    public void addMessage(ChatMessage message) {
        messages.add(message);
    }

    public List<ChatMessage> getPublicMessage() {
        return messages.stream()
                .filter(message -> !message.isPrivate())
                .toList();
    }

    public List<ChatMessage> getPrivateChat(String sender, String receiver) {
        return messages.stream()
                .filter(ChatMessage::isPrivate)
                .filter(m -> (m.getSender().equals(sender) && m.getReceiver().equals(receiver)) ||
                        (m.getSender().equals(receiver) && m.getReceiver().equals(sender)))
                .toList();
    }
}
