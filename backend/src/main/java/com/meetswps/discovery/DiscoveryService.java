package com.meetswps.discovery;

import com.meetswps.common.InvalidRequestException;
import com.meetswps.common.NotFoundException;
import com.meetswps.connection.ProfileCard;
import com.meetswps.connection.ProfileCards;
import com.meetswps.profile.Audience;
import com.meetswps.profile.Campus;
import com.meetswps.profile.Gender;
import com.meetswps.profile.Intent;
import com.meetswps.profile.Profile;
import com.meetswps.profile.ProfileRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
class DiscoveryService {

    static final int PAGE_SIZE = 20;

    private final JdbcClient jdbc;
    private final ProfileRepository profiles;
    private final ProfileCards cards;
    private final Audience audience;

    DiscoveryService(JdbcClient jdbc, ProfileRepository profiles, ProfileCards cards, Audience audience) {
        this.jdbc = jdbc;
        this.profiles = profiles;
        this.cards = cards;
        this.audience = audience;
    }

    /**
     * Students in one section, most recently updated first. Applies the same rules as {@link Audience}, in SQL, so
     * paging stays correct.
     */
    @Transactional(readOnly = true)
    List<ProfileCard> discover(UUID viewerId, Intent intent, Filters filters, int page) {
        Profile viewer = myProfile(viewerId);
        if (!viewer.getIntents().contains(intent)) {
            throw new InvalidRequestException("intent", "add this to what you are looking for to see this section");
        }

        StringBuilder sql = new StringBuilder("""
                select p.user_id from profiles p join users u on u.id = p.user_id
                where p.user_id <> :viewer and u.email_verified and not u.banned and not p.hidden
                  and :intent = any(p.intents)
                  and not exists (select 1 from blocks b
                                  where (b.blocker_id = :viewer and b.blocked_id = p.user_id)
                                     or (b.blocker_id = p.user_id and b.blocked_id = :viewer))
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("viewer", viewerId);
        params.put("intent", intent.name());
        if (intent == Intent.DATING) {
            sql.append(" and :gender = any(p.interested_in) and p.gender in (:interestedIn)");
            params.put("gender", viewer.getGender().name());
            params.put("interestedIn", viewer.getInterestedIn().stream().map(Gender::name).toList());
        }
        if (filters.campus() != null) {
            sql.append(" and p.campus = :campus");
            params.put("campus", filters.campus().name());
        }
        if (filters.program() != null && !filters.program().isBlank()) {
            sql.append(" and p.program ilike :program");
            params.put("program", "%" + escapeLike(filters.program().strip()) + "%");
        }
        if (filters.interest() != null && !filters.interest().isBlank()) {
            sql.append(" and exists (select 1 from unnest(p.interests) i where lower(i) = lower(:interest))");
            params.put("interest", filters.interest().strip());
        }
        sql.append(" order by p.updated_at desc, p.user_id limit :limit offset :offset");
        params.put("limit", PAGE_SIZE);
        params.put("offset", Math.max(page, 0) * PAGE_SIZE);

        List<UUID> ids = jdbc.sql(sql.toString()).params(params).query(UUID.class).list();
        Map<UUID, Profile> byId = profiles.findAllById(ids).stream()
                .collect(Collectors.toMap(Profile::getUserId, Function.identity()));
        return cards.build(viewer, ids.stream().map(byId::get).filter(Objects::nonNull).toList());
    }

    /** Another student's profile, if the two share a section or have matched. */
    @Transactional(readOnly = true)
    ProfileCard profile(UUID viewerId, UUID targetId) {
        Profile viewer = myProfile(viewerId);
        return audience.activeProfile(viewerId, targetId)
                .filter(target -> !Audience.sharedIntents(viewer, target).isEmpty()
                        || audience.matched(viewerId, targetId))
                .map(target -> cards.build(viewer, target))
                .orElseThrow(() -> new NotFoundException("Profile not found"));
    }

    private Profile myProfile(UUID viewerId) {
        return profiles.findById(viewerId).orElseThrow(() -> new NotFoundException("Create your profile first"));
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    record Filters(Campus campus, String program, String interest) {
    }
}
