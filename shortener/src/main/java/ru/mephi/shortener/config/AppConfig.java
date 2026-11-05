package ru.mephi.shortener.config;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public record AppConfig(
        String domain,
        int codeLength,
        Duration linkTtl,
        int defaultClickLimit,
        int maxClickLimit,
        int maxUrlLength,
        Duration cleanupInterval,
        Optional<Path> storageFile) {

    private static final int MIN_CODE_LENGTH = 4;
    private static final int MAX_CODE_LENGTH = 16;
    private static final int MIN_URL_LENGTH = 16;

    private static final Pattern DOMAIN = Pattern.compile("[A-Za-z0-9.-]+(:\\d{1,5})?");

    public AppConfig {
        Objects.requireNonNull(domain, "domain");
        Objects.requireNonNull(linkTtl, "linkTtl");
        Objects.requireNonNull(cleanupInterval, "cleanupInterval");
        Objects.requireNonNull(storageFile, "storageFile");
        require(DOMAIN.matcher(domain).matches(),
                "%s должен содержать только домен без схемы и пути, например clck.ru".formatted(ConfigKeys.DOMAIN));
        require(codeLength >= MIN_CODE_LENGTH && codeLength <= MAX_CODE_LENGTH,
                "%s должен быть от %d до %d".formatted(ConfigKeys.CODE_LENGTH, MIN_CODE_LENGTH, MAX_CODE_LENGTH));
        require(isPositive(linkTtl), "%s должен быть больше нуля".formatted(ConfigKeys.LINK_TTL));
        require(isPositive(cleanupInterval), "%s должен быть больше нуля".formatted(ConfigKeys.CLEANUP_INTERVAL));
        require(defaultClickLimit >= 1, "%s должен быть не меньше 1".formatted(ConfigKeys.DEFAULT_CLICK_LIMIT));
        require(maxClickLimit >= defaultClickLimit,
                "%s не может быть меньше %s".formatted(ConfigKeys.MAX_CLICK_LIMIT, ConfigKeys.DEFAULT_CLICK_LIMIT));
        require(maxUrlLength >= MIN_URL_LENGTH,
                "%s должен быть не меньше %d".formatted(ConfigKeys.MAX_URL_LENGTH, MIN_URL_LENGTH));
    }

    private static boolean isPositive(Duration duration) {
        return !duration.isNegative() && !duration.isZero();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new ConfigurationException("Некорректная конфигурация: " + message);
        }
    }
}
