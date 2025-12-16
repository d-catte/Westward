package io.github.onu_eccs1621_sp2025.westward.data;

import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.game.ItemStack;

/**
 * The items for sale in a shop
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.2
 * @param itemId The id of the item
 * @param itemType The type of item
 * @param price The price of the item
 */
public record ShopListing(String itemId, ItemStack.ItemType itemType, float price) {
    /**
     * Purchases the ItemStack being sold
     * @param amount The amount of the item that should be purchased
     */
    public void purchase(short amount) {
        Game.getInstance().modifyMoney(-price * amount);
        ItemStack purchased = this.getItem(amount);
        Game.getInstance().getInventory().addItemStack(purchased);
    }

    private ItemStack getItem(short amount) {
        return new ItemStack(this.itemId, this.itemType, amount);
    }
}
