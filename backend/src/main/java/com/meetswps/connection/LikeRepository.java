package com.meetswps.connection;

import com.meetswps.profile.Intent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface LikeRepository extends JpaRepository<Like, UUID> {

    boolean existsByFromIdAndToIdAndIntent(UUID fromId, UUID toId, Intent intent);

    List<Like> findAllByFromIdAndToIdIn(UUID fromId, Collection<UUID> toIds);

    @Modifying
    @Query("delete from Like l where l.fromId = :fromId and l.toId = :toId and l.intent = :intent")
    int deleteLike(UUID fromId, UUID toId, Intent intent);

    @Modifying
    @Query("delete from Like l where (l.fromId = :a and l.toId = :b) or (l.fromId = :b and l.toId = :a)")
    void deleteBetween(UUID a, UUID b);
}
