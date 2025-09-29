package com.ssafy.chakeng.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String access;
    private long accessExpiresIn;
    private String refresh;
    private long refreshExpiresIn;
    private boolean isFirstLogin;
}
