package io.github.onu_eccs1621_sp2025.westward.data;

import com.google.gson.annotations.SerializedName;
import io.github.onu_eccs1621_sp2025.westward.utils.WeatherHelper;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

/**
 * Stores the data for the current Date
 * @author Dylan Catte
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public class Date {
    private Month month;
    private int day;

    /**
     * Stores the data for the current Date
     * @param month The current month of travelling
     * @param day The current day in the month
     * @param totalDays The amount of days on the before-game trails
     */
    public Date(Month month, int day, int totalDays) {
        this.month = month;
        this.day = day;
        this.addDays(totalDays);
    }

    /**
     * Increases the day by one and adjusts months accordingly
     */
    public void incrementDay() {
        this.day++;
        if (this.day > this.month.maxDaysInMonth()) {
            this.day = 1;
            this.month = this.month.getNext();
        }
        // Calculates new weather
        WeatherHelper.forecastTemperature(this.month);
    }

    /**
     * Gets the current month
     * @return Current month
     */
    public Month getMonth() {
        return this.month;
    }

    /**
     * Gets the current day in the month
     * @return Current day in the month
     */
    public int getDay() {
        return this.day;
    }

    /**
     * Adds a specified number of days to the day counter
     */
    public void addDays(int days) {
        while (days > 0) {
            int maxDaysThisMonth = this.month.maxDaysInMonth();
            if (days > maxDaysThisMonth) {
                days -= maxDaysThisMonth;
                this.month = this.month.getNext();
            } else {
                this.day = days;
                days = 0;
            }
        }
    }

    /**
     * Formats the Date into the <b>Month, Day</b> format
     * @return Formatted Date
     */
    @Override
    public String toString() {
        String dayStr = String.valueOf(this.day);
        char firstChar = dayStr.charAt(0);
        if (firstChar == '1') {
            return this.month.toString() + " " + this.day + Translations.getTranslatedText("ordinalPrefix.teen");
        }
        char lastChar = dayStr.toCharArray()[dayStr.length() - 1];
        String ending = switch (lastChar) {
            case '1' -> Translations.getTranslatedText("ordinalPrefix.1");
            case '2' -> Translations.getTranslatedText("ordinalPrefix.2");
            case '3' -> Translations.getTranslatedText("ordinalPrefix.3");
            default -> Translations.getTranslatedText("ordinalPrefix.other");
        };
        return this.month.toString() + " " + this.day + ending;
    }

    /**
     * The Month of the year
     */
    public enum Month {
        @SerializedName("none")
        NONE(0, "month.none"),
        @SerializedName("jan")
        JAN(1, "month.jan"),
        @SerializedName("feb")
        FEB(2, "month.feb"),
        @SerializedName("mar")
        MAR(3, "month.mar"),
        @SerializedName("apr")
        APR(4, "month.apr"),
        @SerializedName("may")
        MAY(5, "month.may"),
        @SerializedName("jun")
        JUN(6, "month.jun"),
        @SerializedName("jul")
        JUL(7, "month.jul"),
        @SerializedName("aug")
        AUG(8, "month.aug"),
        @SerializedName("sep")
        SEP(9, "month.sep"),
        @SerializedName("oct")
        OCT(10, "month.oct"),
        @SerializedName("nov")
        NOV(11, "month.nov"),
        @SerializedName("dec")
        DEC(12, "month.dec");

        private final String translation;
        private final int index;
        Month(int index, String translation) {
            this.translation = translation;
            this.index = index;
        }

        /**
         * Gets an array of all the Months
         * @return Array of all Months
         */
        public static String[] getMonths() {
            Month[] monthsEnum = Month.values();
            String[] months = new String[12];
            for (int i = 0; i < months.length; i++) {
                months[i] = monthsEnum[i].toString();
            }
            return months;
        }

        /**
         * Gets the next month
         * @return Next month after current
         */
        public Month getNext() {
            if (this.index == 12) {
                return Month.JAN;
            }
            return Month.values()[this.index + 1];
        }

        /**
         * Gets the total number of days in the Month
         * @return Total number of days in the month
         */
        public int maxDaysInMonth() {
            return switch (this) {
                case JAN, MAR, MAY, JUL, AUG, OCT, DEC -> 31;
                case APR, JUN, SEP, NOV -> 30;
                case FEB -> 28;
                default -> 0;
            };
        }

        @Override
        public String toString() {
            return Translations.getTranslatedText(this.translation);
        }
    }
}
