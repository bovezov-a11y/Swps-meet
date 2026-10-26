package com.meetswps.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {

    List<Photo> findAllByUserIdOrderByPosition(UUID userId);

    List<Photo> findAllByUserIdInOrderByPosition(Collection<UUID> userIds);
}
