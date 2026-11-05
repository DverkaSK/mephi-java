package ru.mephi.shortener.infra.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ru.mephi.shortener.domain.ShortLink;
import ru.mephi.shortener.service.port.LinkRepository;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class FileLinkRepository implements LinkRepository {

    private final Path file;
    private final ObjectMapper mapper;
    private final InMemoryLinkRepository cache = new InMemoryLinkRepository();

    public FileLinkRepository(Path file) {
        this(file, new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT));
    }

    FileLinkRepository(Path file, ObjectMapper mapper) {
        this.file = Objects.requireNonNull(file, "file").toAbsolutePath();
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        load();
    }

    public Path file() {
        return file;
    }

    @Override
    public synchronized Optional<ShortLink> findByCode(String code) {
        return cache.findByCode(code);
    }

    @Override
    public synchronized List<ShortLink> findByOwner(UUID owner) {
        return cache.findByOwner(owner);
    }

    @Override
    public synchronized List<ShortLink> findAll() {
        return cache.findAll();
    }

    @Override
    public synchronized boolean existsByCode(String code) {
        return cache.existsByCode(code);
    }

    @Override
    public synchronized void save(ShortLink link) {
        cache.save(link);
        persist();
    }

    @Override
    public synchronized void delete(String code) {
        cache.delete(code);
        persist();
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            Arrays.stream(mapper.readValue(file.toFile(), LinkRecord[].class))
                    .map(LinkRecord::toDomain)
                    .forEach(cache::save);
        } catch (IOException | RuntimeException e) {
            throw new StorageException("Не удалось прочитать файл хранилища " + file, e);
        }
    }

    private void persist() {
        List<LinkRecord> records = cache.findAll().stream()
                .sorted(Comparator.comparing(ShortLink::createdAt).thenComparing(ShortLink::code))
                .map(LinkRecord::from)
                .toList();
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            mapper.writeValue(temp.toFile(), records);
            move(temp);
        } catch (IOException e) {
            throw new StorageException("Не удалось сохранить файл хранилища " + file, e);
        }
    }

    private void move(Path temp) throws IOException {
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
