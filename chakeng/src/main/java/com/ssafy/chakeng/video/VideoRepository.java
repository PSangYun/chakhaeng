package com.ssafy.chakeng.video;

import com.ssafy.chakeng.video.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {
    List<Video> findAllByOwner_IdOrderByCreatedAtDesc(UUID ownerId);
}
