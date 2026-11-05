package ru.mephi.shortener.cli;

import java.util.List;

public interface Command {

    String name();

    default List<String> aliases() {
        return List.of();
    }

    String usage();

    String description();

    default List<String> examples() {
        return List.of();
    }

    CommandResult execute(List<String> args, CliContext context);
}
