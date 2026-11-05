package ru.mephi.shortener.service;

import ru.mephi.shortener.domain.error.InvalidInputException;

import java.time.Duration;
import java.util.Objects;
import java.util.OptionalInt;

public record LinkPolicy(Duration ttl, int defaultClickLimit, int maxClickLimit) {

    public LinkPolicy {
        Objects.requireNonNull(ttl, "ttl");
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("ttl must be positive: " + ttl);
        }
        if (defaultClickLimit < 1 || defaultClickLimit > maxClickLimit) {
            throw new IllegalArgumentException("defaultClickLimit must be within [1, maxClickLimit]");
        }
    }

    public int resolveClickLimit(OptionalInt requested) {
        return validateClickLimit(requested.orElse(defaultClickLimit));
    }

    public int validateClickLimit(int limit) {
        if (limit < 1 || limit > maxClickLimit) {
            throw new InvalidInputException(
                    "Лимит переходов должен быть от 1 до %d, указано %d".formatted(maxClickLimit, limit));
        }
        return limit;
    }
}
