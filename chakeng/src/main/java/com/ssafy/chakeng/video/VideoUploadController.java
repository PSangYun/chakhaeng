package com.ssafy.chakeng.video;

import com.ssafy.chakeng.video.dto.CreateUploadUrlRequest;
import com.ssafy.chakeng.video.dto.CreateUploadUrlResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoUploadController {

    private final VideoStorageService videoStorageService;

    @PostMapping("/upload-url")
    public ResponseEntity<CreateUploadUrlResponse> createUploadUrl(@Valid @RequestBody CreateUploadUrlRequest req) {
        return ResponseEntity.ok(videoStorageService.createUploadUrl(req));
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
