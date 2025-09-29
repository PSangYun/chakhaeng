package com.ssafy.chakeng.video;

import com.ssafy.chakeng.user.UserRepository;
import com.ssafy.chakeng.user.domain.User;
import com.ssafy.chakeng.video.domain.Video;
import com.ssafy.chakeng.video.dto.VideoCompleteRequest;
import com.ssafy.chakeng.video.dto.VideoResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    @Transactional
    public VideoResponse registerUploadedObject(UUID userId, VideoCompleteRequest req) {

        Video video = new Video();
        video.setOwnerId(userId);
        video.setObjectKey(req.getObjectKey());
        video.setOriginalName(req.getOriginalName());
        video.setContentType(req.getContentType());
        video.setSizeBytes(req.getSizeBytes());
        video.setDurationSec(req.getDurationSec());
        video.setStatus(Video.VideoStatus.UPLOADED);
        video.setCreatedAt(OffsetDateTime.now());

        Video saved = videoRepository.save(video);

        return VideoResponse.builder()
                .id(saved.getId())
                .objectKey(saved.getObjectKey())
                .originalName(saved.getOriginalName())
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
