package io.github.onu_eccs1621_sp2025.westward.data.loot_table;

import io.github.onu_eccs1621_sp2025.westward.game.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a loot table, which is a collection of loot table entries.
 * Each entry in the loot table has an item, a weight, a count range, and a boolean indicating whether the item should always drop.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @version 1.0
 * @since 1.0.0 Alpha 1
 * @param entries A list of loot table entries
 * @param rolls The number of times the loot table should be rolled/executed.
 */
public record LootTable(int rolls, List<LootTableEntry> entries) {
    public List<?> run() {
        List<?> items = new ArrayList<>();
        for (int i = 0; i < this.rolls; i++) {
            for (LootTableEntry entry : this.entries) {
                Optional<?> roll = entry.roll();
                if (roll.isPresent()) {
                    Object object = roll.get();
                    if (object instanceof ItemStack item) {
                        mergeItems((List<ItemStack>) items, item);
                    }
                }
            }
        }
        return items;
    }

    // Merges the items into the list if they are the same. If not, it adds the item to the list.
    private void mergeItems(List<ItemStack> items, ItemStack merge) {
        for (ItemStack item : items) {
            if (item.equals(merge)) {
                item.mergeItemStacks(merge);
                return;
            }
        }
        items.add(merge);
    }
}
