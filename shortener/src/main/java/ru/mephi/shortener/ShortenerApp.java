package ru.mephi.shortener;

import ru.mephi.shortener.cli.BrowserLauncher;
import ru.mephi.shortener.cli.CliContext;
import ru.mephi.shortener.cli.CommandRegistry;
import ru.mephi.shortener.cli.LinkView;
import ru.mephi.shortener.cli.Session;
import ru.mephi.shortener.cli.ShortenerCli;
import ru.mephi.shortener.config.AppConfig;
import ru.mephi.shortener.config.ConfigLoader;
import ru.mephi.shortener.config.ConfigurationException;
import ru.mephi.shortener.infra.browser.DesktopBrowserLauncher;
import ru.mephi.shortener.infra.notification.InMemoryNotificationInbox;
import ru.mephi.shortener.infra.storage.FileLinkRepository;
import ru.mephi.shortener.infra.storage.InMemoryLinkRepository;
import ru.mephi.shortener.infra.storage.StorageException;
import ru.mephi.shortener.service.ExpirationScheduler;
import ru.mephi.shortener.service.LinkPolicy;
import ru.mephi.shortener.service.LinkService;
import ru.mephi.shortener.service.RandomCodeGenerator;
import ru.mephi.shortener.service.ShortLinkFormat;
import ru.mephi.shortener.service.UrlValidator;
import ru.mephi.shortener.service.port.LinkRepository;
import ru.mephi.shortener.service.port.NotificationInbox;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public final class ShortenerApp {

    private static final String CONFIG_PROPERTY = "shortener.config";
    private static final String CONFIG_OPTION = "--config";

    private static final int EXIT_OK = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_USAGE = 2;

    private ShortenerApp() {
    }

    public static void main(String[] args) {
        int exitCode = run(args, System.out, System.err);
        if (exitCode != EXIT_OK) {
            System.exit(exitCode);
        }
    }

    private static int run(String[] args, PrintStream out, PrintStream err) {
        try {
            AppConfig config = ConfigLoader.load(configPath(args), System.getProperties());
            return start(config, createRepository(config), out);
        } catch (ConfigurationException e) {
            err.println(e.getMessage());
            err.println("Использование: shortener [--config <путь к файлу .properties>]");
            return EXIT_USAGE;
        } catch (StorageException e) {
            err.println(e.getMessage() + ": " + e.getCause().getMessage());
            return EXIT_FAILURE;
        } catch (IOException e) {
            err.println("Ошибка чтения ввода: " + e.getMessage());
            return EXIT_FAILURE;
        }
    }

    private static int start(AppConfig config, LinkRepository repository, PrintStream out) throws IOException {
        Components components = assemble(config, repository, new DesktopBrowserLauncher(),
                Clock.systemDefaultZone(), UUID::randomUUID, out);
        try (ExpirationScheduler scheduler = new ExpirationScheduler(components.links(), config.cleanupInterval())) {
            scheduler.start();
            components.cli().run(new BufferedReader(new InputStreamReader(System.in, inputCharset())));
            return EXIT_OK;
        }
    }

    private static LinkRepository createRepository(AppConfig config) {
        return config.storageFile()
                .<LinkRepository>map(FileLinkRepository::new)
                .orElseGet(InMemoryLinkRepository::new);
    }

    public static Components assemble(
            AppConfig config,
            LinkRepository repository,
            BrowserLauncher browser,
            Clock clock,
            Supplier<UUID> idGenerator,
            PrintStream out) {
        NotificationInbox inbox = new InMemoryNotificationInbox();
        ShortLinkFormat format = new ShortLinkFormat(config.domain());
        LinkService links = new LinkService(
                repository,
                new RandomCodeGenerator(config.codeLength()),
                new UrlValidator(config.maxUrlLength(), config.domain()),
                inbox,
                new LinkPolicy(config.linkTtl(), config.defaultClickLimit(), config.maxClickLimit()),
                clock);
        CliContext context = new CliContext(
                links, new Session(idGenerator), inbox, browser, format, new LinkView(format, clock), out);
        return new Components(links, new ShortenerCli(CommandRegistry.standard(), context));
    }

    public static Optional<Path> configPath(String[] args) {
        if (args.length == 0) {
            return Optional.ofNullable(System.getProperty(CONFIG_PROPERTY)).map(ShortenerApp::toPath);
        }
        if (args.length == 2 && CONFIG_OPTION.equals(args[0])) {
            return Optional.of(toPath(args[1]));
        }
        if (args.length == 1 && args[0].startsWith(CONFIG_OPTION + "=")) {
            return Optional.of(toPath(args[0].substring(CONFIG_OPTION.length() + 1)));
        }
        throw new ConfigurationException("Неизвестные аргументы: " + String.join(" ", args));
    }

    private static Path toPath(String value) {
        try {
            return Path.of(value);
        } catch (InvalidPathException e) {
            throw new ConfigurationException("Некорректный путь к файлу конфигурации: " + value, e);
        }
    }

    private static Charset inputCharset() {
        Console console = System.console();
        return console != null ? console.charset() : StandardCharsets.UTF_8;
    }

    public record Components(LinkService links, ShortenerCli cli) {
    }
}
