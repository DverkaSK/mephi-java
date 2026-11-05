package ru.mephi.shortener.cli.command;

import ru.mephi.shortener.cli.Arguments;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.Command;
import ru.mephi.shortener.cli.CommandException;
import ru.mephi.shortener.cli.CommandRegistry;
import ru.mephi.shortener.cli.CommandResult;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

public final class HelpCommand implements Command {

    private static final int USAGE_WIDTH = 30;

    private final CommandRegistry registry;

    public HelpCommand(CommandRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public List<String> aliases() {
        return List.of("?", "h");
    }

    @Override
    public String usage() {
        return "help [команда]";
    }

    @Override
    public String description() {
        return "Показать список команд или подробную справку по одной команде";
    }

    @Override
    public List<String> examples() {
        return List.of("help", "help shorten");
    }

    @Override
    public CommandResult execute(List<String> args, CliContext context) {
        Arguments.requireCount(this, args, 0, 1);
        if (args.isEmpty()) {
            printOverview(context);
        } else {
            Command command = registry.find(args.getFirst())
                    .orElseThrow(() -> new CommandException("Команда '%s' не найдена".formatted(args.getFirst())));
            printDetails(command, context.out());
        }
        return CommandResult.CONTINUE;
    }

    private void printOverview(CliContext context) {
        PrintStream out = context.out();
        out.println("Доступные команды:");
        registry.commands().forEach(command ->
                out.printf("  %-" + USAGE_WIDTH + "s %s%n", command.usage(), command.description()));
        String sample = context.format().format("aB3xYz");
        out.println();
        out.println("Вместо <ссылка> можно указать как код (aB3xYz), так и короткую ссылку целиком (" + sample + ").");
        out.println("Чтобы перейти по короткой ссылке, достаточно вставить ее в консоль: " + sample);
        out.println();
        out.println("Пример сеанса:");
        out.println("  shorten https://www.baeldung.com/java-9-http-client 5");
        out.println("  " + sample);
        out.println("  list");
        out.println("  limit aB3xYz 20");
        out.println("  delete aB3xYz");
        out.println();
        out.println("Подробнее о команде: help <команда>");
    }

    private static void printDetails(Command command, PrintStream out) {
        out.println(command.usage());
        out.println("  " + command.description());
        if (!command.aliases().isEmpty()) {
            out.println("  Синонимы: " + String.join(", ", command.aliases()));
        }
        if (!command.examples().isEmpty()) {
            out.println("  Примеры:");
            command.examples().forEach(example -> out.println("    " + example));
        }
    }
}
