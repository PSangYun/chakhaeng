package com.ssafy.chakeng.video;

import com.ssafy.chakeng.common.ApiResponse;
import com.ssafy.chakeng.video.dto.CreateUploadUrlRequest;
import com.ssafy.chakeng.video.dto.CreateUploadUrlResponse;
import com.ssafy.chakeng.video.dto.VideoCompleteRequest;
import com.ssafy.chakeng.video.dto.VideoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoUploadController {

    private final VideoStorageService videoStorageService;
    private final VideoService videoService;

    @PostMapping("/upload-url")
    public ResponseEntity<ApiResponse<CreateUploadUrlResponse>> createUploadUrl(@Valid @RequestBody CreateUploadUrlRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("S3 주소입니다",videoStorageService.createUploadUrl(req)));
    }

    @PostMapping("/videos/complete")
    public ResponseEntity<ApiResponse<VideoResponse>> complete(@RequestBody VideoCompleteRequest req,
                                                  @RequestAttribute("userId") UUID userId) {
        VideoResponse response = videoService.registerUploadedObject(userId, req);
        return ResponseEntity.ok(ApiResponse.ok("서버에 동영상 url 저장 완료욧",response));
    }

    @GetMapping("/{videoKey}/play-url")
    public ResponseEntity<Map<String, String>> getPlayUrl(@PathVariable("videoKey") String videoKey) {
        String objectKey = "videos/" + videoKey + ".mp4";
        String url = videoStorageService.createDownloadUrl(objectKey);
        return ResponseEntity.ok(Map.of("playUrl", url));
    }

    @GetMapping("/download-url")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@RequestParam("objectKey") String objectKey) {
        return ResponseEntity.ok(Map.of("downloadUrl", videoStorageService.createDownloadUrl(objectKey)));
    }
}
