package io.github.onu_eccs1621_sp2025.westward.data;

import io.github.onu_eccs1621_sp2025.westward.data.member.Member;
import io.github.onu_eccs1621_sp2025.westward.game.Inventory;
import io.github.onu_eccs1621_sp2025.westward.utils.Config;
import io.github.onu_eccs1621_sp2025.westward.utils.DebugLogger;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.MemberPlaqueData;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Creates data for a save file
 * @param saveName The name of the current game
 * @param members The members in the game
 * @param inventory The group's inventory
 * @param money The group's money
 * @param mile The current mile the group is at
 * @param difficulty The game's difficulty
 * @param date The game's current date
 * @param nextLandmarkIndex The index of the next landmark in the registry
 * @param canSleep If the members can sleep
 * @param dateSaved When the file was last saved
 * @author Dylan Catte
 * @since 1.0.0 Alpha 1
 * @version 1.1
 */
public record SaveData(
        String saveName,
        List<Member> members,
        Inventory inventory,
        float money,
        int mile,
        short difficulty,
        Date date,
        int nextLandmarkIndex,
        boolean canSleep,
        String dateSaved
) {

    /**
     * Builder class for creating SaveData
     */
    public static class SaveDataBuilder {
        private String saveName;
        private final List<Member> members = new ArrayList<>();
        private short difficulty = -1;
        private Inventory inventory;
        private float money = -1;
        private int day = -1;
        private Date.Month month = Date.Month.NONE;

        /**
         * Add a save name
         * @param saveName Name of the game file
         * @return Adds the save name to the data and returns it
         */
        public SaveDataBuilder saveName(String saveName) {
            this.saveName = saveName;
            return this;
        }

        /**
         * Adds a Member to the data
         * @param data Member data instance
         * @return Adds the member data to the data and returns it
         */
        public SaveDataBuilder addMember(MemberPlaqueData data) {
            this.members.add(data.getMember());
            return this;
        }

        /**
         * Adds a random Member to the data.<p>
         * Use {@link SaveDataBuilder#addMember(MemberPlaqueData)} to add a preconfigured Member
         * @return Adds the member data to the data and returns it
         */
        public SaveDataBuilder addMember() {
            this.members.add(Member.randomize());
            return this;
        }

        /**
         * Adds the difficulty to the data
         * @param difficulty Difficulty of the game
         * @return Adds the difficulty to the data and returns it
         */
        public SaveDataBuilder difficulty(short difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        /**
         * Adds money to the data
         * @param money Amount of money
         * @return Adds the money to the data and returns it
         */
        public SaveDataBuilder money(float money) {
            this.money = money;
            return this;
        }

        /**
         * Adds the Date to the data
         * @param day Day of the month
         * @param monthIndex Index of the month (Jan = 0; Dec = 11)
         * @return Adds the Date to the data and returns it
         */
        public SaveDataBuilder date(int day, int monthIndex) {
            this.month = Date.Month.values()[monthIndex];
            this.day = day;
            return this;
        }

        /**
         * Builds all the data contained in the Builder and returns the built SaveData
         * @return The built SaveData
         */
        public SaveData build() {
            if (this.saveName == null || this.members.isEmpty() || this.difficulty == -1) {
                DebugLogger.warn("Invalid Game Data");
                return null;
            }

            if (this.inventory == null) {
                this.inventory = Inventory.generateRandom(this.difficulty);
            }

            if (this.money == -1) {
                this.money = this.randomMoney();
            }

            if (this.month == Date.Month.NONE) {
                this.month = this.randomStartingMonth();
            }

            if (this.day == -1 || this.day > this.month.maxDaysInMonth() || this.day == 0) {
                this.day = this.randomStartingDay();
            }

            return new SaveData(this.saveName, this.members, this.inventory, this.money, 0, this.difficulty, new Date(this.month, this.day, this.randomTotalDays()), 0, false, getTimeNow());
        }

        private float randomMoney() {
            float startingMoney = Config.getConfig().getStartingMoney();
            return switch (this.difficulty) {
                case 1 -> startingMoney;
                case 2 -> Math.round(startingMoney * ThreadLocalRandom.current().nextFloat(0.5F, 1.0F));
                case 3 -> Math.min(startingMoney, Math.max(Math.round(startingMoney / 16F), Math.round(ThreadLocalRandom.current().nextGaussian(startingMoney / 2F, startingMoney / 4F))));
                default -> 0.0F; // Unobtainable
            };
        }

        private int randomTotalDays() {
            return Math.round(Config.getConfig().getPreviousMiles(this.difficulty) / ThreadLocalRandom.current().nextFloat(10, 20));
        }

        private int randomStartingDay() {
            return ThreadLocalRandom.current().nextInt(1, this.month.maxDaysInMonth() + 1);
        }

        private Date.Month randomStartingMonth() {
            return switch (this.difficulty) {
                case 1 -> Date.Month.APR;
                case 2 -> {
                    int index = (int) Math.max(1, Math.round(ThreadLocalRandom.current().nextGaussian(4, 1)) % 13);
                    yield Date.Month.values()[index];
                }
                case 3 -> {
                    int index = (int) Math.max(1, Math.round(ThreadLocalRandom.current().nextGaussian(4, 2.5)) % 13);
                    yield Date.Month.values()[index];
                }
                default -> Date.Month.NONE;
            };
        }
    }

    /**
     * Gets the current time
     * @return Formatted date/time from now
     */
    public static String getTimeNow() {
        java.util.Date currentDate = java.util.Date.from(Instant.now());
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy hh:mm a");
        return formatter.format(currentDate);
    }

    @Override
    public String toString() {
        return String.format("""
                Name: %s;
                Members: %s
                Money: %s
                Mile: %s
                Next Landmark Index: %s
                """, this.saveName, this.members.toString(), this.money, this.mile, this.nextLandmarkIndex);
    }
}
