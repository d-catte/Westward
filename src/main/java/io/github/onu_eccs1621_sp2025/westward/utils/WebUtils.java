package io.github.onu_eccs1621_sp2025.westward.utils;

import java.io.IOException;

/**
 * Utilities for interfacing with the browser
 * @author Dylan Catte
 * @since 1.0.0 Alpha 2
 * @version 1.1
 */
public final class WebUtils {
    /**
     * Opens the default web browser with the specific URL
     * @param url URL to the webpage
     */
    public static void openWebPage(final String url) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            } else if (os.contains("mac") || os.contains("osx")) {
                new ProcessBuilder("open", url).start();
            } else if (os.contains("nix") || os.contains("nux")) {
                new ProcessBuilder("xdg-open", url).start();
            } else {
                DebugLogger.error("Failed to open webpage: Unsupported OS: " + os);
            }
        } catch (IOException e) {
            DebugLogger.error("Failed to open webpage: ", e);
        }
    }
}
