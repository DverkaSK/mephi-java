package ru.mephi.shortener.service;

import org.junit.jupiter.api.Test;
import ru.mephi.shortener.MutableClock;
import ru.mephi.shortener.domain.NotificationType;
import ru.mephi.shortener.domain.ShortLink;
import ru.mephi.shortener.infra.notification.InMemoryNotificationInbox;
import ru.mephi.shortener.infra.storage.InMemoryLinkRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.OptionalInt;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpirationSchedulerTest {

    private static final UUID OWNER = UUID.randomUUID();

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-05T10:00:00Z"));
    private final InMemoryLinkRepository repository = new InMemoryLinkRepository();
    private final InMemoryNotificationInbox inbox = new InMemoryNotificationInbox();
    private final LinkService service = new LinkService(repository, new RandomCodeGenerator(6),
            new UrlValidator(2048, "clck.ru"), inbox, new LinkPolicy(Duration.ofHours(1), 10, 100), clock);

    @Test
    void removesExpiredLinksInBackgroundWithoutAnyAccess() throws InterruptedException {
        ShortLink link = service.shorten(OWNER, "https://openjdk.org", OptionalInt.empty());

        try (ExpirationScheduler scheduler = new ExpirationScheduler(service, Duration.ofMillis(20))) {
            scheduler.start();
            Thread.sleep(100);
            assertTrue(repository.existsByCode(link.code()));

            clock.advance(Duration.ofHours(1));
            long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
            while (repository.existsByCode(link.code()) && System.nanoTime() < deadline) {
                Thread.sleep(10);
            }
        }

        assertFalse(repository.existsByCode(link.code()));
        assertEquals(NotificationType.EXPIRED, inbox.drain(OWNER).getFirst().type());
    }

    @Test
    void sweepSurvivesFailures() {
        Clock broken = new Clock() {
            @Override
            public ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                throw new IllegalStateException("clock failure");
            }
        };
        LinkService failing = new LinkService(repository, new RandomCodeGenerator(6),
                new UrlValidator(2048, "clck.ru"), inbox, new LinkPolicy(Duration.ofHours(1), 10, 100), broken);

        try (ExpirationScheduler scheduler = new ExpirationScheduler(failing, Duration.ofMinutes(1))) {
            scheduler.sweep();
        }
        assertThrows(IllegalArgumentException.class, () -> new ExpirationScheduler(service, Duration.ZERO));
    }
}
