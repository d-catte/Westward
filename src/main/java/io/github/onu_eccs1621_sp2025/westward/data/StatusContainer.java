package io.github.onu_eccs1621_sp2025.westward.data;

import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.utils.ShallowClone;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Contains data for statuses (change of intensity)
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @version 1.2
 * @since 1.0.0 Alpha 1
 */
public class StatusContainer implements ShallowClone<StatusContainer> {
    private final String name;
    private final String obtainedTranslationKey;
    private final boolean healable;
    private byte level;
    private final byte maxLevel;
    private final float baseChance;

    /**
     * For internal use only.
     * Allows hardcoding status effects
     * @param name Name of the Status
     * @param level Current level of the status
     * @param maxLevel The max level of the status
     * @param obtainedTranslationKey The translation key to get the translation when the status is obtained
     * @param healable If the status level can be decreased from healing
     * @param baseChance The chance of inflicting damage on a player
     */
    public StatusContainer(String name, byte level, byte maxLevel, String obtainedTranslationKey, boolean healable, float baseChance) {
        this.name = name;
        this.obtainedTranslationKey = obtainedTranslationKey;
        this.level = level;
        this.maxLevel = maxLevel;
        this.baseChance = baseChance;
        this.healable = healable;
    }

    public static StatusContainer getDefaultInstance(String statusName) {
        return ((StatusContainer) Registry.getAsset(Registry.AssetType.STATUS, statusName));
    }

    /**
     * Gets the level of the Status Container.<p>
     * Use {@link StatusContainer#increaseLevel()} to increase the level by 1.<p>
     * Use {@link StatusContainer#decreaseLevel()} to decrease the level by 1.
     * @return The level of the Status
     */
    public byte getLevel() {
        return this.level;
    }

    /**
     * Gets the Status's name
     * @return The Status's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the max level possible for the status
     * @return The max level
     */
    public byte getMaxLevel() {
        return getDefaultInstance(this.name).maxLevel;
    }

    /**
     * Gets the translation key for the status when obtained
     * @return The translation key
     */
    public String getObtainedTranslation() {
        return this.obtainedTranslationKey == null ? "notification.addStatus" : this.obtainedTranslationKey;
    }

    /**
     * If the status can be healed (level decreased)
     * @return if the status can be healed
     */
    public boolean isHealable() {
        return getDefaultInstance(this.name).healable;
    }

    /**
     * Increases the severity level.
     * @return Returns true if the highest level is reached.
     *         This indicates that a member should be killed.
     */
    public boolean increaseLevel() {
        level++;
        return level >= maxLevel;
    }

    /**
     * Decreases the severity level
     * @return Returns false if the severity level reaches 0.
     *         This indicates that the status should be removed.
     */
    public boolean decreaseLevel() {
        if (level <= 1) {
            return false;
        }
        level--;
        return true;
    }

    /**
     * Rolls the chance of obtaining the status.<p>
     * Rolls as many times as the current level of the status
     * @return If the status should affect the member
     */
    public boolean chance() {
        for (int i = 0; i < this.level; i++) {
            if (ThreadLocalRandom.current().nextFloat() < (getDefaultInstance(this.name).baseChance * Game.getInstance().getDifficulty())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the name of the current status id the object is an instance of StatusContainer
     * @param obj An object being checked for if it's an instance of the StatusContainer
     * @return Returns name of the Status
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatusContainer container) {
            return this.name.equals(container.name);
        }
        return false;
    }

    /**
     * Gets a clone of the StatusContainer with a level of 1
     * @return Clone of StatusContainer
     */
    @Override
    public StatusContainer shallowClone() {
        return new StatusContainer(this.name, (byte) 1, this.maxLevel, this.obtainedTranslationKey, this.healable, this.baseChance);
    }

    @Override
    public String toString() {
        return Translations.getTranslatedText("status.string", this.name, String.valueOf(this.level));
    }
}
