package com.meetswps.safety;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Once either student blocks the other, neither sees the other anywhere in the app. */
@Entity
@Table(name = "blocks")
class Block {

    @Id
    private UUID id;

    @Column(name = "blocker_id", nullable = false)
    private UUID blockerId;

    @Column(name = "blocked_id", nullable = false)
    private UUID blockedId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Block() {
    }

    Block(UUID blockerId, UUID blockedId) {
        this.id = UUID.randomUUID();
        this.blockerId = blockerId;
        this.blockedId = blockedId;
        this.createdAt = Instant.now();
    }

    UUID getBlockedId() {
        return blockedId;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
