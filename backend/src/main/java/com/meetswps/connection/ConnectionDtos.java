package com.meetswps.connection;

import com.meetswps.profile.Intent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

final class ConnectionDtos {

    private ConnectionDtos() {
    }

    record LikeResult(boolean matched, UUID matchId) {
    }

    record MessageResponse(UUID id, boolean mine, String body, Instant createdAt) {

        static MessageResponse from(Message message, UUID viewerId) {
            return new MessageResponse(message.getId(), message.getSenderId().equals(viewerId), message.getBody(),
                    message.getCreatedAt());
        }
    }

    record MatchSummary(UUID id, Intent intent, Instant createdAt, ProfileCard person, MessageResponse lastMessage) {
    }

    record SendMessageRequest(@NotBlank @Size(max = 2000) String body) {
    }
}
