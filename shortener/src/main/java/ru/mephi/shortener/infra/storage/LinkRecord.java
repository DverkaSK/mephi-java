package ru.mephi.shortener.infra.storage;

import ru.mephi.shortener.domain.ShortLink;

import java.time.Instant;
import java.util.UUID;

record LinkRecord(
        String code,
        String originalUrl,
        String owner,
        String createdAt,
        String expiresAt,
        int clickLimit,
        int clickCount) {

    static LinkRecord from(ShortLink link) {
        return new LinkRecord(
                link.code(),
                link.originalUrl(),
                link.owner().toString(),
                link.createdAt().toString(),
                link.expiresAt().toString(),
                link.clickLimit(),
                link.clickCount());
    }

    ShortLink toDomain() {
        return new ShortLink(
                code,
                originalUrl,
                UUID.fromString(owner),
                Instant.parse(createdAt),
                Instant.parse(expiresAt),
                clickLimit,
                clickCount);
    }
}
