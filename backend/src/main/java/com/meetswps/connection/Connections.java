package com.meetswps.connection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Lets safety features end everything between two students when one blocks or reports the other. */
@Component
public class Connections {

    private final LikeRepository likes;
    private final MatchRepository matches;

    Connections(LikeRepository likes, MatchRepository matches) {
        this.likes = likes;
        this.matches = matches;
    }

    /** Removes their match (with its messages) and every like between them. */
    @Transactional
    public void disconnect(UUID a, UUID b) {
        matches.findBetween(a, b).ifPresent(matches::delete);
        likes.deleteBetween(a, b);
    }
}
