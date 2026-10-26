package com.meetswps.connection;

import java.util.UUID;

/** Published when a chat message is sent; listeners run after the message is saved. */
public record MessageSent(UUID matchId, UUID senderId, UUID recipientId, String body) {
}
