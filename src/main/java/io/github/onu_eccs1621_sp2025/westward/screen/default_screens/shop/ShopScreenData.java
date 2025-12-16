package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.shop;

import io.github.onu_eccs1621_sp2025.westward.data.ShopListing;

import java.util.List;

/**
 * The data for the ShopScreen class
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.1
 * @param items Items for sale
 * @param id The screen id for the shop screen
 */
public record ShopScreenData(List<ShopListing> items, String id) {
}
