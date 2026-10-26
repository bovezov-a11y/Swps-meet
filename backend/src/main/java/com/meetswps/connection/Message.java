package com.meetswps.connection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
class Message {

    @Id
    private UUID id;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(nullable = false, length = 2000)
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Message() {
    }

    Message(UUID matchId, UUID senderId, String body) {
        this.id = UUID.randomUUID();
        this.matchId = matchId;
        this.senderId = senderId;
        this.body = body;
        this.createdAt = Instant.now();
    }

    UUID getId() {
        return id;
    }

    UUID getSenderId() {
        return senderId;
    }

    String getBody() {
        return body;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
