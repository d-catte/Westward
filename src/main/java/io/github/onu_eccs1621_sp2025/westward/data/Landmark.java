package io.github.onu_eccs1621_sp2025.westward.data;

/**
 * Contains landmark data
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.2
 * @param name Landmark's name
 * @param screenId The screen identifier to open
 * @param shopScreenId The screen identifier to open the shop
 * @param spritePath Path to the sprite image file
 * @param mile The mile marker for the identifier
 * @param hasRiver If a river can be crossed on the way to this landmark
 */
public record Landmark(String name, String screenId, String shopScreenId, String spritePath, int mile, boolean hasRiver) implements Comparable<Landmark> {
    @Override
    public int compareTo(Landmark otherLandmark) {
        // Sort from smallest to largest
        return Integer.compare(this.mile, otherLandmark.mile);
    }

    /**
     * If this landmark has a shop
     * @return If the landmark contains a shop
     */
    public boolean hasShop() {
        return this.shopScreenId != null;
    }
}
