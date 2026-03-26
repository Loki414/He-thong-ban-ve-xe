package com.example.busticket.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class GoogleOAuthService {

    @Value("${google.oauth.client-id:}")
    private String clientId;

    public GoogleIdToken.Payload verifyIdToken(String idTokenString) {
        if (clientId == null || clientId.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Đăng nhập Google chưa được cấu hình (thiếu google.oauth.client-id).");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(clientId)).build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new ResponseStatusException(UNAUTHORIZED, "Không xác thực được token Google.");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            Boolean emailVerified = payload.getEmailVerified();
            if (emailVerified != null && !emailVerified) {
                throw new ResponseStatusException(UNAUTHORIZED, "Email Google chưa được xác minh.");
            }
            return payload;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token Google không hợp lệ.");
        }
    }
}
