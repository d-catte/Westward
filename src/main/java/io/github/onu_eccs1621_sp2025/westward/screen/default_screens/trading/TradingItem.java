package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.trading;

/**
 * Items available to trade
 * @param id Corresponds to the ID of the ItemStack this item represents
 * @param chance The chance between 0 and 1 that this item is offered up for trade
 * @param barterValue The value of this item in the system of bartering
 */
public record TradingItem(String id, float chance, float barterValue) {
}
