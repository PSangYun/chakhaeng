package com.ssafy.chakeng.video;

import com.ssafy.chakeng.video.dto.CreateUploadUrlRequest;
import com.ssafy.chakeng.video.dto.CreateUploadUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoStorageService {

    private final S3Presigner presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${app.upload.url-expire-minutes:15}")
    private int uploadExpireMinutes;

    @Value("${app.download.url-expire-minutes:10}")
    private int downloadExpireMinutes;

    public CreateUploadUrlResponse createUploadUrl(CreateUploadUrlRequest req) {
        String contentType = (req.getContentType() == null || req.getContentType().isBlank())
                ? "video/mp4" : req.getContentType();

        // S3 object key 규칙 예시: videos/{uuid}_{원파일명}
        String key = "videos/%s_%s".formatted(UUID.randomUUID(), req.getFilename());

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(uploadExpireMinutes))
                .putObjectRequest(put)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
        URL url = presigned.url();

        return new CreateUploadUrlResponse(url.toString(), key);
    }

    public String createDownloadUrl(String objectKey) {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(downloadExpireMinutes))
                .getObjectRequest(get)
                .build();

        PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
    }
}