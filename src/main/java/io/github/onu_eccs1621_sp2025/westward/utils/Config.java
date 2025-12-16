package io.github.onu_eccs1621_sp2025.westward.utils;

import com.google.gson.reflect.TypeToken;
import io.github.onu_eccs1621_sp2025.westward.TrailApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * The configuration for the game.<p>
 * Contains information that only has one instance per game session.<p>
 * These options typically alter the entire game.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.1
 */
public class Config {
    private String configVer;
    private float startingMoney;
    private int teamMemberCount;
    private int diffOneStartingMiles;
    private int diffTwoStartingMiles;
    private int diffThreeStartingMiles;
    private int totalMiles;
    private int musicVolume;
    private int sfxVolume;
    private int fpsLimit;
    private Map<String, String> languagesEnabled;
    private String language;
    private String[] defaultFemaleNames;
    private String[] defaultMaleNames;

    /**
     * The current config version
     */
    private static final String CURRENT_CONFIG_VER = "0.1";
    /**
     * The global instance of the config
     */
    private static final Config INSTANCE = loadConfig();

    /**
     * Gets an instance of the Config after it is parsed.
     * @return An instance of the Config that can be read from
     */
    public static Config getConfig() {
       return INSTANCE;
    }

    private static Config loadConfig() {
        try (BufferedReader reader = Files.newBufferedReader(TrailApplication.getDataPaths().configPath())) {
            return TrailApplication.getGsonInstance().fromJson(reader, Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the config file
     */
    public void saveConfig() {
        try (BufferedWriter writer = Files.newBufferedWriter(TrailApplication.getDataPaths().configPath())) {
            TrailApplication.getGsonInstance().toJson(
                    this,
                    new TypeToken<Config>(){}.getType(),
                    writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Overwrites removed values and writes new values to the config
     */
    public void updateConfig() {
        if (!CURRENT_CONFIG_VER.equals(this.configVer)) {
            this.configVer = CURRENT_CONFIG_VER;
            saveConfig();
        }
    }

    /**
     * Gets the miles of the trail before the game starts
     * @param difficulty Difficulty of the game
     * @return The miles from pregame
     */
    public int getPreviousMiles(final short difficulty) {
        return switch (difficulty) {
            case 1 -> diffOneStartingMiles;
            case 2 -> diffTwoStartingMiles;
            case 3 -> diffThreeStartingMiles;
            default -> 0;
        };
    }

    /**
     * The maximum amount of starting money
     * @return The max starting money
     */
    public float getStartingMoney() {
        return startingMoney;
    }

    /**
     * Sets the maximum amount of starting money
     * @param startingMoney New max starting money
     */
    public void setStartingMoney(float startingMoney) {
        this.startingMoney = startingMoney;
    }

    /**
     * The maximum amount of members per game
     * @return Maximum amount of members
     */
    public int getTeamMemberCount() {
        return teamMemberCount;
    }

    /**
     * Sets the max number of members per group
     * @param teamMemberCount New max member count
     */
    public void setTeamMemberCount(int teamMemberCount) {
        this.teamMemberCount = teamMemberCount;
    }

    /**
     * The length of the trail (not including the starting point)
     * @return Length of the trail
     */
    public int getTotalMiles() {
        return totalMiles;
    }

    /**
     * The volume of the music
     * @return Volume of music
     */
    public int getMusicVolume() {
        return musicVolume;
    }

    /**
     * Sets the volume of the music
     * @param musicVolume New volume
     */
    public void setMusicVolume(int musicVolume) {
        this.musicVolume = musicVolume;
    }

    /**
     * Gets the sound effect volume
     * @return Sound effect volume
     */
    public int getSfxVolume() {
        return sfxVolume;
    }

    /**
     * Sets the sound effect volume
     * @param sfxVolume New volume
     */
    public void setSfxVolume(int sfxVolume) {
        this.sfxVolume = sfxVolume;
    }

    /**
     * Gets the FPS limiter value
     * @return FPS limit
     */
    public int getFpsLimit() {
        return fpsLimit;
    }

    /**
     * Sets the default FPS limit value
     * @param fpsLimit New FPS limit value
     */
    public void setFpsLimit(int fpsLimit) {
        this.fpsLimit = fpsLimit;
    }

    /**
     * Gets the current language selected
     * @return Current language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the current language
     * @param language New language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Gets the default male names for members
     * @return Default male names
     */
    public String[] getDefaultMaleNames() {
        return defaultMaleNames;
    }

    /**
     * Gets the default female names for members
     * @return Default female names
     */
    public String[] getDefaultFemaleNames() {
        return defaultFemaleNames;
    }

    /**
     * Gets a map of all languages and their values
     * @return Map of all languages and their translations
     */
    public Map<String, String> getLanguagesEnabled() {
        return languagesEnabled;
    }
}
