package ru.mephi.shortener.service;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public final class ShortLinkFormat {

    private static final Pattern CODE = Pattern.compile("[A-Za-z0-9]{1,32}");
    private static final Pattern SCHEME = Pattern.compile("^(?i)https?://");

    private final String domain;
    private final String prefix;

    public ShortLinkFormat(String domain) {
        this.domain = Objects.requireNonNull(domain, "domain").toLowerCase(Locale.ROOT);
        this.prefix = this.domain + "/";
    }

    public String domain() {
        return domain;
    }

    public String format(String code) {
        return prefix + code;
    }

    public boolean isShortLink(String input) {
        return codeAfterDomain(input).isPresent();
    }

    public Optional<String> parseCode(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String candidate = input.strip();
        return CODE.matcher(candidate).matches() ? Optional.of(candidate) : codeAfterDomain(candidate);
    }

    private Optional<String> codeAfterDomain(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String withoutScheme = SCHEME.matcher(input.strip()).replaceFirst("");
        if (!withoutScheme.toLowerCase(Locale.ROOT).startsWith(prefix)) {
            return Optional.empty();
        }
        String code = withoutScheme.substring(prefix.length());
        return CODE.matcher(code).matches() ? Optional.of(code) : Optional.empty();
    }
}
