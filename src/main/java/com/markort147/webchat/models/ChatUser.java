package com.markort147.webchat.models;

import lombok.Data;

import java.time.LocalDate;
import java.util.Objects;

@Data
public class ChatUser implements Comparable<ChatUser> {
    private String username;
    private boolean isOnline;
    private LocalDate onlineSince;

    @Override
    public int compareTo(ChatUser chatUser) {
        return this.onlineSince.compareTo(chatUser.onlineSince);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatUser chatUser)) return false;
        return Objects.equals(username, chatUser.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }
}
