package com.helix.gpo.web_crm.shared;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

public final class ImageUploadValidator {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB

    private ImageUploadValidator() {
    }

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Es wurde keine Datei übermittelt.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Die Datei ist zu groß - maximal 5 MB erlaubt.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Nur JPEG-, PNG- oder WebP-Bilder sind erlaubt.");
        }
    }

    public static String generateKey(String prefix, UUID ownerId, MultipartFile file) {
        String extension = switch (file.getContentType()) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
        return prefix + "/" + ownerId + "/" + UUID.randomUUID() + "." + extension;
    }

}
