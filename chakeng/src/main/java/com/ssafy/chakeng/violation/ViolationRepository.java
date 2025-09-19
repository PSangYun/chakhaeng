package com.ssafy.chakeng.violation;

import com.ssafy.chakeng.violation.domain.Violation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ViolationRepository extends JpaRepository<Violation, UUID> {

    @Query("""
    select count(v)
    from Violation v
    where v.video.ownerId = :ownerId and v.createdAt >= :start
  """)
    long countToday(@Param("ownerId") UUID ownerId, @Param("start") OffsetDateTime start);

    Page<Violation> findByVideoOwnerIdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);

    List<Violation> findAllByVideoOwnerIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID ownerId, OffsetDateTime from, OffsetDateTime to
    );
}


