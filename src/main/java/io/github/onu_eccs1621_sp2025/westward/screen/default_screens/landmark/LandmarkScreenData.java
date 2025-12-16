package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.landmark;

/**
 * Screen data for the Landmark Screen
 * @author Dylan Catte
 * @since 1.0.0 Alpha 2
 * @version 1.0
 * @param imagePath The path to the image
 * @param translationKey The translation key for the description text
 * @param learnMoreUrl The url for the Learn More button. Null if none is present
 * @param id The screen ID for the screen
 */
public record LandmarkScreenData(String imagePath, String translationKey, String learnMoreUrl, String id) {
}
