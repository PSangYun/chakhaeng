package com.ssafy.chakeng.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ssafy.chakeng.auth.dto.AuthResponse;
import com.ssafy.chakeng.auth.refresh.RefreshTokenStore;
import com.ssafy.chakeng.config.security.JwtTokenProvider;
import com.ssafy.chakeng.user.domain.User;
import com.ssafy.chakeng.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository users;
    private final JwtTokenProvider jwt;
    private final RefreshTokenStore refreshStore;

    private final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(List.of(
                    "978187782649-h21o05tmapn2lpsvjg8sudnnv24uq691.apps.googleusercontent.com"
            ))
            .build();

    public AuthResponse loginWithIdToken(String idToken) {
        GoogleIdToken t = verify(idToken);
        GoogleIdToken.Payload p = t.getPayload();

        String iss = p.getIssuer();
        if (!"https://accounts.google.com".equals(iss) && !"accounts.google.com".equals(iss))
            throw new BadCredentialsException("invalid issuer");

        String sub = p.getSubject();
        String email = (String) p.get("email");
        String name = (String) p.get("name");
        String picture = (String) p.get("picture");

        User u = (email != null) ? users.findByEmail(email).orElse(null) : null;
        if (u == null) u = users.findByProviderAndProviderId(User.AuthProvider.GOOGLE, sub).orElse(null);

        boolean first = false;
        if (u == null) {
            u = new User();
            u.setEmail(email);
            u.setProvider(User.AuthProvider.GOOGLE);
            u.setProviderId(sub);
            u.setName(Optional.ofNullable(name).orElse("사용자"));
            u.setPicture(picture);
            u.setActive(true);
            first = true;
        }
        users.save(u);

        String access = jwt.createAccessToken(u.getId(), u.getEmail());
        String refresh = jwt.createRefreshToken(u.getId());
        refreshStore.save(u.getId(), refresh, Duration.ofDays(30));

        return new AuthResponse(access, 150000, refresh, 30L * 24 * 60 * 60, first);
    }

    private GoogleIdToken verify(String idToken) {
        try {
            GoogleIdToken parsed = GoogleIdToken.parse(GsonFactory.getDefaultInstance(), idToken);
            if (!verifier.verify(parsed)) throw new BadCredentialsException("invalid id token");
            return parsed;
        } catch (Exception e) {
            throw new BadCredentialsException("google verify failed", e);
        }
    }
}
