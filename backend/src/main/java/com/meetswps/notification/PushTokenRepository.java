package com.meetswps.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface PushTokenRepository extends JpaRepository<PushToken, String> {

    List<PushToken> findAllByUserIdIn(Collection<UUID> userIds);
}
