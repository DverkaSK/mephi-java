package ru.mephi.shortener.config;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DurationParser {

    private static final Pattern SHORT_FORM = Pattern.compile("(\\d{1,9})\\s*([smhd])");

    private DurationParser() {
    }

    static Duration parse(String key, String text) {
        String value = text.strip().toLowerCase(Locale.ROOT);
        Matcher matcher = SHORT_FORM.matcher(value);
        if (matcher.matches()) {
            long amount = Long.parseLong(matcher.group(1));
            return switch (matcher.group(2)) {
                case "s" -> Duration.ofSeconds(amount);
                case "m" -> Duration.ofMinutes(amount);
                case "h" -> Duration.ofHours(amount);
                case "d" -> Duration.ofDays(amount);
                default -> throw new IllegalStateException("Unexpected unit " + matcher.group(2));
            };
        }
        try {
            return Duration.parse(value.toUpperCase(Locale.ROOT));
        } catch (DateTimeParseException e) {
            String message = "Параметр %s: '%s' не является длительностью, примеры: 30s, 15m, 24h, 1d, PT1H";
            throw new ConfigurationException(message.formatted(key, text), e);
        }
    }
}
