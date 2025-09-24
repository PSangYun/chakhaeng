package com.ssafy.chakeng.report;

import com.ssafy.chakeng.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    @Query("""
    select count(r)
    from Report r
    where r.ownerId = :ownerId and (r.status = "COMPLETED" or r.status = "PENDING")
      and r.createdAt >= :start
  """)
    long countTodayCompleted(@Param("ownerId") UUID ownerId, @Param("start") OffsetDateTime start);

    boolean existsByOwnerIdAndPlateNumberAndOccurredAt(UUID ownerId, String plateNumber, java.time.OffsetDateTime occurredAt);

    List<Report> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    Optional<Report> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<Report> findByOwnerIdAndCreatedAtBetween(UUID userId, OffsetDateTime from, OffsetDateTime to);

    List<Report> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
}
