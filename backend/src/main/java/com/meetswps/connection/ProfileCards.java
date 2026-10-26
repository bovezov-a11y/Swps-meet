package com.meetswps.connection;

import com.meetswps.profile.Audience;
import com.meetswps.profile.Intent;
import com.meetswps.profile.Photo;
import com.meetswps.profile.PhotoRepository;
import com.meetswps.profile.Profile;
import com.meetswps.user.User;
import com.meetswps.user.UserRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Builds {@link ProfileCard}s for a viewer in a few queries, however many profiles are shown. */
@Component
public class ProfileCards {

    private final UserRepository users;
    private final PhotoRepository photos;
    private final LikeRepository likes;
    private final MatchRepository matches;

    ProfileCards(UserRepository users, PhotoRepository photos, LikeRepository likes, MatchRepository matches) {
        this.users = users;
        this.photos = photos;
        this.likes = likes;
        this.matches = matches;
    }

    /** Cards in the order of {@code targets}. */
    public List<ProfileCard> build(Profile viewer, Collection<Profile> targets) {
        if (targets.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = targets.stream().map(Profile::getUserId).toList();
        Map<UUID, User> usersById = users.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<UUID, List<UUID>> photoIds = new HashMap<>();
        for (Photo photo : photos.findAllByUserIdInOrderByPosition(ids)) {
            photoIds.computeIfAbsent(photo.getUserId(), id -> new ArrayList<>()).add(photo.getId());
        }
        Map<UUID, Set<Intent>> liked = new HashMap<>();
        for (Like like : likes.findAllByFromIdAndToIdIn(viewer.getUserId(), ids)) {
            liked.computeIfAbsent(like.getToId(), id -> EnumSet.noneOf(Intent.class)).add(like.getIntent());
        }
        Map<UUID, UUID> matchIds = new HashMap<>();
        for (Match match : matches.findAllFor(viewer.getUserId())) {
            matchIds.put(match.otherThan(viewer.getUserId()), match.getId());
        }

        List<ProfileCard> cards = new ArrayList<>();
        for (Profile target : targets) {
            UUID id = target.getUserId();
            Set<Intent> shared = Audience.sharedIntents(viewer, target);
            boolean internship = shared.contains(Intent.INTERNSHIP);
            UUID matchId = matchIds.get(id);
            cards.add(new ProfileCard(id, usersById.get(id).getFirstName(), target.getAge(), target.getCampus(),
                    target.getProgram(), target.getStudyYear(), target.getDegreeLevel(), target.getBio(),
                    target.getInterests(), target.getLanguages(), photoIds.getOrDefault(id, List.of()), shared,
                    internship ? target.getInternshipRole() : null, internship ? target.getIndustry() : null,
                    internship ? target.getWorkplace() : null, liked.getOrDefault(id, Set.of()), matchId,
                    matchId != null ? target.getInstagram() : null));
        }
        return cards;
    }

    public ProfileCard build(Profile viewer, Profile target) {
        return build(viewer, List.of(target)).getFirst();
    }
}
