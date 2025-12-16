package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.trading;

import java.util.List;

/**
 * The data for the TradingScreen class
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.0
 * @param id The screen id for the trading screen
 * @param imagePath The path to the image
 * @param translationKey The translation key for the screen's text
 * @param possibleItems List of items that could be offered for trade
 */
public record TradingScreenData(String id, String imagePath, String translationKey, List<TradingItem> possibleItems) {
}
