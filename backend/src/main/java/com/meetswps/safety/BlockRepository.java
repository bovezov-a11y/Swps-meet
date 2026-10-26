package com.meetswps.safety;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface BlockRepository extends JpaRepository<Block, UUID> {

    boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    @Query("select count(b) > 0 from Block b where (b.blockerId = :a and b.blockedId = :b) "
            + "or (b.blockerId = :b and b.blockedId = :a)")
    boolean existsBetween(UUID a, UUID b);

    List<Block> findAllByBlockerIdOrderByCreatedAtDesc(UUID blockerId);

    @Modifying
    @Query("delete from Block b where b.blockerId = :blockerId and b.blockedId = :blockedId")
    int deleteByPair(UUID blockerId, UUID blockedId);

    Optional<Block> findByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);
}
