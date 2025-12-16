package io.github.onu_eccs1621_sp2025.westward.game;

import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Contains the items that the pioneers have
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @version 1.0
 * @since 1.0.0 Alpha 1
 */
public class Inventory {
    private final List<ItemStack> items = new ArrayList<>();

    /**
     * Get all the items in the inventory.
     * @return List of ItemStacks in the Inventory
     */
    public List<ItemStack> getItems() {
        return this.items;
    }

    /**
     * Adds an ItemStack to the Inventory
     * @param itemStack ItemStack to add
     */
    public void addItemStack(ItemStack itemStack) {
        containsItem(itemStack).ifPresentOrElse(
                (originalStack) -> originalStack.mergeItemStacks(itemStack),
                () -> this.items.add(itemStack)
        );
    }

    /**
     * Checks what item the stack contains
     * @param stack What stack you are checking
     * @return Returns the name of the stack
     */
    public Optional<ItemStack> containsItem(ItemStack stack) {
        return containsItem(stack.getId());
    }

    /**
     * Finds if the inventory already contains the specified item
     * @param item Item's id
     * @return If the item exists
     */
    public Optional<ItemStack> containsItem(String item) {
        for (ItemStack stack : this.items) {
            if (stack.getId().equals(item)) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the number of items in the Inventory
     * @return Number of items in the Inventory
     */
    public int getItemCount() {
        int size = 0;
        for (ItemStack stack : this.items) {
            size += stack.getCount();
        }
        return size;
    }

    /**
     * Removes the specified ItemStack (including amount) from the Inventory
     * @param itemStack The ItemStack (including amount) to remove
     * @return True if success and False if the ItemStack cannot be removed
     */
    public boolean removeItemStack(ItemStack itemStack) {
        // Use references instead of Atomics
        // This is required due to the ifPresentOrElse statement
        var ref = new Object() {
            boolean flag;
        };
        containsItem(itemStack).ifPresentOrElse(
                (originalStack) -> {
                    if (originalStack.canConsume(itemStack.getCount())) {
                        if (originalStack.consume(itemStack.getCount())) {
                            this.items.remove(originalStack);
                        }
                        ref.flag = true;
                    } else {
                        ref.flag = false;
                    }
                },
                () -> ref.flag = false
        );
        return ref.flag;
    }

    /**
     * Removes a random amount from the specified type
     * @param type Type of item
     * @param meanConsumption Average consumption
     * @param stdDev Standard deviation of the consumption
     * @return If the consumption was successful
     */
    public boolean removeOfType(ItemStack.ItemType type, double meanConsumption, double stdDev) {
        int countOfType = countOfType(type);
        if (countOfType == 0) {
            return false;
        }

        List<ItemStack> ofType = this.items.stream()
                .filter(itemStack -> itemStack.getType() == type)
                .toList();
        if (ofType.isEmpty()) {
            return false;
        }
        short amountToConsume;
        if (stdDev == 0) {
            amountToConsume = (short) Math.round(meanConsumption);
        } else {
            amountToConsume = (short) Math.round(ThreadLocalRandom.current().nextGaussian(meanConsumption, stdDev));
        }
        if (amountToConsume > countOfType) {
            amountToConsume = (short) countOfType;
        }

        short amountConsumed = 0;
        while (amountConsumed < amountToConsume) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, ofType.size());
            if (ofType.get(randomIndex).canConsume((short) 1)) {
                amountConsumed++;
                if (ofType.get(randomIndex).consume((short) 1)) {
                    this.items.remove(ofType.get(randomIndex));
                }
            }
        }
        return true;
    }

    /**
     * Removes a number of items of a specific type from the Inventory
     * @param type The type of item to remove
     * @param amount The amount of that type to remove
     * @return True if the consumption was successful. False if there weren't enough items
     */
    public boolean removeOfType(ItemStack.ItemType type, int amount) {
        int countOfType = countOfType(type);
        if (countOfType == 0) {
            return false;
        }

        List<ItemStack> ofType = this.items.stream()
                .filter(itemStack -> itemStack.getType() == type)
                .toList();
        if (ofType.isEmpty()) {
            return false;
        }

        short amountConsumed = 0;
        while (amountConsumed < amount) {
            int randomIndex = ThreadLocalRandom.current().nextInt(0, ofType.size());
            if (ofType.get(randomIndex).canConsume((short) 1)) {
                amountConsumed++;
                if (ofType.get(randomIndex).consume((short) 1)) {
                    this.items.remove(ofType.get(randomIndex));
                }
            }
        }
        return true;
    }

    /**
     * Gets the amount of a specific item there are
     * @param type The item type to search for
     * @return The amount of that type in the inventory
     */
    public int countOfType(ItemStack.ItemType type) {
        return this.items.stream()
                .filter(itemStack -> itemStack.getType() == type)
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    /**
     * Gets a List of all ItemStacks that fall in the type
     * @param type Type of ItemStack to search for
     * @return List of ItemStacks with type
     */
    public List<ItemStack> getOfType(ItemStack.ItemType type) {
        return this.items.stream()
                .filter(itemStack -> itemStack.getType() == type)
                .toList();
    }

    /**
     * Gets the amount of an ItemStack are present
     * @param item Item name to query
     * @return The amount of items of that type in the Inventory
     */
    public short itemStackCount(String item) {
        // Use references instead of Atomics
        // This is required due to the ifPresentOrElse statement
        var ref = new Object() {
            short amount;
        };
        containsItem(item).ifPresentOrElse(
                (originalStack) -> ref.amount = originalStack.getCount(),
                () -> ref.amount = 0
        );
        return ref.amount;
    }

    /**
     * Gets random items and puts them into an Inventory based on the current difficulty
     * @param difficulty Game difficulty
     * @return Randomized Inventory
     */
    public static Inventory generateRandom(short difficulty) {
        ItemStack[] items = (ItemStack[]) Registry.getAssets(Registry.AssetType.ITEM);
        Inventory inventory = new Inventory();
        for (ItemStack stack : items) {
            if (difficulty != 1 && ThreadLocalRandom.current().nextBoolean()) {
                continue;
            }
            switch (stack.getType()) {
                case FOOD, MEDICINE ->
                        inventory.addItemStack(new ItemStack(stack.getId(), (short) ThreadLocalRandom.current().nextInt(1, 10), stack.getType(), stack.getBarterValue()));
                case FOOD_INGREDIENT, SUPPLIES ->
                        inventory.addItemStack(new ItemStack(stack.getId(), (short) ThreadLocalRandom.current().nextInt(1, 20), stack.getType(), stack.getBarterValue()));
                case WEAPON, TOOL ->
                        inventory.addItemStack(new ItemStack(stack.getId(), (short) ThreadLocalRandom.current().nextInt(1, 4), stack.getType(), stack.getBarterValue()));
                case AMMUNITION ->
                        inventory.addItemStack(new ItemStack(stack.getId(), (short) ThreadLocalRandom.current().nextInt(1, 100), stack.getType(), stack.getBarterValue()));
                case WAGON_PARTS -> {
                    // 25% chance
                    if (ThreadLocalRandom.current().nextInt(0, 3) == 3) {
                        inventory.addItemStack(new ItemStack(stack.getId(), (short) 1, stack.getType(), stack.getBarterValue()));
                    }
                }
                case CLOTHES -> inventory.addItemStack(new ItemStack(stack.getId(), (short) ThreadLocalRandom.current().nextInt(1, 8), stack.getType(), stack.getBarterValue()));
            }
        }
        return inventory;
    }

    /**
     * Picks random items and removes them from the inventory.
     * @param amount The amount of items to remove from the inventory.
     */
    public void removeRandomItems(int amount) {
        for (int i = 0; i < amount; i++) {
            ItemStack stack = this.items.get(ThreadLocalRandom.current().nextInt(this.items.size()));
            ItemStack clone = stack.shallowClone();
            clone.setCount((short) 1);
            this.removeItemStack(clone);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ItemStack stack : this.items) {
            sb.append(stack).append("\n");
        }
        return sb.toString();
    }
}
