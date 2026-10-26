package com.meetswps.safety;

import com.meetswps.auth.CurrentUser;
import com.meetswps.safety.SafetyDtos.BlockedUser;
import com.meetswps.safety.SafetyDtos.QueueItem;
import com.meetswps.safety.SafetyDtos.ReportRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Blocking and reporting for every student, and the review queue for moderators (secured by role). */
@RestController
class SafetyController {

    private final SafetyService safety;

    SafetyController(SafetyService safety) {
        this.safety = safety;
    }

    @PutMapping("/api/blocks/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void block(CurrentUser user, @PathVariable UUID userId) {
        safety.block(user.id(), userId);
    }

    @DeleteMapping("/api/blocks/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unblock(CurrentUser user, @PathVariable UUID userId) {
        safety.unblock(user.id(), userId);
    }

    @GetMapping("/api/blocks")
    List<BlockedUser> blocked(CurrentUser user) {
        return safety.blocked(user.id());
    }

    @PostMapping("/api/reports")
    @ResponseStatus(HttpStatus.CREATED)
    void report(CurrentUser user, @Valid @RequestBody ReportRequest request) {
        safety.report(user.id(), request.userId(), request.reason(), request.details());
    }

    @GetMapping("/api/moderation/reports")
    List<QueueItem> queue() {
        return safety.queue();
    }

    @PostMapping("/api/moderation/users/{userId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void restore(@PathVariable UUID userId) {
        safety.restore(userId);
    }

    @PostMapping("/api/moderation/users/{userId}/ban")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void ban(@PathVariable UUID userId) {
        safety.ban(userId);
    }
}
