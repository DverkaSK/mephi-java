package ru.mephi.shortener.infra.storage;

import ru.mephi.shortener.domain.ShortLink;
import ru.mephi.shortener.service.port.LinkRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryLinkRepository implements LinkRepository {

    private final Map<String, ShortLink> links = new ConcurrentHashMap<>();

    @Override
    public Optional<ShortLink> findByCode(String code) {
        return Optional.ofNullable(links.get(code));
    }

    @Override
    public List<ShortLink> findByOwner(UUID owner) {
        return links.values().stream()
                .filter(link -> link.isOwnedBy(owner))
                .toList();
    }

    @Override
    public List<ShortLink> findAll() {
        return List.copyOf(links.values());
    }

    @Override
    public boolean existsByCode(String code) {
        return links.containsKey(code);
    }

    @Override
    public void save(ShortLink link) {
        links.put(link.code(), link);
    }

    @Override
    public void delete(String code) {
        links.remove(code);
    }
}
