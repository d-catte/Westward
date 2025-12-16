package io.github.onu_eccs1621_sp2025.westward.game.event;

import com.google.gson.annotations.SerializedName;
import io.github.onu_eccs1621_sp2025.westward.TrailApplication;
import io.github.onu_eccs1621_sp2025.westward.data.StatusContainer;
import io.github.onu_eccs1621_sp2025.westward.data.member.Member;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.game.ItemStack;
import io.github.onu_eccs1621_sp2025.westward.utils.DebugLogger;
import io.github.onu_eccs1621_sp2025.westward.utils.WeatherHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Constraints to test for when executing an Event
 * @author Dylan Catte
 * @author Ben Westover
 * @since 1.0.0 Alpha 1
 * @version 1.1
 */
public class EventConstraint {
    private Argument argument;
    private ComplexArgument complexArgument;
    private final CompareOperator compareOperator;
    private Integer intValue;
    private final String stringValue;
    private final Equation equation;

    // Fake constructor to allow for null values
    private EventConstraint() {
        this.stringValue = null;
        this.equation = null;
        this.compareOperator = null;
    }

    /**
     * Compares two values
     */
    public enum CompareOperator {
        @SerializedName("==")
        EQUALS,
        @SerializedName("!=")
        NOT_EQUALS,
        @SerializedName(">")
        GREATER_THAN,
        @SerializedName("<")
        LESS_THAN,
        @SerializedName(">=")
        GREATER_THAN_OR_EQUAL_TO,
        @SerializedName("<=")
        LESS_THAN_OR_EQUAL_TO,
        @SerializedName(":")
        CONTAINS,
        @SerializedName("!:")
        NOT_CONTAINS
    }

    /**
     * The type of EventConstraint
     */
    public enum Argument {
        @SerializedName("currentDay")
        DAY,
        @SerializedName("currentMile")
        MILE,
        @SerializedName("role")
        ROLE,
        @SerializedName("gender")
        GENDER,
        @SerializedName("health")
        HEALTH,
        @SerializedName("money")
        MONEY,
        @SerializedName("foodCount")
        FOOD_COUNT,
        @SerializedName("itemCount")
        ITEM_COUNT,
        @SerializedName("clothesCount")
        CLOTHES_COUNT,
        @SerializedName("status")
        STATUS,
        @SerializedName("temp")
        TEMP,
        @SerializedName("memberCount")
        MEMBER_COUNT
    }

    /**
     * Does arithmetic on two values
     */
    public static class Equation {
        private final Argument argument = null;
        private final ComplexArgument complexArgument = null;
        private ArithmeticOperator operator;
        private int modifier;

        public enum ArithmeticOperator {
            @SerializedName("*")
            MULTIPLY,
            @SerializedName("+")
            ADD,
            @SerializedName("-")
            SUBTRACT,
            @SerializedName("/")
            DIVIDE
        }

        /**
         * Gets the value after operations
         * @param argValue The value specified in the EventConstraint
         * @return Operated value
         */
        public int getValue(int argValue) {
            switch (this.operator) {
                case MULTIPLY -> {
                    return argValue * this.modifier;
                }
                case ADD -> {
                    return argValue + this.modifier;
                }
                case SUBTRACT -> {
                    return argValue - this.modifier;
                }
                case DIVIDE -> {
                    return argValue / this.modifier;
                }
                default -> {
                    return 0;
                }
            }
        }
    }

    /**
     * An Argument with a value
     */
    public static class ComplexArgument {
        private final Argument argument;
        private final String value;

        // Fake constructor to allow for final values
        private ComplexArgument() {
            this.argument = null;
            this.value = null;
        }
    }

    /**
     * Tests if the conditions are met for the EventConstraint
     * @return if the conditions are met
     */
    public boolean validate(String eventName) {
        if (this.argument != null) {
            Integer intValue = getArgumentValueInt(this.argument);
            if (intValue != null) {
                return compare(intValue);
            } else {
                DebugLogger.warn("EventConstraint for {} is invalid: Integer value not found", eventName);
                return false;
            }
        } else if (this.complexArgument != null) {
            Integer intValue = getArgumentValueInt(this.complexArgument);
            if (intValue != null) {
                return compare(intValue);
            } else {
                DebugLogger.warn("EventConstraint for {} is invalid: Integer value not found", eventName);
                return false;
            }
        } else if (this.equation != null) {
            if (this.equation.argument != null) {
                this.argument = this.equation.argument;
                int previousVal = this.intValue;
                this.intValue = this.equation.getValue(previousVal);
                boolean result = validate(eventName);
                this.argument = null;
                this.intValue = previousVal;
                return result;
            } else if (this.equation.complexArgument != null) {
                this.complexArgument = this.equation.complexArgument;
                int previousVal = this.intValue;
                this.intValue = this.equation.getValue(previousVal);
                boolean result = validate(eventName);
                this.complexArgument = null;
                this.intValue = previousVal;
                return result;
            }
        }
        DebugLogger.warn("EventConstraint for {} is invalid: Missing Argument", eventName);
        return false;
    }

    /**
     * Tests if the conditions are met for the EventConstraint
     * @param eventName The event name for debugging
     * @param member The member to test the conditions against
     * @return if the conditions are met
     */
    public boolean validate(String eventName, Member member) {
        if (this.argument != null) {
            Argument arg = this.argument;
            String strValue = getArgumentValueStr(arg, member);
            Integer intValue = getArgumentValueInt(arg, member);
            List<String> strListValue = getArgumentValueStrList(arg, member);
            if (intValue != null) {
                return compare(intValue);
            } else if (strValue != null) {
                return compare(strValue);
            } else if (strListValue != null) {
                return compare(strListValue);
            } else {
                DebugLogger.warn("EventConstraint for {} is invalid: Integer &/Or String value not found", eventName);
                return false;
            }
        } else if (this.complexArgument != null) {
            // This isn't used with Member
            return validate(eventName);
        } else if (this.equation != null) {
            if (this.equation.argument != null) {
                this.argument = this.equation.argument;
                int previousVal = this.intValue;
                this.intValue = this.equation.getValue(previousVal);
                boolean result = validate(eventName, member);
                this.argument = null;
                this.intValue = previousVal;
                return result;
            } else if (this.equation.complexArgument != null) {
                this.complexArgument = this.equation.complexArgument;
                int previousVal = this.intValue;
                this.intValue = this.equation.getValue(previousVal);
                boolean result = validate(eventName, member);
                this.complexArgument = null;
                this.intValue = previousVal;
                return result;
            }
        }
        DebugLogger.warn("EventConstraint for {} is invalid: Missing Argument", eventName);
        return false;
    }

    private boolean compare(int value) {
        if (this.intValue != null) {
            int intValue = this.intValue;
            switch (this.compareOperator) {
                case EQUALS -> {
                    DebugLogger.info("{} == {}", value, intValue);
                    return value == intValue;
                }
                case NOT_EQUALS -> {
                    DebugLogger.info("{} != {}", value, intValue);
                    return value != intValue;
                }
                case GREATER_THAN -> {
                    DebugLogger.info("{} > {}", value, intValue);
                    return value > intValue;
                }
                case LESS_THAN -> {
                    DebugLogger.info("{} < {}", value, intValue);
                    return value < intValue;
                }
                case GREATER_THAN_OR_EQUAL_TO -> {
                    DebugLogger.info("{} >= {}", value, intValue);
                    return value >= intValue;
                }
                case LESS_THAN_OR_EQUAL_TO -> {
                    DebugLogger.info("{} <= {}", value, intValue);
                    return value <= intValue;
                }
                default -> {
                    DebugLogger.warn("EventConstraint is invalid: contains operator not implemented for int");
                    return false;
                }
            }
        }
        DebugLogger.warn("EventConstraint is invalid: Integer value not found");
        return false;
    }

    private boolean compare(String value) {
        if (this.stringValue != null) {
            String stringValue = this.stringValue;
            switch (this.compareOperator) {
                case EQUALS -> {
                    DebugLogger.info("{} == {}", stringValue, value);
                    return stringValue.equals(value);
                }
                case NOT_EQUALS -> {
                    DebugLogger.info("{} != {}", stringValue, value);
                    return !stringValue.equals(value);
                }
                default -> {
                    DebugLogger.warn("EventConstraint is invalid: String doesn't implement greater/less than or contains");
                    return false;
                }
            }
        } else {
            DebugLogger.warn("EventConstraint is invalid: String value not found");
            return false;
        }
    }

    private boolean compare(List<String> value) {
        if (this.stringValue != null) {
            String stringValue = this.stringValue;
            switch (this.compareOperator) {
                case CONTAINS -> {
                    return value.contains(stringValue);
                }
                case NOT_CONTAINS -> {
                    return !value.contains(stringValue);
                }
                default -> {
                    DebugLogger.warn("EventConstraint is invalid: String list only implements contains operator");
                    return false;
                }
            }
        } else {
            DebugLogger.warn("EventConstraint is invalid: String value not found");
            return false;
        }
    }

    private Integer getArgumentValueInt(Argument argument) {
        switch (argument) {
            case DAY -> {
                return Game.getInstance().getDate().getDay();
            }
            case MILE -> {
                return Game.getInstance().getCurrentMile();
            }
            case FOOD_COUNT -> {
                int foodCount = 0;
                for (ItemStack item : Game.getInstance().getInventory().getItems()) {
                    if (item.getType() == ItemStack.ItemType.FOOD) {
                        foodCount += item.getCount();
                    }
                }
                return foodCount;
            }
            case ITEM_COUNT -> {
                return Game.getInstance().getInventory().getItemCount();
            }
            case CLOTHES_COUNT -> {
                int clothesCount = 0;
                for (ItemStack item : Game.getInstance().getInventory().getItems()) {
                    if (item.getType() == ItemStack.ItemType.CLOTHES) {
                        clothesCount += item.getCount();
                    }
                }
                return clothesCount;
            }
            case MONEY -> {
                // Not ideal, but it works
                return (int) Math.ceil(Game.getInstance().getMoney());
            }
            case TEMP -> {
                return WeatherHelper.getTemp();
            }
            case MEMBER_COUNT -> {
                return Game.getInstance().getMembers().size();
            }
            default -> {
                // Shouldn't be possible to get this
                return null;
            }
        }
    }

    private Integer getArgumentValueInt(Argument argument, Member member) {
        return argument == Argument.HEALTH ? Integer.valueOf(member.getHealth()) : getArgumentValueInt(argument);
    }

    private String getArgumentValueStr(Argument argument, Member member) {
        switch (argument) {
            case ROLE -> {
                return member.getRole().id();
            }
            case GENDER -> {
                return member.getGender().name();
            }
            default -> {
                // Shouldn't be possible to get this
                return null;
            }
        }
    }

    private List<String> getArgumentValueStrList(Argument argument, Member member) {
        if (argument == Argument.STATUS) {
            List<String> statusList = new ArrayList<>();
            for (StatusContainer status : member.getStatuses()) {
                statusList.add(status.getName());
            }
            return statusList;
        } else {
            return null;
        }
    }

    private Integer getArgumentValueInt(ComplexArgument complexArgument) {
        if (complexArgument.argument == Argument.ITEM_COUNT) {
            return (int) Game.getInstance().getInventory().itemStackCount(complexArgument.value);
        } else {
            return null;
        }
    }

    /**
     * Gets if the event constraint requires testing against a member
     * @return if a member should be passed in the validate method
     */
    public boolean requiresMember() {
        return this.argument == Argument.ROLE || this.argument == Argument.GENDER || this.argument == Argument.HEALTH || this.argument == Argument.STATUS;
    }
}
