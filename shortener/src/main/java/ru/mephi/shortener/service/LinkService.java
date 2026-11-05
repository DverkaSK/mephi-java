package ru.mephi.shortener.service;

import ru.mephi.shortener.domain.LinkStatus;
import ru.mephi.shortener.domain.Notification;
import ru.mephi.shortener.domain.NotificationType;
import ru.mephi.shortener.domain.ShortLink;
import ru.mephi.shortener.domain.error.AccessDeniedException;
import ru.mephi.shortener.domain.error.CodeGenerationException;
import ru.mephi.shortener.domain.error.InvalidInputException;
import ru.mephi.shortener.domain.error.LinkNotFoundException;
import ru.mephi.shortener.domain.error.LinkUnavailableException;
import ru.mephi.shortener.service.port.LinkRepository;
import ru.mephi.shortener.service.port.NotificationService;
import ru.mephi.shortener.service.port.ShortCodeGenerator;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Stream;

public final class LinkService {

    static final int MAX_GENERATION_ATTEMPTS = 20;

    private final LinkRepository repository;
    private final ShortCodeGenerator codeGenerator;
    private final UrlValidator urlValidator;
    private final NotificationService notifications;
    private final LinkPolicy policy;
    private final Clock clock;

    public LinkService(
            LinkRepository repository,
            ShortCodeGenerator codeGenerator,
            UrlValidator urlValidator,
            NotificationService notifications,
            LinkPolicy policy,
            Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.codeGenerator = Objects.requireNonNull(codeGenerator, "codeGenerator");
        this.urlValidator = Objects.requireNonNull(urlValidator, "urlValidator");
        this.notifications = Objects.requireNonNull(notifications, "notifications");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public LinkPolicy policy() {
        return policy;
    }

    public synchronized ShortLink shorten(UUID owner, String rawUrl, OptionalInt requestedLimit) {
        Objects.requireNonNull(owner, "owner");
        String url = urlValidator.normalize(rawUrl);
        int clickLimit = policy.resolveClickLimit(requestedLimit);
        Instant now = clock.instant();
        ShortLink link = new ShortLink(generateFreeCode(), url, owner, now, now.plus(policy.ttl()), clickLimit, 0);
        repository.save(link);
        return link;
    }

    public synchronized ShortLink follow(String code) {
        ShortLink link = repository.findByCode(code).orElseThrow(() -> new LinkNotFoundException(code));
        ensureNotExpired(link);
        if (link.isLimitExhausted()) {
            throw new LinkUnavailableException(link, LinkStatus.LIMIT_EXHAUSTED);
        }
        ShortLink clicked = link.withClick();
        repository.save(clicked);
        if (clicked.isLimitExhausted()) {
            notifications.publish(Notification.about(clicked, NotificationType.LIMIT_EXHAUSTED, clock.instant()));
        }
        return clicked;
    }

    public synchronized List<ShortLink> linksOf(UUID owner) {
        purgeExpired();
        return repository.findByOwner(owner).stream()
                .sorted(Comparator.comparing(ShortLink::createdAt).thenComparing(ShortLink::code))
                .toList();
    }

    public synchronized ShortLink linkOf(UUID owner, String code) {
        return requireOwnedActive(owner, code);
    }

    public synchronized ShortLink updateClickLimit(UUID owner, String code, int newLimit) {
        ShortLink link = requireOwnedActive(owner, code);
        policy.validateClickLimit(newLimit);
        if (newLimit <= link.clickCount()) {
            throw new InvalidInputException(
                    "Новый лимит должен быть больше числа уже совершенных переходов (%d)".formatted(link.clickCount()));
        }
        ShortLink updated = link.withClickLimit(newLimit);
        repository.save(updated);
        return updated;
    }

    public synchronized ShortLink updateUrl(UUID owner, String code, String rawUrl) {
        ShortLink link = requireOwnedActive(owner, code);
        ShortLink updated = link.withOriginalUrl(urlValidator.normalize(rawUrl));
        repository.save(updated);
        return updated;
    }

    public synchronized void delete(UUID owner, String code) {
        requireOwnedActive(owner, code);
        repository.delete(code);
    }

    public synchronized int purgeExpired() {
        Instant now = clock.instant();
        List<ShortLink> expired = repository.findAll().stream()
                .filter(link -> link.isExpiredAt(now))
                .toList();
        expired.forEach(this::expire);
        return expired.size();
    }

    private ShortLink requireOwnedActive(UUID owner, String code) {
        Objects.requireNonNull(owner, "owner");
        ShortLink link = repository.findByCode(code).orElseThrow(() -> new LinkNotFoundException(code));
        if (!link.isOwnedBy(owner)) {
            throw new AccessDeniedException(code);
        }
        ensureNotExpired(link);
        return link;
    }

    private void ensureNotExpired(ShortLink link) {
        if (link.isExpiredAt(clock.instant())) {
            expire(link);
            throw new LinkUnavailableException(link, LinkStatus.EXPIRED);
        }
    }

    private void expire(ShortLink link) {
        repository.delete(link.code());
        notifications.publish(Notification.about(link, NotificationType.EXPIRED, clock.instant()));
    }

    private String generateFreeCode() {
        return Stream.generate(codeGenerator::nextCode)
                .limit(MAX_GENERATION_ATTEMPTS)
                .filter(code -> !repository.existsByCode(code))
                .findFirst()
                .orElseThrow(() -> new CodeGenerationException(MAX_GENERATION_ATTEMPTS));
    }
}
