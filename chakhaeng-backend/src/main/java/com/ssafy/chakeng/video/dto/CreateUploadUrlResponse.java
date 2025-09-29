package com.ssafy.chakeng.video.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateUploadUrlResponse {
    private String uploadUrl; // PUT presigned URL
    private String objectKey; // S3 key 저장용
}
