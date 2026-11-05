package ru.mephi.shortener.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Notification(UUID recipient, NotificationType type, String code, String originalUrl, Instant createdAt) {

    public Notification {
        Objects.requireNonNull(recipient, "recipient");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(originalUrl, "originalUrl");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public static Notification about(ShortLink link, NotificationType type, Instant createdAt) {
        return new Notification(link.owner(), type, link.code(), link.originalUrl(), createdAt);
    }
}
