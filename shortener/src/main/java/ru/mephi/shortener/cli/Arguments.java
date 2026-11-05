package ru.mephi.shortener.cli;

import java.util.List;
import java.util.UUID;

public final class Arguments {

    private Arguments() {
    }

    public static void requireCount(Command command, List<String> args, int min, int max) {
        if (args.size() < min || args.size() > max) {
            throw CommandException.usage(command);
        }
    }

    public static int parseLimit(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new CommandException("Лимит переходов должен быть целым числом, указано '%s'".formatted(value));
        }
    }

    public static UUID parseUuid(String value) {
        try {
            UUID uuid = UUID.fromString(value);
            if (!uuid.toString().equalsIgnoreCase(value)) {
                throw new IllegalArgumentException(value);
            }
            return uuid;
        } catch (IllegalArgumentException e) {
            throw new CommandException(
                    "'%s' не является UUID, пример: 3f2b8c1e-5d4a-4e6b-9c7d-1a2b3c4d5e6f".formatted(value));
        }
    }

    public static String parseCode(CliContext context, String value) {
        return context.format().parseCode(value)
                .orElseThrow(() -> new CommandException("'%s' не похоже на короткую ссылку, пример: %s"
                        .formatted(value, context.format().format("aB3xYz"))));
    }
}
