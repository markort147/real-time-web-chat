package chat.services;

import chat.models.ChatUser;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ChatUsersService {
    private static final List<ChatUser> users = new CopyOnWriteArrayList<>();

    public void registerByUsername(String username) {
        ChatUser chatUser = new ChatUser();
        chatUser.setUsername(username);
        chatUser.setOnlineSince(LocalDate.now());
        chatUser.setOnline(true);
        users.add(chatUser);
    }

    public List<ChatUser> getOnlineUsers() {
        return users.stream()
                .filter(ChatUser::isOnline)
                .toList();
    }

    public Optional<ChatUser> getByUsername(String username) {
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    public void setOffline(String username) {
        users.stream()
                .filter(user -> user.getUsername().equals(username))
                .forEach(user -> user.setOnline(false));
    }

    public void setOnline(String username) {
        users.stream()
                .filter(user -> user.getUsername().equals(username))
                .forEach(user -> user.setOnline(true));
    }
}
