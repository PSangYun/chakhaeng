package com.ssafy.chakeng.fcm;

import com.ssafy.chakeng.fcm.dto.RegisterFcmTokenRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FCMController {
    private final DeviceTokenService deviceTokenService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterFcmTokenRequest req,
                                         @RequestAttribute("userId") UUID userId) {
        deviceTokenService.registerOrUpdate(userId, req);
        return ResponseEntity.ok().build();
    }
}
