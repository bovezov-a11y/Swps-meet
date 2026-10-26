package com.meetswps.connection;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findAllByMatchIdOrderByCreatedAtDesc(UUID matchId, Limit limit);

    List<Message> findAllByMatchIdAndCreatedAtAfterOrderByCreatedAt(UUID matchId, Instant after);

    Optional<Message> findFirstByMatchIdOrderByCreatedAtDesc(UUID matchId);
}
