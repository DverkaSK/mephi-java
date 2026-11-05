package ru.mephi.shortener.service.port;

import ru.mephi.shortener.domain.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationInbox extends NotificationService {

    List<Notification> drain(UUID recipient);
}
