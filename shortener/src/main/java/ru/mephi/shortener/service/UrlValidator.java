package ru.mephi.shortener.service;

import ru.mephi.shortener.domain.error.InvalidInputException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class UrlValidator {

    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");
    private static final String DEFAULT_SCHEME_PREFIX = "https://";

    private final int maxLength;
    private final String ownDomain;

    public UrlValidator(int maxLength, String ownDomain) {
        if (maxLength < 1) {
            throw new IllegalArgumentException("maxLength must be positive: " + maxLength);
        }
        this.maxLength = maxLength;
        this.ownDomain = Objects.requireNonNull(ownDomain, "ownDomain");
    }

    public String normalize(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new InvalidInputException("URL не может быть пустым");
        }
        String trimmed = rawUrl.strip();
        String candidate = trimmed.contains("://") ? trimmed : DEFAULT_SCHEME_PREFIX + trimmed;
        if (candidate.length() > maxLength) {
            throw new InvalidInputException(
                    "URL слишком длинный: %d символов при максимуме %d".formatted(candidate.length(), maxLength));
        }
        URI uri = parse(candidate);
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_SCHEMES.contains(scheme)) {
            throw new InvalidInputException(
                    "Схема '%s' не поддерживается, используйте http или https".formatted(scheme));
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new InvalidInputException("В URL не указан домен: " + trimmed);
        }
        if (!host.contains(".") && !host.equalsIgnoreCase("localhost")) {
            throw new InvalidInputException("Некорректный домен '%s'".formatted(host));
        }
        if (host.equalsIgnoreCase(ownDomain)) {
            throw new InvalidInputException("Ссылка уже короткая, повторно сокращать ее не нужно");
        }
        return uri.toString();
    }

    private static URI parse(String candidate) {
        try {
            return new URI(candidate).parseServerAuthority();
        } catch (URISyntaxException e) {
            throw new InvalidInputException("Некорректный URL '%s': %s".formatted(candidate, e.getReason()));
        }
    }
}
