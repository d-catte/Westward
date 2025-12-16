package io.github.onu_eccs1621_sp2025.westward.utils.text;

import com.google.gson.reflect.TypeToken;
import io.github.onu_eccs1621_sp2025.westward.TrailApplication;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.screen.Renderer;
import io.github.onu_eccs1621_sp2025.westward.utils.DebugLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * For changing text in the game.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @version 1.0
 * @since 1.0.0 Alpha 1
 */
public class Translations {
    /**
     * The translation map for all text
     */
    private static Map<String, String> translationMap;
    /**
     * The message displayed when no translation is available
     */
    private static final String UNKNOWN_TRANSLATION = "This translation is missing";

    /**
     * Gets the translation for the specified translation key
     * @param translationKey The key for specifying the translation
     * @return The translated text in the language chosen
     */
    public static String getTranslatedText(final String translationKey) {
        return translationMap.getOrDefault(translationKey, UNKNOWN_TRANSLATION);
    }

    /**
     * Gets the translation for the specified translation key
     * @param translationKey The key for specifying the translation
     * @param additionalData Data that will be filled in for %s patterns
     * @return The translated text in the language chosen
     */
    public static String getTranslatedText(final String translationKey, final String... additionalData) {
        String unformattedString = getTranslatedText(translationKey);
        int index = 0;
        while (unformattedString.contains("%s")) {
            if (index < additionalData.length) {
                unformattedString = unformattedString.replaceFirst("%s", additionalData[index]);
                index++;
            } else {
                break;
            }
        }
        return unformattedString;
    }

    /**
     * Loads the translations from the specified language file
     */
    public static void loadTranslations(final String lang) {
        final Path path = TrailApplication.getDataPaths().translationsPath().resolve(lang + ".json");
        try {
            translationMap = TrailApplication.getGsonInstance().fromJson(Files.newBufferedReader(path), new TypeToken<Map<String, String>>(){}.getType());
        } catch (IOException e) {
            DebugLogger.warn("Translation file not found: {}", lang + ".json");
        }

        // Reload translation caches
        Renderer.reloadTranslations();
        Game.reloadTranslationCaches();
    }
}
