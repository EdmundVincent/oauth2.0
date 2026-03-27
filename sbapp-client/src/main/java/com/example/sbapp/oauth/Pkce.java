package com.example.sbapp.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class Pkce {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Url = Base64.getUrlEncoder().withoutPadding();

    private Pkce() {
    }

    public static String newVerifier() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return base64Url.encodeToString(bytes);
    }

    public static String challengeS256(String verifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] sum = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return base64Url.encodeToString(sum);
        } catch (Exception e) {
            throw new IllegalStateException("failed to compute pkce challenge", e);
        }
    }

    public static String newState() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return base64Url.encodeToString(bytes);
    }

    public static String newNonce() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return base64Url.encodeToString(bytes);
    }
}
