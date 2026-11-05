package ru.mephi.shortener.infra.notification;

import ru.mephi.shortener.domain.Notification;
import ru.mephi.shortener.service.port.NotificationInbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class InMemoryNotificationInbox implements NotificationInbox {

    private final Map<UUID, List<Notification>> pending = new HashMap<>();

    @Override
    public synchronized void publish(Notification notification) {
        pending.computeIfAbsent(notification.recipient(), recipient -> new ArrayList<>()).add(notification);
    }

    @Override
    public synchronized List<Notification> drain(UUID recipient) {
        List<Notification> notifications = pending.remove(recipient);
        return notifications == null ? List.of() : List.copyOf(notifications);
    }
}
