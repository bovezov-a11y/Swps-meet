package com.meetswps.safety;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

final class SafetyDtos {

    private SafetyDtos() {
    }

    record ReportRequest(@NotNull UUID userId, @NotNull ReportReason reason, @Size(max = 1000) String details) {
    }

    record BlockedUser(UUID userId, String firstName, Instant blockedAt) {
    }

    /** Open reports about one student, for moderators. Reporters are never included. */
    record QueueItem(UUID userId, String firstName, String email, boolean hidden, int reportCount,
                     List<ReportEntry> reports) {
    }

    record ReportEntry(ReportReason reason, String details, Instant createdAt) {
    }
}
