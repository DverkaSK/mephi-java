package ru.mephi.shortener.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShortLinkTest {

    private static final Instant CREATED = Instant.parse("2026-11-05T10:00:00Z");
    private static final Instant EXPIRES = CREATED.plusSeconds(3600);

    private static ShortLink link(int limit, int clicks) {
        return new ShortLink("abc123", "https://openjdk.org", UUID.randomUUID(), CREATED, EXPIRES, limit, clicks);
    }

    @Test
    void reportsStatusByClicksAndTime() {
        assertEquals(LinkStatus.ACTIVE, link(2, 1).statusAt(CREATED));
        assertEquals(LinkStatus.LIMIT_EXHAUSTED, link(2, 2).statusAt(CREATED));
        assertEquals(LinkStatus.EXPIRED, link(2, 0).statusAt(EXPIRES));
    }

    @Test
    void clickIncrementsCounterAndStopsAtLimit() {
        ShortLink clicked = link(1, 0).withClick();

        assertEquals(1, clicked.clickCount());
        assertEquals(0, clicked.remainingClicks());
        assertThrows(IllegalStateException.class, clicked::withClick);
    }

    @Test
    void rejectsInconsistentState() {
        assertThrows(IllegalArgumentException.class, () -> link(0, 0));
        assertThrows(IllegalArgumentException.class, () -> link(1, 2));
        assertThrows(IllegalArgumentException.class, () -> link(1, -1));
        assertThrows(IllegalArgumentException.class,
                () -> new ShortLink("abc", "https://a.b", UUID.randomUUID(), CREATED, CREATED, 1, 0));
    }
}
