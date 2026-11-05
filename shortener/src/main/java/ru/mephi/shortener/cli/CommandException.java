package ru.mephi.shortener.cli;

import java.io.Serial;

public final class CommandException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CommandException(String message) {
        super(message);
    }

    public static CommandException usage(Command command) {
        return new CommandException("Неверные аргументы. Использование: " + command.usage());
    }
}
