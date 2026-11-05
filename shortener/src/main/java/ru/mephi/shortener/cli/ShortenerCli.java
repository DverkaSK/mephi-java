package ru.mephi.shortener.cli;

import ru.mephi.shortener.cli.command.OpenCommand;
import ru.mephi.shortener.domain.error.ShortenerException;
import ru.mephi.shortener.service.LinkPolicy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ShortenerCli {

    private static final String PROMPT = "> ";
    private static final System.Logger LOGGER = System.getLogger(ShortenerCli.class.getName());

    private final CommandRegistry registry;
    private final CliContext context;
    private final PrintStream out;

    public ShortenerCli(CommandRegistry registry, CliContext context) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.context = Objects.requireNonNull(context, "context");
        this.out = context.out();
    }

    public void run(BufferedReader in) throws IOException {
        printWelcome();
        while (true) {
            printNotifications();
            out.print(PROMPT);
            out.flush();
            String line = in.readLine();
            if (line == null) {
                out.println();
                return;
            }
            if (handle(line) == CommandResult.EXIT) {
                return;
            }
        }
    }

    public CommandResult handle(String line) {
        String trimmed = line.strip();
        if (trimmed.isEmpty()) {
            return CommandResult.CONTINUE;
        }
        List<String> tokens = List.of(trimmed.split("\\s+"));
        String head = tokens.getFirst();
        List<String> args = tokens.subList(1, tokens.size());
        try {
            Optional<Command> command = registry.find(head);
            if (command.isPresent()) {
                return command.get().execute(args, context);
            }
            if (args.isEmpty() && context.format().isShortLink(head)) {
                return registry.find(OpenCommand.NAME).orElseThrow().execute(tokens, context);
            }
            out.printf("Неизвестная команда '%s'. Введите help, чтобы увидеть список команд%n", head);
        } catch (CommandException | ShortenerException e) {
            out.println("Ошибка: " + e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.log(System.Logger.Level.ERROR, "Command failed: " + trimmed, e);
            out.println("Непредвиденная ошибка: " + e.getMessage());
        }
        return CommandResult.CONTINUE;
    }

    private void printWelcome() {
        LinkPolicy policy = context.links().policy();
        out.println("Сервис коротких ссылок. Введите help, чтобы увидеть список команд, exit для выхода.");
        out.printf("Срок жизни ссылки: %s, лимит переходов по умолчанию: %d (максимум %d).%n",
                LinkView.humanDuration(policy.ttl()),
                policy.defaultClickLimit(),
                policy.maxClickLimit());
    }

    private void printNotifications() {
        context.session().currentUser()
                .map(context.inbox()::drain)
                .orElse(List.of())
                .forEach(notification -> out.println("[уведомление] " + context.view().notification(notification)));
    }
}
