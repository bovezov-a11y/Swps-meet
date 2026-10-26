package com.meetswps.connection;

import com.meetswps.profile.Intent;

import java.util.UUID;

/** Published when two students like each other; listeners run after the match is saved. */
public record MatchCreated(UUID matchId, UUID firstUserId, UUID secondUserId, Intent intent) {
}
