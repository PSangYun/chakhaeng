package com.ssafy.chakeng.auth;

import com.ssafy.chakeng.auth.dto.AuthResponse;
import com.ssafy.chakeng.auth.dto.GoogleLoginRequest;
import com.ssafy.chakeng.auth.dto.RefreshRequest;

import com.ssafy.chakeng.auth.refresh.RefreshTokenStore;
import com.ssafy.chakeng.common.ApiResponse;
import com.ssafy.chakeng.config.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleAuthService googleAuth;
    private final JwtTokenProvider jwt;
    private final RefreshTokenStore refreshStore;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> google(@RequestBody GoogleLoginRequest req) {
        if (req.getIdToken() == null || req.getIdToken().isBlank())
            throw new BadCredentialsException("idToken required");
        AuthResponse authResponse = googleAuth.loginWithIdToken(req.getIdToken());
        return ResponseEntity.ok(ApiResponse.ok("인증 성공", authResponse));
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh(@RequestBody RefreshRequest req) {
        String refresh = req.getRefresh();
        if (refresh == null || jwt.isExpired(refresh))
            throw new BadCredentialsException("invalid refresh");
        UUID userId = jwt.extractUserId(refresh);
        if (!refreshStore.isValid(userId, refresh))
            throw new BadCredentialsException("revoked refresh");
        String newAccess = jwt.createAccessToken(userId, null);
        return Map.of("access", newAccess, "accessExpiresIn", 15 * 60);
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validate(
            Authentication authentication,
            @RequestHeader("Authorization") String authorization
    ) {
        String token = authorization.substring(7);
        var claims = jwt.parse(token).getBody();
        UUID userId = UUID.fromString(claims.getSubject());
        Date exp = claims.getExpiration();

        Map<String, Object> data = Map.of(
                "userId", userId.toString(),
                "expiresAt", exp.toInstant().toString()
        );
        return ResponseEntity.ok(ApiResponse.ok("Token valid.", data));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest req) {
        String refresh = req.getRefresh();
        if (refresh != null) {
            try {
                refreshStore.revoke(jwt.extractUserId(refresh), refresh);
            } catch (Exception ignored) {
            }
        }
        return ResponseEntity.ok().build();
    }
}
