package ru.mephi.shortener.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ShortLink(
        String code,
        String originalUrl,
        UUID owner,
        Instant createdAt,
        Instant expiresAt,
        int clickLimit,
        int clickCount) {

    public ShortLink {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(originalUrl, "originalUrl");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be after createdAt");
        }
        if (clickLimit < 1) {
            throw new IllegalArgumentException("clickLimit must be positive: " + clickLimit);
        }
        if (clickCount < 0 || clickCount > clickLimit) {
            throw new IllegalArgumentException("clickCount must be within [0, clickLimit]: " + clickCount);
        }
    }

    public boolean isOwnedBy(UUID user) {
        return owner.equals(user);
    }

    public boolean isExpiredAt(Instant moment) {
        return !moment.isBefore(expiresAt);
    }

    public boolean isLimitExhausted() {
        return clickCount >= clickLimit;
    }

    public int remainingClicks() {
        return clickLimit - clickCount;
    }

    public LinkStatus statusAt(Instant moment) {
        if (isExpiredAt(moment)) {
            return LinkStatus.EXPIRED;
        }
        return isLimitExhausted() ? LinkStatus.LIMIT_EXHAUSTED : LinkStatus.ACTIVE;
    }

    public ShortLink withClick() {
        if (isLimitExhausted()) {
            throw new IllegalStateException("Click limit already exhausted for " + code);
        }
        return new ShortLink(code, originalUrl, owner, createdAt, expiresAt, clickLimit, clickCount + 1);
    }

    public ShortLink withClickLimit(int newLimit) {
        return new ShortLink(code, originalUrl, owner, createdAt, expiresAt, newLimit, clickCount);
    }

    public ShortLink withOriginalUrl(String newUrl) {
        return new ShortLink(code, newUrl, owner, createdAt, expiresAt, clickLimit, clickCount);
    }
}
