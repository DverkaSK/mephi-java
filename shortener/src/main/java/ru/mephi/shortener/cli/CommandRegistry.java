package ru.mephi.shortener.cli;

import ru.mephi.shortener.cli.command.DeleteCommand;
import ru.mephi.shortener.cli.command.EditCommand;
import ru.mephi.shortener.cli.command.ExitCommand;
import ru.mephi.shortener.cli.command.HelpCommand;
import ru.mephi.shortener.cli.command.InfoCommand;
import ru.mephi.shortener.cli.command.LimitCommand;
import ru.mephi.shortener.cli.command.ListCommand;
import ru.mephi.shortener.cli.command.LoginCommand;
import ru.mephi.shortener.cli.command.LogoutCommand;
import ru.mephi.shortener.cli.command.OpenCommand;
import ru.mephi.shortener.cli.command.ShortenCommand;
import ru.mephi.shortener.cli.command.WhoAmICommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class CommandRegistry {

    private final List<Command> commands = new ArrayList<>();
    private final Map<String, Command> lookup = new HashMap<>();

    public static CommandRegistry standard() {
        CommandRegistry registry = new CommandRegistry();
        return registry
                .register(new HelpCommand(registry))
                .register(new ShortenCommand())
                .register(new OpenCommand())
                .register(new ListCommand())
                .register(new InfoCommand())
                .register(new LimitCommand())
                .register(new EditCommand())
                .register(new DeleteCommand())
                .register(new WhoAmICommand())
                .register(new LoginCommand())
                .register(new LogoutCommand())
                .register(new ExitCommand());
    }

    public CommandRegistry register(Command command) {
        List<String> keys = Stream.concat(Stream.of(command.name()), command.aliases().stream())
                .map(CommandRegistry::normalize)
                .toList();
        keys.stream()
                .filter(lookup::containsKey)
                .findFirst()
                .ifPresent(key -> {
                    throw new IllegalArgumentException("Duplicate command name: " + key);
                });
        keys.forEach(key -> lookup.put(key, command));
        commands.add(command);
        return this;
    }

    public Optional<Command> find(String name) {
        return Optional.ofNullable(lookup.get(normalize(name)));
    }

    public List<Command> commands() {
        return List.copyOf(commands);
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
