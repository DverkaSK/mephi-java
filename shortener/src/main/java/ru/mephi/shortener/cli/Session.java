package ru.mephi.shortener.cli;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public final class Session {

    private final Supplier<UUID> idGenerator;
    private UUID currentUser;

    public Session(Supplier<UUID> idGenerator) {
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public Optional<UUID> currentUser() {
        return Optional.ofNullable(currentUser);
    }

    public UUID requireUser() {
        return currentUser().orElseThrow(() -> new CommandException(
                "Вы еще не определены как пользователь. Создайте ссылку командой shorten или войдите: login <UUID>"));
    }

    public UUID newUserId() {
        return idGenerator.get();
    }

    public void login(UUID user) {
        this.currentUser = Objects.requireNonNull(user, "user");
    }

    public void logout() {
        this.currentUser = null;
    }
}
