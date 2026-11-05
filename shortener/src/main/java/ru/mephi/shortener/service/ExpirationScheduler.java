package ru.mephi.shortener.service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ExpirationScheduler implements AutoCloseable {

    private static final System.Logger LOGGER = System.getLogger(ExpirationScheduler.class.getName());

    private final LinkService linkService;
    private final Duration interval;
    private final ScheduledExecutorService executor;

    public ExpirationScheduler(LinkService linkService, Duration interval) {
        this.linkService = Objects.requireNonNull(linkService, "linkService");
        this.interval = Objects.requireNonNull(interval, "interval");
        if (interval.isNegative() || interval.isZero()) {
            throw new IllegalArgumentException("interval must be positive: " + interval);
        }
        this.executor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "link-expiration");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        long periodMillis = interval.toMillis();
        executor.scheduleWithFixedDelay(this::sweep, 0, periodMillis, TimeUnit.MILLISECONDS);
    }

    void sweep() {
        try {
            int removed = linkService.purgeExpired();
            if (removed > 0) {
                LOGGER.log(System.Logger.Level.DEBUG, "Removed {0} expired links", removed);
            }
        } catch (RuntimeException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Expired links cleanup failed", e);
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
