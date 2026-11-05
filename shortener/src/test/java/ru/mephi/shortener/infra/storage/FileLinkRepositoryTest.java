package ru.mephi.shortener.infra.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.mephi.shortener.domain.ShortLink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileLinkRepositoryTest {

    private static final UUID OWNER = UUID.randomUUID();
    private static final Instant CREATED = Instant.parse("2026-11-05T10:00:00Z");

    private static ShortLink link(String code, int clicks) {
        return new ShortLink(code, "https://openjdk.org/" + code, OWNER, CREATED, CREATED.plusSeconds(60), 5, clicks);
    }

    @Test
    public void persistsLinksBetweenInstances(@TempDir Path dir) {
        Path file = dir.resolve("nested").resolve("links.json");
        FileLinkRepository first = new FileLinkRepository(file);
        first.save(link("aaa111", 0));
        first.save(link("bbb222", 0));
        first.save(link("aaa111", 3));
        first.delete("bbb222");

        FileLinkRepository second = new FileLinkRepository(file);

        assertEquals(Optional.of(link("aaa111", 3)), second.findByCode("aaa111"));
        assertFalse(second.existsByCode("bbb222"));
        assertEquals(List.of(link("aaa111", 3)), second.findByOwner(OWNER));
        assertFalse(Files.exists(file.resolveSibling("links.json.tmp")));
    }

    @Test
    public void startsEmptyWhenFileIsAbsent(@TempDir Path dir) {
        assertTrue(new FileLinkRepository(dir.resolve("links.json")).findAll().isEmpty());
    }

    @Test
    public void failsClearlyOnCorruptedFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("links.json");
        Files.writeString(file, "{not json");

        StorageException error = assertThrows(StorageException.class, () -> new FileLinkRepository(file));
        assertTrue(error.getMessage().contains("links.json"));
    }
}
