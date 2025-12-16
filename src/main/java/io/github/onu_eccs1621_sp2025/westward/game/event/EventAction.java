package io.github.onu_eccs1621_sp2025.westward.game.event;

import com.google.gson.annotations.Expose;
import io.github.onu_eccs1621_sp2025.westward.data.StatusContainer;
import io.github.onu_eccs1621_sp2025.westward.data.member.Member;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.game.ItemStack;
import io.github.onu_eccs1621_sp2025.westward.screen.Renderer;
import io.github.onu_eccs1621_sp2025.westward.utils.DebugLogger;
import io.github.onu_eccs1621_sp2025.westward.utils.math.IntegerRange;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;

import java.util.List;

/**
 * The actions ran when an Event is executed
 * @author Dylan Catte
 * @author Ben Westover
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public class EventAction {
    @Expose
    private final List<EventConstraint> constraint = null;
    @Expose
    private final IntegerRange amount = null;
    @Expose
    private final String type = null;
    @Expose
    private final Action action;
    private Runnable consequence;

    // Fake constructor to make variables final
    private EventAction() {
        this.action = null;
    }

    private enum Action {
        ADD_DAYS,
        REMOVE_MILES,
        ADD_STATUS_MEMBER,
        ADD_STATUS_ALL,
        REMOVE_ITEMS,
        REMOVE_MONEY,
        KILL,
        SHOW_SCREEN,
        BREAK_WAGON,
    }

    /**
     * Runs the action
     */
    public boolean execute(String eventName) {
        if (Game.getInstance().getMembers().isEmpty()) {
            return false;
        }
        Member randomMember = Game.getInstance().getRandomMember();
        if (this.constraint != null) {
            for (EventConstraint eventConstraint : this.constraint) {
                if (eventConstraint.requiresMember()) {
                    if (!eventConstraint.validate(eventName, randomMember)) {
                        DebugLogger.info("Constraints not met for {}", eventName);
                        return false;
                    }
                } else if (!eventConstraint.validate(eventName)) {
                    DebugLogger.info("Constraints not met for {}", eventName);
                    return false;
                }
            }
        }

        switch (this.action) {
            case ADD_DAYS -> {
                int val = this.amount.random();
                Game.getInstance().getDate().addDays(val);
                this.consequence = () -> Renderer.addConsequence("notification.addDays", String.valueOf(val));
            }
            case REMOVE_MILES -> {
                int val = this.amount.random();
                Game.getInstance().modifyCurrentMile(-val);
                this.consequence = () -> Renderer.addConsequence("notification.removeMiles", String.valueOf(val));
            }
            case ADD_STATUS_MEMBER -> {
                String status = this.type;
                if (randomMember.addStatus(((StatusContainer) Registry.getAsset(Registry.AssetType.STATUS, status)).shallowClone())) {
                    return false;
                } else {
                    StatusContainer container = (StatusContainer) Registry.getAsset(Registry.AssetType.STATUS, status);
                    String obtainedTranslation = container.getObtainedTranslation();
                    this.consequence = () -> Renderer.addConsequence(obtainedTranslation, randomMember.getName(), status);
                }
            }
            case ADD_STATUS_ALL -> {
                this.consequence = () -> Renderer.addConsequence("notification.addStatusAll", this.type);
                for (Member member : Game.getInstance().getMembers()) {
                    member.addStatus(((StatusContainer) Registry.getAsset(Registry.AssetType.STATUS, this.type)).shallowClone());
                }
            }
            case REMOVE_ITEMS -> {
                int amount = this.amount.random();
                if (this.type != null) {
                    int has = Game.getInstance().getInventory().itemStackCount(this.type);
                    if (has >= amount) {
                        Game.getInstance().getInventory().removeItemStack(new ItemStack(this.type, (short) amount));
                        this.consequence = () -> Renderer.addConsequence("notification.removeItem", String.valueOf(amount), this.type);
                    } else {
                        Game.getInstance().getInventory().removeItemStack(new ItemStack(this.type, (short) has));
                        this.consequence = () -> Renderer.addConsequence("notification.removeAllItem", this.type);
                    }
                } else {
                    Game.getInstance().getInventory().removeRandomItems(amount);
                    this.consequence = () -> Renderer.addConsequence("notification.removeRandomItem", String.valueOf(amount));
                }
            }
            case REMOVE_MONEY -> {
                float currentMoney = Game.getInstance().getMoney();

                // Randomly select money to take from party but ensure it's not more than they have
                float amountToTake = this.amount.random();
                DebugLogger.info("Taking ${}", amountToTake);
                if (amountToTake > currentMoney) {
                    amountToTake = currentMoney;
                }

                Game.getInstance().modifyMoney(-amountToTake);
                if (amountToTake == currentMoney) {
                    this.consequence = () -> Renderer.addConsequence("notification.removeAllMoney");
                } else {
                    float finalAmountToTake = amountToTake;
                    this.consequence = () -> Renderer.addConsequence("notification.removeMoney", String.format("%.2f", finalAmountToTake));
                }
            }
            case KILL -> {
                randomMember.onDeath();
                if (this.type != null) {
                    this.consequence = () -> Renderer.addConsequence("notification.deathWithCause", randomMember.getName(), this.type);
                } else {
                    this.consequence = () -> Renderer.addConsequence( "notification.deathWithoutCause", randomMember.getName());
                }
            }
            case SHOW_SCREEN -> {
                // only show the event screen, no consequence
                this.consequence = () -> {};
            }
            case BREAK_WAGON -> {
                if (Game.getInstance().getInventory().removeOfType(ItemStack.ItemType.WAGON_PARTS, 1)) {
                    // Wagon fixed screen
                    this.consequence = () -> Renderer.addConsequence("notification.wagonFixed");
                } else {
                    this.consequence = () -> {
                        Renderer.addConsequence("notification.wagonBroke");
                        // Kill all players
                        Game.getInstance().getMembers().clear();
                    };
                }
            }
        }
        return true;
    }

    /**
     * Gets the consequence of the event action
     * @return Gets a Runnable representation of the action
     */
    public Runnable getConsequence() {
        return this.consequence;
    }
}
