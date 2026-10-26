package com.meetswps.safety;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reports")
class Report {

    @Id
    private UUID id;

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportReason reason;

    @Column(length = 1000)
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected Report() {
    }

    Report(UUID reporterId, UUID targetId, ReportReason reason, String details) {
        this.id = UUID.randomUUID();
        this.reporterId = reporterId;
        this.targetId = targetId;
        this.reason = reason;
        this.details = details;
        this.createdAt = Instant.now();
    }

    UUID getTargetId() {
        return targetId;
    }

    ReportReason getReason() {
        return reason;
    }

    String getDetails() {
        return details;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    void resolve() {
        this.resolvedAt = Instant.now();
    }
}
