package ru.mephi.shortener.cli;

import ru.mephi.shortener.domain.LinkStatus;
import ru.mephi.shortener.domain.Notification;
import ru.mephi.shortener.domain.ShortLink;
import ru.mephi.shortener.domain.error.LinkUnavailableException;
import ru.mephi.shortener.service.ShortLinkFormat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LinkView {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final ShortLinkFormat format;
    private final Clock clock;

    public LinkView(ShortLinkFormat format, Clock clock) {
        this.format = Objects.requireNonNull(format, "format");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public String shortUrl(ShortLink link) {
        return format.format(link.code());
    }

    public String details(ShortLink link) {
        return String.join(System.lineSeparator(),
                "  Короткая ссылка: " + shortUrl(link),
                "  Исходный адрес:  " + link.originalUrl(),
                "  Переходы:        %d из %d".formatted(link.clickCount(), link.clickLimit()),
                "  Статус:          " + status(link),
                "  Действует до:    " + expiry(link));
    }

    public String row(ShortLink link) {
        return "%-22s %7s  %-16s до %s  %s".formatted(
                shortUrl(link),
                link.clickCount() + "/" + link.clickLimit(),
                status(link),
                DATE_TIME.format(link.expiresAt().atZone(clock.getZone())),
                link.originalUrl());
    }

    public String status(ShortLink link) {
        return switch (link.statusAt(clock.instant())) {
            case ACTIVE -> "активна";
            case LIMIT_EXHAUSTED -> "лимит исчерпан";
            case EXPIRED -> "срок истек";
        };
    }

    public String expiry(ShortLink link) {
        Instant now = clock.instant();
        String moment = DATE_TIME.format(link.expiresAt().atZone(clock.getZone()));
        return link.isExpiredAt(now)
                ? moment + " (срок истек)"
                : "%s (осталось %s)".formatted(moment, humanDuration(Duration.between(now, link.expiresAt())));
    }

    public String notification(Notification notification) {
        String shortUrl = format.format(notification.code());
        return switch (notification.type()) {
            case LIMIT_EXHAUSTED -> ("Лимит переходов по ссылке %s (%s) исчерпан, ссылка заблокирована. "
                    + "Увеличьте лимит: limit %s <число> или создайте новую ссылку: shorten %s")
                    .formatted(shortUrl, notification.originalUrl(), notification.code(), notification.originalUrl());
            case EXPIRED -> "Срок жизни ссылки %s (%s) истек, ссылка удалена. Создайте новую: shorten %s"
                    .formatted(shortUrl, notification.originalUrl(), notification.originalUrl());
        };
    }

    public String unavailable(LinkUnavailableException error, boolean ownerIsViewing) {
        String shortUrl = format.format(error.code());
        List<String> parts = new ArrayList<>();
        if (error.reason() == LinkStatus.EXPIRED) {
            parts.add("Ссылка %s недоступна: срок ее жизни истек, ссылка удалена.".formatted(shortUrl));
        } else {
            parts.add("Ссылка %s недоступна: лимит переходов исчерпан.".formatted(shortUrl));
            if (ownerIsViewing) {
                parts.add("Увеличьте лимит: limit %s <число>.".formatted(error.code()));
            }
        }
        parts.add("Создать новую ссылку: shorten <url>");
        return String.join(" ", parts);
    }

    static String humanDuration(Duration duration) {
        if (duration.compareTo(Duration.ofMinutes(1)) < 0) {
            return "меньше минуты";
        }
        List<String> parts = new ArrayList<>();
        if (duration.toDays() > 0) {
            parts.add(duration.toDays() + " д");
        }
        if (duration.toHoursPart() > 0) {
            parts.add(duration.toHoursPart() + " ч");
        }
        if (duration.toMinutesPart() > 0) {
            parts.add(duration.toMinutesPart() + " мин");
        }
        return String.join(" ", parts);
    }
}
