package com.meetswps.connection;

import com.meetswps.profile.Intent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** "I would like to meet this person" in one section. The other student never sees it unless they like back. */
@Entity
@Table(name = "likes")
class Like {

    @Id
    private UUID id;

    @Column(name = "from_id", nullable = false)
    private UUID fromId;

    @Column(name = "to_id", nullable = false)
    private UUID toId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Intent intent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Like() {
    }

    Like(UUID fromId, UUID toId, Intent intent) {
        this.id = UUID.randomUUID();
        this.fromId = fromId;
        this.toId = toId;
        this.intent = intent;
        this.createdAt = Instant.now();
    }

    UUID getToId() {
        return toId;
    }

    Intent getIntent() {
        return intent;
    }
}
