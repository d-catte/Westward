package io.github.onu_eccs1621_sp2025.westward.game;

import com.google.gson.reflect.TypeToken;
import imgui.type.ImInt;
import io.github.onu_eccs1621_sp2025.westward.TrailApplication;
import io.github.onu_eccs1621_sp2025.westward.data.Date;
import io.github.onu_eccs1621_sp2025.westward.data.Landmark;
import io.github.onu_eccs1621_sp2025.westward.data.SaveData;
import io.github.onu_eccs1621_sp2025.westward.data.StatusContainer;
import io.github.onu_eccs1621_sp2025.westward.data.member.Member;
import io.github.onu_eccs1621_sp2025.westward.game.event.Event;
import io.github.onu_eccs1621_sp2025.westward.screen.Renderer;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.screen.default_screens.travel.TravelingScreen;
import io.github.onu_eccs1621_sp2025.westward.utils.Config;
import io.github.onu_eccs1621_sp2025.westward.utils.DebugLogger;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.sound.SoundEngine;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Contains the data for the game.
 * Handles all game actions such as ticking, data retrieval, and rendering.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @version 1.2
 * @since 1.0.0 Alpha 1
 */
public class Game {
    private final List<Member> members;
    private final Inventory inventory;
    private float money;
    private int currentMile;
    private final short difficulty;
    private final String saveName;
    private final Date date;
    private int nextLandmarkIndex;
    private int lastLandmarkIndex;
    private int nextLandmarkMileCache;
    private int lastLandmarkMileCache;
    private boolean lastLandmarkHasShop;
    private boolean canCrossRiver;
    private boolean waitingForUserInput;
    private boolean canSleep;
    private MiniGame currentMinigame = MiniGame.NONE;
    private final ImInt selectedPace = new ImInt(1);
    private String[] pacesTranslations = WagonPace.getTranslations();
    private static Game instance;
    private boolean stopFlag = false;

    /**
     * Creates the game from data
     * @param data SaveData instance
     */
    public Game(SaveData data) {
        // Quit main menu music
        SoundEngine.stopMusic();
        SoundEngine.invalidateCaches();
        DebugLogger.info("Loading Game");
        this.inventory = data.inventory();
        this.money = data.money();
        this.currentMile = data.mile();
        this.difficulty = data.difficulty();
        this.saveName = data.saveName();
        this.members = data.members();
        this.nextLandmarkIndex = data.nextLandmarkIndex();
        if (this.nextLandmarkIndex != 0) {
            Landmark next = Registry.getLandmarkAsset(this.nextLandmarkIndex);
            this.nextLandmarkMileCache = next.mile();
        }
        this.canSleep = data.canSleep();
        this.date = data.date();
        DebugLogger.info("Game Loaded Successfully");
    }

    /**
     * Gets the current game instance
     * @return Current Game instance
     */
    public static Game getInstance() {
        return instance;
    }

    /**
     * Sets the instance to a new instance
     * @param data The data to input into the new instance
     */
    public static void resetInstance(SaveData data) {
        instance = new Game(data);
        instance.waitingForUserInput = true;
    }

    /**
     * Sets the instance to null to end all running processes<p>
     * Use {@link Game#resetInstance(SaveData)} to start a new Game
     */
    public static void endInstance() {
        instance = null;
    }

    /**
     * Tells the game to immediately end its execution.
     * This is run if the game has been saved and needs to stop
     */
    public void markAsEnded() {
        this.stopFlag = true;
    }

    /**
     * Gets a list of every alive member in the game
     * @return List of alive members
     */
    public List<Member> getMembers() {
        return this.members;
    }

    /**
     * Gets if any Member can heal players
     * @return If any Member can heal players
     */
    public boolean hasHealers() {
        for (Member member : this.members) {
            if (member.getRole().canHealPlayers()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the current Inventory that contains all the members' items
     * @return All the members' items in an Inventory
     */
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Gets the members' money<p>
     * Use {@link Game#modifyMoney(float)} to modify the members' money by a specific amount.<p>
     * Use {@link Game#setMoney(float)} to set the members' money to a specific value
     * @return members current money
     */
    public float getMoney() {
        return this.money;
    }

    /**
     * Sets the members' money to a new value
     * @param value The value to set the money to
     */
    public void setMoney(float value) {
        this.money = value;
    }

    /**
     * Modifies the members' money by a specific amount
     * @param by The amount to modify it by. It can be positive (to add) or negative (to subtract)
     */
    public void modifyMoney(float by) {
        this.money += by;
        // Protect against the rare chance of -0
        this.money = Math.abs(this.money);
    }

    /**
     * Gets the current mile the members are on<p>
     * Use {@link Game#modifyCurrentMile(int)} to edit the current mile
     * @return Current mile of the trail (excluding the previous miles from pregame)
     */
    public int getCurrentMile() {
        return this.currentMile;
    }

    /**
     * Modifies the members' current mile by a specific amount
     * @param by The amount to add to the current mile. It can be negative to subtract miles
     */
    public void modifyCurrentMile(int by) {
        this.currentMile += by;
    }

    /**
     * Gets the current difficulty of the game
     * @return Current difficulty
     */
    public short getDifficulty() {
        return this.difficulty;
    }

    /**
     * Gets the current Date on the journey
     * @return Current Date (day, month, season)
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Gets the index of the next Landmark
     * @return Index of the next Landmark
     */
    public int getNextLandmarkIndex() {
        return this.nextLandmarkIndex;
    }

    /**
     * Gets the index of the last Landmark
     * @return Index of the last Landmark
     */
    public int getLastLandmarkIndex() {
        return this.lastLandmarkIndex;
    }

    /**
     * Gets the currently selected pace of travel
     * @return Current pace of travel
     */
    public ImInt getPace() {
        return this.selectedPace;
    }

    /**
     * Gets the translated text for each WagonPace
     * @return Translated WagonPace
     */
    public String[] getPacesTranslations() {
        return this.pacesTranslations;
    }

    /**
     * Gets a random Member
     * @return A random Member
     */
    public Member getRandomMember() {
        int i = ThreadLocalRandom.current().nextInt(0, this.members.size());
        return this.members.get(i);
    }

    // Runs code when the game is won
    private void onGameWon() {
        Renderer.RENDER_QUEUE.add("victory");
        // Remove files
        Registry.removeSaveData(this.saveName);
        this.pause();
        Game.endInstance();
        TrailApplication.returnToMainMenu(false);
    }

    // Runs code when the game is lost
    private void onGameLost() {
        Game.endInstance();
        // Remove files
        Registry.removeSaveData(this.saveName);
        Renderer.RENDER_QUEUE.add("loss");
        this.pause();
        TrailApplication.returnToMainMenu(false);
    }

    /**
     * If the game is waiting for the user to press a button (such as "continue").<p>
     * This does not fire for screens.<p>
     * Use {@link Game#setNotWaiting()} to set this value to false
     * @return If the game is waiting for a user input
     */
    public boolean isWaitingForUserInput() {
        return this.waitingForUserInput;
    }

    /**
     * Sets the game for not waiting
     */
    public void setNotWaiting() {
        this.waitingForUserInput = false;
    }

    // If the members are at a landmark
    private boolean isAtLandmark() {
        return this.lastLandmarkMileCache == this.currentMile;
    }

    /**
     * If the current location is a landmark and it has a civilization
     * @return If the landmark has a civilization
     */
    public boolean isAtCivilization() {
        return this.isAtLandmark() && this.lastLandmarkHasShop;
    }

    /**
     * @return if the pioneers can sleep
     */
    public boolean canSleep() {
        return this.canSleep;
    }

    /**
     * Increments by one day and sets sleep to false
     */
    public void sleep() {
        this.date.incrementDay();
        this.canSleep = false;
    }

    /**
     * If the current offered minigame exists
     * @return true if there is a minigame available
     */
    public boolean hasMinigame() {
        return this.currentMinigame != MiniGame.NONE;
    }

    /**
     * Disables the minigame after playing it
     */
    public void playMinigame() {
        if (this.currentMinigame == MiniGame.RIVER) {
            this.canCrossRiver = false;
        }
        this.currentMinigame = MiniGame.NONE;
    }

    /**
     * Ticks all the game's functions
     */
    public void tickGame() {
        if (this.nextLandmarkIndex == 0) {
            Renderer.RENDER_QUEUE.add("intro");
        }

        // Pause game until no screens are present
        while (!Renderer.RENDER_QUEUE.isEmpty() || this.waitingForUserInput) {
            if (this.stopFlag) {
                return;
            }
        }

        int milesTravelledToday = (int) Math.round(ThreadLocalRandom.current().nextGaussian(15 * WagonPace.getPace().getMileageMultiplier(), 2));
        int milesTravelledCopy = milesTravelledToday;

        // This is meant to be a recursive function
        while (milesTravelledToday > 0) {
            if (this.nextLandmarkIndex != 0) {
                this.currentMile++;
            }

            if (this.nextLandmarkMileCache <= this.currentMile) {
                Landmark landmark = Registry.getLandmarkAsset(this.nextLandmarkIndex);
                Screen screen = (Screen)Registry.getAsset(Registry.AssetType.SCREEN, landmark.screenId());
                Renderer.RENDER_QUEUE.add(screen.getId());
                this.getNextLandmark();
                if (this.currentMile < Config.getConfig().getTotalMiles()) {
                    break;
                }
            }

            // End of the game check
            if (this.currentMile >= Config.getConfig().getTotalMiles()) {
                this.pause();
                onGameWon();
                return;
            }

            // Tick events
            Event.selectRandomEvent();

            // Tick members every 5th mile
            // This also means members are more likely to get ticked when travelling faster
            if (milesTravelledToday % 5 == 0) {
                ListIterator<Member> iterator = this.members.listIterator();
                while (iterator.hasNext()) {
                    iterator.next().tick(iterator);
                }
            }

            // Check for game over
            if (this.members.isEmpty()) {
                if (this.stopFlag) {
                    return;
                } else {
                    this.markAsEnded();
                }
                this.pause();
                onGameLost();
                return;
            }
            milesTravelledToday--;

            // A delay between miles
            try {
                Thread.sleep((long) (500 / WagonPace.getPace().getMileageMultiplier()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            this.pause();
        }

        // Eat food
        StatusContainer starvingStatus = ((StatusContainer) Registry.getAsset(Registry.AssetType.STATUS, "hunger"));
        if (!this.inventory.removeOfType(ItemStack.ItemType.FOOD, this.members.size() * WagonPace.getPace().getFoodMultiplier(), 0.5)) {
            if (!this.members.getFirst().getStatuses().contains(starvingStatus)) {
                for (Member member : this.members) {
                    member.addStatus(starvingStatus.shallowClone());
                }
                Renderer.addConsequence("basicScreen.starve");
            } else if (this.date.getDay() % 2 == 0) {
                // Increment starving level every 2 days
                ListIterator<Member> iterator = this.members.listIterator();
                while (iterator.hasNext()) {
                    Member member = iterator.next();
                    member.addStatus(starvingStatus.shallowClone(), iterator);
                }
                Renderer.addConsequence("basicScreen.continueStarve");
            }
        } else if (members.getFirst().getStatuses().contains(starvingStatus)) {
            // Remove starving status when food is obtained again
            for (Member member : members) {
                member.getStatuses().remove(starvingStatus);
            }
        }

        if (this.currentMile != 0) {
            this.date.incrementDay();
            TravelingScreen travelScreen = (TravelingScreen) Registry.getAsset(Registry.AssetType.SCREEN, "travel");
            travelScreen.setData(milesTravelledCopy - milesTravelledToday);
            Renderer.RENDER_QUEUE.add(travelScreen.getId());
            this.canSleep = true;
            this.currentMinigame = MiniGame.getRandom(this.canCrossRiver);
        }

        // Force player feedback
        this.waitingForUserInput = true;

        tickGame();
    }

    private void getNextLandmark() {
        this.lastLandmarkIndex = this.nextLandmarkIndex;
        this.lastLandmarkMileCache = this.nextLandmarkMileCache;
        Landmark last = Registry.getLandmarkAsset(this.nextLandmarkIndex);
        this.lastLandmarkHasShop = last.hasShop();
        if (this.nextLandmarkIndex + 1 >= Registry.getLoadedAssetsCount(Registry.AssetType.LANDMARK)) {
            this.nextLandmarkMileCache = Config.getConfig().getTotalMiles();
            return;
        }

        Landmark next = Registry.getLandmarkAsset(++this.nextLandmarkIndex);
        this.nextLandmarkMileCache = next.mile();
        this.canCrossRiver = next.hasRiver();
    }

    private void pause() {
        // Pause game until no screens are present
        while (!Renderer.RENDER_QUEUE.isEmpty()) {
        }
    }

    /**
     * Gets the starting trail based on difficulty
     * @return Starting trail name
     */
    public String getStartingTrail() {
        return switch (this.difficulty) {
            case 1 -> Translations.getTranslatedText("menu.trailSelection.easyTrail");
            case 2 -> Translations.getTranslatedText("menu.trailSelection.medTrail");
            case 3 -> Translations.getTranslatedText("menu.trailSelection.hardTrail");
            default -> RenderUtils.EMPTY_STR;
        };
    }

    /**
     * Gets the percentage of the game that has been completed to the next landmark
     * @return Percentage of travel to next landmark
     */
    public float percentageToNextLandmark() {
        int progress = this.currentMile - this.lastLandmarkMileCache;
        int goal = this.nextLandmarkMileCache - this.lastLandmarkMileCache;
        return (float) progress / goal;
    }

    /**
     * Calculates the score of the game upon completion.<p>
     * It takes into consideration the members' health, roles, inventory, and the difficulty
     * @return The final score
     */
    public int calculateScore() {
        int score = 0;
        for (Member member : this.members) {
            int memberScore = 10 * member.getHealth();
            memberScore += member.getRole().bonusPoints() != null ? member.getRole().bonusPoints() : 0;
            score += memberScore;
        }
        score += (int) (10 * this.money);
        score += 10 * this.inventory.getItemCount();

        switch (this.difficulty) {
            case 2 -> score = (int) (score * 1.1);
            case 3 -> score = (int) (score * 1.3);
        }

        return score;
    }

    /**
     * Saves all data in the game to a file
     */
    public void saveGame() {
        SaveData save = new SaveData(
                this.saveName,
                this.members,
                this.inventory,
                this.money,
                this.currentMile,
                this.difficulty,
                this.date,
                this.nextLandmarkIndex,
                this.canSleep,
                SaveData.getTimeNow()
        );

        Path path = TrailApplication.getDataPaths().savesDirectoryPath().resolve(this.saveName + ".json");

        try (FileWriter writer = new FileWriter(path.toFile())) {
            Registry.getGsonInstance().toJson(save, new TypeToken<SaveData>(){}.getType(), writer);
        } catch (IOException e) {
            DebugLogger.warn("Failed to save game");
            throw new RuntimeException(e);
        }
        Registry.replaceSaveData(this.saveName);
        DebugLogger.info("Game saved successfully");
    }

    /**
     * Gets the current minigame
     * @return current minigame
     */
    public MiniGame getCurrentMinigame() {
        return this.currentMinigame;
    }

    /**
     * The games embedded in Westward.
     * New minigames can be easily added by adding another enum value
     */
    public enum MiniGame {
        /**
         * The hunting minigame
         */
        HUNTING("minigame.hunting"),
        /**
         * The river crossing minigame
         */
        RIVER("minigame.river"),
        /**
         * Debug value
         */
        NONE(RenderUtils.EMPTY_STR);

        private final String translationKey;
        MiniGame(String translationKey) {
            this.translationKey = translationKey;
        }

        /**
         * Gets the translation key for the minigame's name
         * @return Translation key for minigame
         */
        public String getTranslationKey() {
            return this.translationKey;
        }
        
        private static MiniGame getRandom(boolean canCrossRiver) {
            MiniGame random = MiniGame.values()[ThreadLocalRandom.current().nextInt(0, MiniGame.values().length)];
            return (random == MiniGame.RIVER && !canCrossRiver) ? MiniGame.NONE : random;
        }
    }

    /**
     * The speed that the pioneers are travelling
     */
    public enum WagonPace {
        /**
         * The slowest movement speed
         */
        LEISURELY(0.6F, 0.5F, "game.pace.leisurely"),
        /**
         * The default movement speed
         */
        NORMAL(1, 1, "game.pace.normal"),
        /**
         * The fastest movement speed
         */
        QUICK(1.4F, 1.8F, "game.pace.quick");

        private final float mileageMultiplier;
        private final float foodMultiplier;
        private final String translationKey;
        WagonPace(float mileageMultiplier, float foodMultiplier, String translationKey) {
            this.mileageMultiplier = mileageMultiplier;
            this.foodMultiplier = foodMultiplier;
            this.translationKey= translationKey;
        }
        
        private float getMileageMultiplier() {
            return this.mileageMultiplier;
        }

        private float getFoodMultiplier() {
            return this.foodMultiplier;
        }

        private String getTranslationKey() {
            return this.translationKey;
        }

        private static String[] getTranslations() {
            WagonPace[] paces = WagonPace.values();
            String[] translations = new String[paces.length];
            for (int i = 0; i < paces.length; i++) {
                translations[i] = Translations.getTranslatedText(paces[i].getTranslationKey());
            }
            return translations;
        }

        private static WagonPace getPace() {
            return WagonPace.values()[Game.getInstance().selectedPace.get()];
        }
    }

    /**
     * Reloads the cached translated text
     */
    public static void reloadTranslationCaches() {
        if (Game.getInstance() != null) {
            Game.getInstance().pacesTranslations = WagonPace.getTranslations();
        }
    }
}
