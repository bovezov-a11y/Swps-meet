package com.meetswps.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** The current emailed code of a user who has not confirmed their address yet. One per user. */
@Entity
@Table(name = "verification_codes")
class VerificationCode {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected VerificationCode() {
    }

    VerificationCode(UUID userId, String codeHash, Instant createdAt, Instant expiresAt) {
        this.userId = userId;
        this.codeHash = codeHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    String getCodeHash() {
        return codeHash;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    int getAttempts() {
        return attempts;
    }

    void recordFailedAttempt() {
        attempts++;
    }

    boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }
}
