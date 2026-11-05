package ru.mephi.shortener.cli;

import java.net.URI;

@FunctionalInterface
public interface BrowserLauncher {

    boolean open(URI uri);
}
