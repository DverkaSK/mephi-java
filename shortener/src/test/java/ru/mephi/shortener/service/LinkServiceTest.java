package ru.mephi.shortener.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mephi.shortener.MutableClock;
import ru.mephi.shortener.domain.LinkStatus;
import ru.mephi.shortener.domain.Notification;
import ru.mephi.shortener.domain.NotificationType;
import ru.mephi.shortener.domain.ShortLink;
import ru.mephi.shortener.domain.error.AccessDeniedException;
import ru.mephi.shortener.domain.error.CodeGenerationException;
import ru.mephi.shortener.domain.error.InvalidInputException;
import ru.mephi.shortener.domain.error.LinkNotFoundException;
import ru.mephi.shortener.domain.error.LinkUnavailableException;
import ru.mephi.shortener.infra.notification.InMemoryNotificationInbox;
import ru.mephi.shortener.infra.storage.InMemoryLinkRepository;
import ru.mephi.shortener.service.port.ShortCodeGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkServiceTest {

    private static final String URL = "https://www.baeldung.com/java-9-http-client";
    private static final Duration TTL = Duration.ofHours(24);
    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-05T10:00:00Z"));
    private final InMemoryLinkRepository repository = new InMemoryLinkRepository();
    private final InMemoryNotificationInbox inbox = new InMemoryNotificationInbox();

    private LinkService service(ShortCodeGenerator generator, Duration ttl) {
        return new LinkService(repository, generator, new UrlValidator(2048, "clck.ru"), inbox,
                new LinkPolicy(ttl, 10, 1000), clock);
    }

    private LinkService service() {
        return service(new RandomCodeGenerator(6), TTL);
    }

    @Test
    void sameUrlShortenedByDifferentUsersGetsDifferentCodes() {
        LinkService service = service();

        ShortLink alices = service.shorten(ALICE, URL, OptionalInt.empty());
        ShortLink bobs = service.shorten(BOB, URL, OptionalInt.empty());

        assertNotEquals(alices.code(), bobs.code());
        assertEquals(URL, service.follow(alices.code()).originalUrl());
        assertEquals(URL, service.follow(bobs.code()).originalUrl());
    }

    @Test
    void codesStayUniqueAcrossManyUsersAndLinks() {
        LinkService service = service(new RandomCodeGenerator(4), TTL);

        long distinct = IntStream.range(0, 2000)
                .mapToObj(i -> service.shorten(UUID.randomUUID(), URL, OptionalInt.empty()).code())
                .distinct()
                .count();

        assertEquals(2000, distinct);
    }

    @Test
    void retriesWhenGeneratedCodeIsTaken() {
        Deque<String> codes = new ArrayDeque<>(List.of("AAAAAA", "AAAAAA", "BBBBBB"));
        LinkService service = service(codes::poll, TTL);

        ShortLink first = service.shorten(ALICE, URL, OptionalInt.empty());
        ShortLink second = service.shorten(BOB, URL, OptionalInt.empty());

        assertEquals("AAAAAA", first.code());
        assertEquals("BBBBBB", second.code());
    }

    @Test
    void failsWhenNoFreeCodeFound() {
        LinkService service = service(() -> "SAME00", TTL);
        service.shorten(ALICE, URL, OptionalInt.empty());

        assertThrows(CodeGenerationException.class, () -> service.shorten(BOB, URL, OptionalInt.empty()));
    }

    @Test
    void userCanOwnSeveralLinksAndSeesOnlyOwnLinks() {
        LinkService service = service();
        ShortLink first = service.shorten(ALICE, "https://openjdk.org", OptionalInt.empty());
        clock.advance(Duration.ofSeconds(1));
        ShortLink second = service.shorten(ALICE, URL, OptionalInt.of(3));
        service.shorten(BOB, URL, OptionalInt.empty());

        assertEquals(List.of(first, second), service.linksOf(ALICE));
        assertEquals(1, service.linksOf(BOB).size());
    }

    @Test
    void usesDefaultLimitAndTtlFromPolicy() {
        ShortLink link = service().shorten(ALICE, URL, OptionalInt.empty());

        assertEquals(10, link.clickLimit());
        assertEquals(clock.instant().plus(TTL), link.expiresAt());
    }

    @Test
    void blocksLinkAfterClickLimitAndNotifiesOwnerOnce() {
        LinkService service = service();
        ShortLink link = service.shorten(ALICE, URL, OptionalInt.of(2));

        service.follow(link.code());
        assertTrue(inbox.drain(ALICE).isEmpty());
        ShortLink last = service.follow(link.code());

        assertTrue(last.isLimitExhausted());
        LinkUnavailableException blocked = assertThrows(LinkUnavailableException.class,
                () -> service.follow(link.code()));
        assertEquals(LinkStatus.LIMIT_EXHAUSTED, blocked.reason());
        assertThrows(LinkUnavailableException.class, () -> service.follow(link.code()));

        List<Notification> notifications = inbox.drain(ALICE);
        assertEquals(1, notifications.size());
        assertEquals(NotificationType.LIMIT_EXHAUSTED, notifications.getFirst().type());
        assertEquals(2, repository.findByCode(link.code()).orElseThrow().clickCount());
    }

    @Test
    void countsConcurrentClicksExactly() throws InterruptedException {
        LinkService service = service();
        ShortLink link = service.shorten(ALICE, URL, OptionalInt.of(100));
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(300);

        try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
            IntStream.range(0, 300).forEach(i -> executor.execute(() -> {
                try {
                    service.follow(link.code());
                    succeeded.incrementAndGet();
                } catch (LinkUnavailableException e) {
                    rejected.incrementAndGet();
                } finally {
                    done.countDown();
                }
            }));
            done.await();
        }

        assertEquals(100, succeeded.get());
        assertEquals(200, rejected.get());
        assertEquals(100, repository.findByCode(link.code()).orElseThrow().clickCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PT1H", "PT24H", "P7D"})
    void expiredLinkIsUnavailableExactlyAtTtl(String ttlText) {
        Duration ttl = Duration.parse(ttlText);
        LinkService service = service(new RandomCodeGenerator(6), ttl);
        ShortLink link = service.shorten(ALICE, URL, OptionalInt.empty());

        clock.advance(ttl.minusSeconds(1));
        assertEquals(URL, service.follow(link.code()).originalUrl());

        clock.advance(Duration.ofSeconds(1));
        LinkUnavailableException expired = assertThrows(LinkUnavailableException.class,
                () -> service.follow(link.code()));

        assertEquals(LinkStatus.EXPIRED, expired.reason());
        assertFalse(repository.existsByCode(link.code()));
        assertEquals(NotificationType.EXPIRED, inbox.drain(ALICE).getFirst().type());
    }

    @Test
    void purgeRemovesOnlyExpiredLinksAndNotifiesOwners() {
        LinkService service = service();
        ShortLink old = service.shorten(ALICE, URL, OptionalInt.empty());
        clock.advance(Duration.ofHours(12));
        ShortLink fresh = service.shorten(BOB, URL, OptionalInt.empty());
        clock.advance(Duration.ofHours(12));

        assertEquals(1, service.purgeExpired());

        assertFalse(repository.existsByCode(old.code()));
        assertTrue(repository.existsByCode(fresh.code()));
        assertEquals(1, inbox.drain(ALICE).size());
        assertTrue(inbox.drain(BOB).isEmpty());
        assertEquals(0, service.purgeExpired());
    }

    @Test
    void onlyOwnerCanEditOrDelete() {
        LinkService service = service();
        String code = service.shorten(ALICE, URL, OptionalInt.empty()).code();

        assertAll(
                () -> assertThrows(AccessDeniedException.class, () -> service.delete(BOB, code)),
                () -> assertThrows(AccessDeniedException.class, () -> service.updateClickLimit(BOB, code, 50)),
                () -> assertThrows(AccessDeniedException.class,
                        () -> service.updateUrl(BOB, code, "https://evil.example")),
                () -> assertThrows(AccessDeniedException.class, () -> service.linkOf(BOB, code)));
        assertEquals(URL, repository.findByCode(code).orElseThrow().originalUrl());

        service.delete(ALICE, code);
        assertThrows(LinkNotFoundException.class, () -> service.follow(code));
    }

    @Test
    void ownerCanRaiseLimitToUnblockLink() {
        LinkService service = service();
        String code = service.shorten(ALICE, URL, OptionalInt.of(1)).code();
        service.follow(code);
        assertThrows(LinkUnavailableException.class, () -> service.follow(code));

        ShortLink updated = service.updateClickLimit(ALICE, code, 3);

        assertEquals(2, updated.remainingClicks());
        assertEquals(URL, service.follow(code).originalUrl());
    }

    @Test
    void rejectsInvalidLimits() {
        LinkService service = service();
        String code = service.shorten(ALICE, URL, OptionalInt.of(5)).code();
        service.follow(code);
        service.follow(code);

        assertAll(
                () -> assertThrows(InvalidInputException.class, () -> service.updateClickLimit(ALICE, code, 2)),
                () -> assertThrows(InvalidInputException.class, () -> service.updateClickLimit(ALICE, code, 0)),
                () -> assertThrows(InvalidInputException.class, () -> service.updateClickLimit(ALICE, code, 1001)),
                () -> assertThrows(InvalidInputException.class, () -> service.shorten(ALICE, URL, OptionalInt.of(-1))));
        assertEquals(5, repository.findByCode(code).orElseThrow().clickLimit());
    }

    @Test
    void ownerCanChangeTargetUrlKeepingCode() {
        LinkService service = service();
        String code = service.shorten(ALICE, URL, OptionalInt.empty()).code();

        service.updateUrl(ALICE, code, "openjdk.org");

        assertEquals("https://openjdk.org", service.follow(code).originalUrl());
        assertThrows(InvalidInputException.class, () -> service.updateUrl(ALICE, code, "ftp://files.example"));
    }

    @Test
    void ownerOperationsOnExpiredLinkReportExpiration() {
        LinkService service = service();
        String code = service.shorten(ALICE, URL, OptionalInt.empty()).code();
        clock.advance(TTL);

        LinkUnavailableException error = assertThrows(LinkUnavailableException.class,
                () -> service.updateClickLimit(ALICE, code, 50));

        assertEquals(LinkStatus.EXPIRED, error.reason());
        assertTrue(service.linksOf(ALICE).isEmpty());
    }

    @Test
    void unknownCodeIsReported() {
        assertThrows(LinkNotFoundException.class, () -> service().follow("nope00"));
    }
}
