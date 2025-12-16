package io.github.onu_eccs1621_sp2025.westward.data.loot_table;

import io.github.onu_eccs1621_sp2025.westward.data.StatusContainer;
import io.github.onu_eccs1621_sp2025.westward.game.ItemStack;
import io.github.onu_eccs1621_sp2025.westward.utils.math.IntegerRange;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * An entry in a loot table that describes the object and probability of obtaining different amounts of that object.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @version 1.1
 * @since 1.0.0 Alpha 1
 */
public class LootTableEntry {
    private final Registry.AssetType returnType;
    private final String identifier;
    private final float weight;
    private final IntegerRange count;
    private final boolean alwaysDrop;

    /**
     * Creates the entry.
     * @param returnType The class type of the loot object.
     * @param identifier The identifier for the object to be given.<p>
     *                   - For items, this is ItemStack.name.<p>
     *                   - For Statuses, this is StatusContainer.name.
     * @param weight The probability of obtaining the object
     * @param count <p>- For items, this is the range of the amount of the item to be given.<p>
     *              - For Statuses, this is the severity level.
     * @param alwaysDrop if the entry should always drop at least one item
     */
    public LootTableEntry(Registry.AssetType returnType, String identifier, float weight, IntegerRange count, boolean alwaysDrop) {
        this.returnType = returnType;
        this.identifier = identifier;
        this.weight = weight;
        this.count = count;
        this.alwaysDrop = alwaysDrop;
    }

    /**
     * Executes the LootTable to have it randomly pick an item (or no item)
     * @return Randomly selected item, or nothing (Optional.empty())
     */
    public Optional<?> roll() {
        if (this.returnType == Registry.AssetType.ITEM) {
            if (this.alwaysDrop) {
                return Optional.of(new ItemStack(this.identifier, (short) this.count.random()));
            } else {
                OptionalInt optionalCount = this.count.optionalRandom(this.weight);
                if (optionalCount.isPresent()) {
                    // TODO: Specify type of item to drop
                    return Optional.of(new ItemStack(this.identifier, ItemStack.ItemType.FOOD, (short) optionalCount.getAsInt()));
                } else {
                    return Optional.empty();
                }
            }
        } else if (this.returnType == Registry.AssetType.STATUS) {
            if (this.alwaysDrop) {
                return Optional.of(StatusContainer.getDefaultInstance(this.identifier).shallowClone());
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
