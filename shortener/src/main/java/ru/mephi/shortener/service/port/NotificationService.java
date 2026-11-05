package ru.mephi.shortener.service.port;

import ru.mephi.shortener.domain.Notification;

@FunctionalInterface
public interface NotificationService {

    void publish(Notification notification);
}
