package com.ssafy.chakeng.video.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VideoCompleteRequest {
    @NotBlank
    private String objectKey;
    @NotBlank
    private String originalName;

    private String contentType;
    private Long sizeBytes;
    private Integer durationSec;
}
