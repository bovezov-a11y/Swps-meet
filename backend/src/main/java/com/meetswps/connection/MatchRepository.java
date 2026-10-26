package com.meetswps.connection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    @Query("select m from Match m where (m.userLow = :a and m.userHigh = :b) or (m.userLow = :b and m.userHigh = :a)")
    Optional<Match> findBetween(UUID a, UUID b);

    @Query("select m from Match m where m.userLow = :userId or m.userHigh = :userId order by m.createdAt desc")
    List<Match> findAllFor(UUID userId);
}
