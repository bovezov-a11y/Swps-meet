package com.meetswps.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "push_tokens")
class PushToken {

    @Id
    @Column(length = 255)
    private String token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PushToken() {
    }

    PushToken(String token, UUID userId) {
        this.token = token;
        assignTo(userId);
    }

    /** The phone's app is now logged in as {@code userId}. */
    void assignTo(UUID userId) {
        this.userId = userId;
        this.updatedAt = Instant.now();
    }

    String getToken() {
        return token;
    }

    UUID getUserId() {
        return userId;
    }
}
