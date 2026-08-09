package com.helix.gpo.web_crm.testimonial.internal;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
class TokenGenerator {

    private static final int TOKEN_BYTE_LENGTH = 32; // 256 bit Entropie
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Erzeugt einen kryptografisch zufälligen, für sich genommen bedeutungslosen
     * Token - enthält KEINE codierten Infos (keine Partner-/Tenant-ID etc.).
     */
    String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * SHA-256-Hash des Rohtokens - NUR der Hash wird persistiert.
     * Der Rohtoken existiert nur einmal, im Response an den Aufrufer.
     */
    String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

}
