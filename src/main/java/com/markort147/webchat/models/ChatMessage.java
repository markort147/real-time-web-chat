package chat.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage implements Comparable<ChatMessage> {

    private MessageType type;
    private String content;
    private String sender;
    private String receiver;
    private LocalDateTime timestamp;
    private boolean isPrivate;

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }

    @Override
    public int compareTo(ChatMessage message) {
        if (timestamp.equals(message.timestamp)) {
            return sender.compareTo(message.sender);
        }
        return timestamp.compareTo(message.timestamp);
    }
}
