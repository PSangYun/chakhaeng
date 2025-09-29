package com.ssafy.chakeng.video.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUploadUrlRequest {
    @NotBlank
    private String filename;     // 예: sample.mp4
    private String contentType;  // 예: video/mp4 (미지정 시 기본 video/mp4)
}