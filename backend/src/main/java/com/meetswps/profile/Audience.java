package com.meetswps.profile;

import com.meetswps.connection.MatchRepository;
import com.meetswps.safety.Blocks;
import com.meetswps.user.User;
import com.meetswps.user.UserRepository;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Decides who may see whom. Students only see each other in sections they have both chosen. Blocks, reports and bans
 * hide a student from everyone affected.
 */
@Component
public class Audience {

    private final ProfileRepository profiles;
    private final UserRepository users;
    private final Blocks blocks;
    private final MatchRepository matches;

    Audience(ProfileRepository profiles, UserRepository users, Blocks blocks, MatchRepository matches) {
        this.profiles = profiles;
        this.users = users;
        this.blocks = blocks;
        this.matches = matches;
    }

    /** The intents in which {@code viewer} and {@code target} can see each other. */
    public static Set<Intent> sharedIntents(Profile viewer, Profile target) {
        Set<Intent> shared = EnumSet.noneOf(Intent.class);
        shared.addAll(viewer.getIntents());
        shared.retainAll(target.getIntents());
        return shared;
    }

    /**
     * The target's profile if it is active and not blocked either way. Whether the two share a section is up to the
     * caller, since matched students keep seeing each other after one leaves the section.
     */
    public Optional<Profile> activeProfile(UUID viewerId, UUID targetId) {
        if (viewerId.equals(targetId) || blocks.existBetween(viewerId, targetId)) {
            return Optional.empty();
        }
        boolean active = users.findById(targetId).filter(User::isEmailVerified).filter(user -> !user.isBanned())
                .isPresent();
        return active ? profiles.findById(targetId).filter(profile -> !profile.isHidden()) : Optional.empty();
    }

    public boolean matched(UUID a, UUID b) {
        return matches.findBetween(a, b).isPresent();
    }
}
