package ru.mephi.shortener.service.port;

import ru.mephi.shortener.domain.ShortLink;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepository {

    Optional<ShortLink> findByCode(String code);

    List<ShortLink> findByOwner(UUID owner);

    List<ShortLink> findAll();

    boolean existsByCode(String code);

    void save(ShortLink link);

    void delete(String code);
}
