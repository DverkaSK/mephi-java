package ru.mephi.shortener.service;

import ru.mephi.shortener.service.port.ShortCodeGenerator;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class RandomCodeGenerator implements ShortCodeGenerator {

    static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final int length;
    private final RandomGenerator random;

    public RandomCodeGenerator(int length) {
        this(length, new SecureRandom());
    }

    RandomCodeGenerator(int length, RandomGenerator random) {
        if (length < 1) {
            throw new IllegalArgumentException("length must be positive: " + length);
        }
        this.length = length;
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public String nextCode() {
        return random.ints(length, 0, ALPHABET.length())
                .map(ALPHABET::charAt)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
