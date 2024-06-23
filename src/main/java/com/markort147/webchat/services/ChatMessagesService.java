package chat.services;

import chat.models.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ChatMessagesService {

    private final ChatUsersService chatUsersService;

    private static final List<ChatMessage> messages = new CopyOnWriteArrayList<>();

    @Autowired
    public ChatMessagesService(ChatUsersService chatUsersService) {
        this.chatUsersService = chatUsersService;
    }

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
