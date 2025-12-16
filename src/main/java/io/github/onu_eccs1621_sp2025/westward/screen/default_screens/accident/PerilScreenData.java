package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.accident;

/**
 * Basic screen data to show an image and translatable text for perils
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.1
 * @param imagePath The path to the image
 * @param translationKey The translation key for the description text
 * @param id The screen ID for the screen
 */
public record PerilScreenData(String imagePath, String translationKey, String id) {
}
