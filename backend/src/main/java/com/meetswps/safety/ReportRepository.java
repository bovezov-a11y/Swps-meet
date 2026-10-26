package com.meetswps.safety;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

interface ReportRepository extends JpaRepository<Report, UUID> {

    boolean existsByReporterIdAndTargetId(UUID reporterId, UUID targetId);

    @Query("select count(r) from Report r where r.targetId = :targetId and r.resolvedAt is null")
    long countOpen(UUID targetId);

    @Query("select r from Report r where r.resolvedAt is null order by r.createdAt")
    List<Report> findOpen();

    @Query("select r from Report r where r.targetId = :targetId and r.resolvedAt is null")
    List<Report> findOpenFor(UUID targetId);
}
