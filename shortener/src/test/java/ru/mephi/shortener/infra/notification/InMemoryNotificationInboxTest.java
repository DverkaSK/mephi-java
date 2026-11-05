package ru.mephi.shortener.infra.notification;

import org.junit.jupiter.api.Test;
import ru.mephi.shortener.domain.Notification;
import ru.mephi.shortener.domain.NotificationType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryNotificationInboxTest {

    @Test
    public void deliversNotificationsOnlyToRecipientAndOnlyOnce() {
        InMemoryNotificationInbox inbox = new InMemoryNotificationInbox();
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();
        Notification first = new Notification(
                alice, NotificationType.EXPIRED, "a1", "https://a.ru", Instant.EPOCH);
        Notification second = new Notification(
                alice, NotificationType.LIMIT_EXHAUSTED, "a2", "https://b.ru", Instant.EPOCH);
        inbox.publish(first);
        inbox.publish(second);

        assertTrue(inbox.drain(bob).isEmpty());
        assertEquals(List.of(first, second), inbox.drain(alice));
        assertTrue(inbox.drain(alice).isEmpty());
    }
}
