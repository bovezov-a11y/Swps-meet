package com.meetswps.connection;

import com.meetswps.common.InvalidRequestException;
import com.meetswps.common.NotFoundException;
import com.meetswps.connection.ConnectionDtos.LikeResult;
import com.meetswps.connection.ConnectionDtos.MatchSummary;
import com.meetswps.connection.ConnectionDtos.MessageResponse;
import com.meetswps.profile.Audience;
import com.meetswps.profile.Intent;
import com.meetswps.profile.Profile;
import com.meetswps.profile.ProfileRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class ConnectionService {

    static final int MESSAGE_PAGE = 100;

    private final LikeRepository likes;
    private final MatchRepository matches;
    private final MessageRepository messages;
    private final ProfileRepository profiles;
    private final Audience audience;
    private final ProfileCards cards;
    private final Connections connections;

    ConnectionService(LikeRepository likes, MatchRepository matches, MessageRepository messages,
                      ProfileRepository profiles, Audience audience, ProfileCards cards, Connections connections) {
        this.likes = likes;
        this.matches = matches;
        this.messages = messages;
        this.profiles = profiles;
        this.audience = audience;
        this.cards = cards;
        this.connections = connections;
    }

    /** Records that the viewer wants to meet the target in one section; a like back makes a match. */
    @Transactional
    LikeResult like(UUID viewerId, UUID targetId, Intent intent) {
        Profile viewer = myProfile(viewerId);
        Profile target = audience.activeProfile(viewerId, targetId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
        if (!Audience.sharedIntents(viewer, target).contains(intent)) {
            throw new InvalidRequestException("intent", "you can only connect in a section you are both in");
        }
        Optional<Match> existing = matches.findBetween(viewerId, targetId);
        if (existing.isPresent()) {
            return new LikeResult(true, existing.get().getId());
        }
        if (!likes.existsByFromIdAndToIdAndIntent(viewerId, targetId, intent)) {
            likes.save(new Like(viewerId, targetId, intent));
        }
        if (likes.existsByFromIdAndToIdAndIntent(targetId, viewerId, intent)) {
            Match match = matches.save(new Match(viewerId, targetId, intent));
            return new LikeResult(true, match.getId());
        }
        return new LikeResult(false, null);
    }

    @Transactional
    void unlike(UUID viewerId, UUID targetId, Intent intent) {
        likes.deleteLike(viewerId, targetId, intent);
    }

    /** The viewer's matches, newest first, leaving out anyone who has since been hidden or banned. */
    @Transactional(readOnly = true)
    List<MatchSummary> matches(UUID viewerId) {
        Profile viewer = myProfile(viewerId);
        List<MatchSummary> result = new ArrayList<>();
        for (Match match : matches.findAllFor(viewerId)) {
            Optional<Profile> other = audience.activeProfile(viewerId, match.otherThan(viewerId));
            if (other.isEmpty()) {
                continue;
            }
            MessageResponse last = messages.findFirstByMatchIdOrderByCreatedAtDesc(match.getId())
                    .map(message -> MessageResponse.from(message, viewerId)).orElse(null);
            result.add(new MatchSummary(match.getId(), match.getIntent(), match.getCreatedAt(),
                    cards.build(viewer, other.get()), last));
        }
        return result;
    }

    /** Ends the match. Neither student is told, and their likes are cleared so they do not match again by itself. */
    @Transactional
    void unmatch(UUID viewerId, UUID matchId) {
        Match match = activeMatch(viewerId, matchId);
        connections.disconnect(viewerId, match.otherThan(viewerId));
    }

    /**
     * Messages oldest first: the latest {@value #MESSAGE_PAGE}, or, when {@code after} is given, only newer ones so
     * the app can poll cheaply.
     */
    @Transactional(readOnly = true)
    List<MessageResponse> messages(UUID viewerId, UUID matchId, Instant after) {
        activeMatch(viewerId, matchId);
        List<Message> found = after == null
                ? messages.findAllByMatchIdOrderByCreatedAtDesc(matchId, Limit.of(MESSAGE_PAGE)).reversed()
                : messages.findAllByMatchIdAndCreatedAtAfterOrderByCreatedAt(matchId, after);
        return found.stream().map(message -> MessageResponse.from(message, viewerId)).toList();
    }

    @Transactional
    MessageResponse send(UUID viewerId, UUID matchId, String body) {
        activeMatch(viewerId, matchId);
        return MessageResponse.from(messages.save(new Message(matchId, viewerId, body.strip())), viewerId);
    }

    private Match activeMatch(UUID viewerId, UUID matchId) {
        return matches.findById(matchId)
                .filter(match -> match.includes(viewerId))
                .filter(match -> audience.activeProfile(viewerId, match.otherThan(viewerId)).isPresent())
                .orElseThrow(() -> new NotFoundException("Match not found"));
    }

    private Profile myProfile(UUID viewerId) {
        return profiles.findById(viewerId)
                .orElseThrow(() -> new NotFoundException("Create your profile first"));
    }
}
