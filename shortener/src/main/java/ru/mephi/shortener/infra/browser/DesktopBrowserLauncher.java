package ru.mephi.shortener.infra.browser;

import ru.mephi.shortener.cli.BrowserLauncher;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URI;

public final class DesktopBrowserLauncher implements BrowserLauncher {

    private static final System.Logger LOGGER = System.getLogger(DesktopBrowserLauncher.class.getName());

    @Override
    public boolean open(URI uri) {
        if (GraphicsEnvironment.isHeadless() || !Desktop.isDesktopSupported()) {
            return false;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            return false;
        }
        try {
            desktop.browse(uri);
            return true;
        } catch (IOException | SecurityException | UnsupportedOperationException e) {
            LOGGER.log(System.Logger.Level.DEBUG, "Browser launch failed for " + uri, e);
            return false;
        }
    }
}
