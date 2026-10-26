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

/** Two students who liked each other. Only matches can chat and see each other's Instagram. */
@Entity
@Table(name = "matches")
public class Match {

    @Id
    private UUID id;

    @Column(name = "user_low", nullable = false)
    private UUID userLow;

    @Column(name = "user_high", nullable = false)
    private UUID userHigh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Intent intent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Match() {
    }

    Match(UUID a, UUID b, Intent intent) {
        this.id = UUID.randomUUID();
        this.userLow = a.compareTo(b) < 0 ? a : b;
        this.userHigh = a.compareTo(b) < 0 ? b : a;
        this.intent = intent;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Intent getIntent() {
        return intent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean includes(UUID userId) {
        return userLow.equals(userId) || userHigh.equals(userId);
    }

    public UUID otherThan(UUID userId) {
        return userLow.equals(userId) ? userHigh : userLow;
    }
}
