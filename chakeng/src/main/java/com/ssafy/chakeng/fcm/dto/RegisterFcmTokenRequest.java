package com.ssafy.chakeng.fcm.dto;

import com.ssafy.chakeng.fcm.domain.DeviceToken;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterFcmTokenRequest {
    @NotBlank
    private String token;
    @NotNull
    private DeviceToken.Platform platform;
}

