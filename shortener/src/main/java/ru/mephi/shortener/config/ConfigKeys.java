package ru.mephi.shortener.config;

import java.util.List;

public final class ConfigKeys {

    public static final String DOMAIN = "link.domain";
    public static final String CODE_LENGTH = "link.code-length";
    public static final String LINK_TTL = "link.ttl";
    public static final String DEFAULT_CLICK_LIMIT = "link.click-limit.default";
    public static final String MAX_CLICK_LIMIT = "link.click-limit.max";
    public static final String MAX_URL_LENGTH = "link.url.max-length";
    public static final String CLEANUP_INTERVAL = "cleanup.interval";
    public static final String STORAGE_FILE = "storage.file";

    public static final List<String> ALL = List.of(
            DOMAIN, CODE_LENGTH, LINK_TTL, DEFAULT_CLICK_LIMIT, MAX_CLICK_LIMIT,
            MAX_URL_LENGTH, CLEANUP_INTERVAL, STORAGE_FILE);

    private ConfigKeys() {
    }
}
