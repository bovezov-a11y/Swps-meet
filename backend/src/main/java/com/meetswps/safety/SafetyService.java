package com.meetswps.safety;

import com.meetswps.common.ConflictException;
import com.meetswps.common.InvalidRequestException;
import com.meetswps.common.NotFoundException;
import com.meetswps.connection.Connections;
import com.meetswps.profile.ProfileRepository;
import com.meetswps.safety.SafetyDtos.BlockedUser;
import com.meetswps.safety.SafetyDtos.QueueItem;
import com.meetswps.safety.SafetyDtos.ReportEntry;
import com.meetswps.user.User;
import com.meetswps.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
class SafetyService {

    private final BlockRepository blocks;
    private final ReportRepository reports;
    private final UserRepository users;
    private final ProfileRepository profiles;
    private final Connections connections;
    private final int autoHideThreshold;

    SafetyService(BlockRepository blocks, ReportRepository reports, UserRepository users, ProfileRepository profiles,
                  Connections connections, @Value("${app.moderation.auto-hide-threshold}") int autoHideThreshold) {
        this.blocks = blocks;
        this.reports = reports;
        this.users = users;
        this.profiles = profiles;
        this.connections = connections;
        this.autoHideThreshold = autoHideThreshold;
    }

    /** Hides the two students from each other everywhere and ends any match between them. */
    @Transactional
    void block(UUID blockerId, UUID blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new InvalidRequestException("userId", "you cannot block yourself");
        }
        if (!users.existsById(blockedId)) {
            throw new NotFoundException("User not found");
        }
        if (!blocks.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            blocks.save(new Block(blockerId, blockedId));
        }
        connections.disconnect(blockerId, blockedId);
    }

    @Transactional
    void unblock(UUID blockerId, UUID blockedId) {
        blocks.deleteByPair(blockerId, blockedId);
    }

    @Transactional(readOnly = true)
    List<BlockedUser> blocked(UUID blockerId) {
        List<BlockedUser> result = new ArrayList<>();
        for (Block block : blocks.findAllByBlockerIdOrderByCreatedAtDesc(blockerId)) {
            users.findById(block.getBlockedId()).ifPresent(user ->
                    result.add(new BlockedUser(user.getId(), user.getFirstName(), block.getCreatedAt())));
        }
        return result;
    }

    /**
     * Reporting also blocks, so the reporter never sees the student again. Once enough different students report
     * the same profile it is hidden from everyone until a moderator reviews it.
     */
    @Transactional
    void report(UUID reporterId, UUID targetId, ReportReason reason, String details) {
        if (reporterId.equals(targetId)) {
            throw new InvalidRequestException("userId", "you cannot report yourself");
        }
        if (!users.existsById(targetId)) {
            throw new NotFoundException("User not found");
        }
        if (reports.existsByReporterIdAndTargetId(reporterId, targetId)) {
            throw new ConflictException("You have already reported this student");
        }
        reports.save(new Report(reporterId, targetId, reason,
                details == null || details.isBlank() ? null : details.strip()));
        block(reporterId, targetId);
        if (reports.countOpen(targetId) >= autoHideThreshold) {
            profiles.findById(targetId).ifPresent(profile -> profile.setHidden(true));
        }
    }

    @Transactional(readOnly = true)
    List<QueueItem> queue() {
        Map<UUID, List<Report>> grouped = new LinkedHashMap<>();
        for (Report report : reports.findOpen()) {
            grouped.computeIfAbsent(report.getTargetId(), id -> new ArrayList<>()).add(report);
        }
        List<QueueItem> items = new ArrayList<>();
        grouped.forEach((userId, userReports) -> users.findById(userId).ifPresent(user -> items.add(new QueueItem(
                userId, user.getFirstName(), user.getEmail(),
                profiles.findById(userId).map(com.meetswps.profile.Profile::isHidden).orElse(false), userReports.size(),
                userReports.stream().map(r -> new ReportEntry(r.getReason(), r.getDetails(), r.getCreatedAt()))
                        .toList()))));
        return items;
    }

    /** The reports were unfounded: the profile is shown again and the reports are closed. */
    @Transactional
    void restore(UUID userId) {
        users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        profiles.findById(userId).ifPresent(profile -> profile.setHidden(false));
        reports.findOpenFor(userId).forEach(Report::resolve);
    }

    /** Suspends the account: the student is logged out, cannot log in again and is hidden from everyone. */
    @Transactional
    void ban(UUID userId) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        user.ban();
        profiles.findById(userId).ifPresent(profile -> profile.setHidden(true));
        reports.findOpenFor(userId).forEach(Report::resolve);
    }
}
