package com.meetswps.connection;

import com.meetswps.auth.CurrentUser;
import com.meetswps.connection.ConnectionDtos.LikeResult;
import com.meetswps.connection.ConnectionDtos.MatchSummary;
import com.meetswps.connection.ConnectionDtos.MessageResponse;
import com.meetswps.connection.ConnectionDtos.SendMessageRequest;
import com.meetswps.profile.Intent;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
class ConnectionController {

    private final ConnectionService connections;

    ConnectionController(ConnectionService connections) {
        this.connections = connections;
    }

    @PutMapping("/api/likes/{userId}")
    LikeResult like(CurrentUser user, @PathVariable UUID userId, @RequestParam Intent intent) {
        return connections.like(user.id(), userId, intent);
    }

    @DeleteMapping("/api/likes/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unlike(CurrentUser user, @PathVariable UUID userId, @RequestParam Intent intent) {
        connections.unlike(user.id(), userId, intent);
    }

    @GetMapping("/api/matches")
    List<MatchSummary> matches(CurrentUser user) {
        return connections.matches(user.id());
    }

    @DeleteMapping("/api/matches/{matchId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unmatch(CurrentUser user, @PathVariable UUID matchId) {
        connections.unmatch(user.id(), matchId);
    }

    @GetMapping("/api/matches/{matchId}/messages")
    List<MessageResponse> messages(CurrentUser user, @PathVariable UUID matchId,
                                   @RequestParam(required = false) Instant after) {
        return connections.messages(user.id(), matchId, after);
    }

    @PostMapping("/api/matches/{matchId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    MessageResponse send(CurrentUser user, @PathVariable UUID matchId,
                         @Valid @RequestBody SendMessageRequest request) {
        return connections.send(user.id(), matchId, request.body());
    }
}
