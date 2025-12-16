package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.trading;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.type.ImInt;
import io.github.onu_eccs1621_sp2025.westward.TrailApplication;
import io.github.onu_eccs1621_sp2025.westward.data.Audio;
import io.github.onu_eccs1621_sp2025.westward.data.member.Gender;
import io.github.onu_eccs1621_sp2025.westward.data.member.Member;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.game.ItemStack;
import io.github.onu_eccs1621_sp2025.westward.screen.Renderer;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.screen.default_screens.accident.ConsequenceScreen;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.sound.SoundEngine;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The screen where players trade with Native Americans and other settlers
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public class TradingScreen extends Screen {
    private final List<TradingItem> inventory;
    private final String translationKey;
    private final String image;

    private boolean firstStage = true;
    private short tradeAttemptsLeft;
    private TradingItem traderItem;
    private int inventorySize;
    ArrayList<String> playerNames;
    ImInt memberIndex = new ImInt(0);
    ImInt[] selectedItemAmounts;

    /**
     * Creates the trading screen.
     * @param data The data for the trading screen
     */
    public TradingScreen(TradingScreenData data) {
        super(data.id());
        this.inventory = data.possibleItems();
        this.translationKey = data.translationKey();
        this.image = data.imagePath();
    }

    @Override
    public void render() {
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        if (firstStage) {
                RenderUtils.textWrapScaling();
                ImGui.textWrapped(Translations.getTranslatedText(this.translationKey));
                ImGui.popTextWrapPos();

                ImVec2 imagePos = RenderUtils.getCursorRelative(0, 0.08F);
                ImVec2 imageScale = RenderUtils.getItemScaleMaintainAspectRatio(1.1F, 0.7F);
                RenderUtils.centeredHorizontal(imageScale.x, imagePos.y);
                ImGui.image((Long) Registry.getAsset(Registry.AssetType.ASSET, this.image), imageScale);
                ImVec2 separatorPos = RenderUtils.getCursorRelative(0, 0.8F);
                ImGui.setCursorPosY(separatorPos.y);
                ImGui.separator();

                ImVec2 buttonSize = RenderUtils.getCursorRelative(0.2F, 0.05F);
                ImVec2 tradeButtonPos = RenderUtils.getCursorRelative(0.25F, 0.9F);
                ImVec2 leaveButtonPos = RenderUtils.getCursorRelative(0.55F, 0.9F);
                ImGui.setCursorPos(tradeButtonPos);
                if (ImGui.button(Translations.getTranslatedText("game.button.trade"), buttonSize.x, buttonSize.y)) {
                    SoundEngine.loadSFX(Audio.CLICK_1);
                    // Select item to trade by choosing a random float from 0 to 1
                    float itemSelection = ThreadLocalRandom.current().nextFloat();

                    // To find the corresponding item, add up the chances of each one available until it passes itemSelection
                    float itemChanceAccumulation = 0F;
                    for (int i = 0; i < this.inventory.size() && itemChanceAccumulation < itemSelection; i++) {
                        traderItem = this.inventory.get(i);
                        itemChanceAccumulation += traderItem.chance();
                    }

                    // Fill player name array
                    playerNames = new ArrayList<>();
                    for (Member player : Game.getInstance().getMembers()) {
                        playerNames.add(player.getName());
                    }

                    // Initialize inventory item list
                    inventorySize = Game.getInstance().getInventory().getItems().size();
                    selectedItemAmounts = new ImInt[inventorySize];
                    for (int i = 0; i < inventorySize; i++) {
                        selectedItemAmounts[i] = new ImInt(0);
                    }
                    // Reset trade attempts left
                    tradeAttemptsLeft = 3;

                    // Go to the next screen
                    firstStage = false;
                }
                ImGui.setCursorPos(leaveButtonPos);
                if (ImGui.button(Translations.getTranslatedText("tradingScreen.leave"), buttonSize.x, buttonSize.y)) {
                    SoundEngine.loadSFX(Audio.CLICK_2);
                    this.close();
                }
        } else {
                RenderUtils.textWrapScaling();
                ImVec2 playerChoiceTextWidth = ImGui.calcTextSize(Translations.getTranslatedText("tradingScreen.playerChoice"));
                RenderUtils.centeredHorizontal(playerChoiceTextWidth.x, 0F);
                ImGui.textWrapped(Translations.getTranslatedText("tradingScreen.playerChoice"));
                ImGui.popTextWrapPos();

                ImVec2 combo = RenderUtils.getCursorRelative(0.4F, 0.02F);
                RenderUtils.centeredHorizontal(combo.x, playerChoiceTextWidth.y + combo.y);
                ImGui.setNextItemWidth(combo.x);
                if (ImGui.combo("##player", memberIndex, playerNames.toArray(new String[0]))) {
                    SoundEngine.loadSFX(Audio.CLICK_2);
                }

                ImGui.setCursorPos(RenderUtils.getCursorRelative(0.01F, 0.15F));
                RenderUtils.textWrapScaling();
                ImGui.textWrapped(Translations.getTranslatedText("tradingScreen.trade", Translations.getTranslatedText(traderItem.id())));
                ImGui.popTextWrapPos();

                ImGui.setCursorPos(RenderUtils.getCursorRelative(0.1F, 0.25F));
                ImGui.beginListBox("##inventory", RenderUtils.getCursorRelative(0.8F, 0.6F));
                for (int i = 0; i < inventorySize; i++) {
                    ItemStack item = Game.getInstance().getInventory().getItems().get(i);

                    ImGui.setCursorPos(RenderUtils.getCursorRelative(0.02F, 0.02F + i * 0.06F));
                    ImGui.setNextItemWidth(RenderUtils.getCursorRelative(0.2F, 0F).x);
                    if (ImGui.inputInt(" " + item.getName(), selectedItemAmounts[i], 1, item.getCount())) {
                        if (selectedItemAmounts[i].get() > item.getCount()) {
                            selectedItemAmounts[i].set(item.getCount());
                        } else if (selectedItemAmounts[i].get() < 0) {
                            selectedItemAmounts[i].set(0);
                        }
                    }
                }
                ImGui.endListBox();

                ImVec2 buttonSize = RenderUtils.getCursorRelative(0.2F, 0.05F);
                ImVec2 tradeButtonPos = RenderUtils.getCursorRelative(0.25F, 0.9F);
                ImVec2 leaveButtonPos = RenderUtils.getCursorRelative(0.55F, 0.9F);
                ImGui.setCursorPos(tradeButtonPos);
                if (ImGui.button(Translations.getTranslatedText("game.button.trade"), buttonSize.x, buttonSize.y)) {
                    SoundEngine.loadSFX(Audio.CLICK_1);
                    List<ItemStack> tradeOffer = new ArrayList<>();
                    for (int i = 0; i < inventorySize; i++) {
                        if (selectedItemAmounts[i].get() > 0) {
                            tradeOffer.add(new ItemStack(Game.getInstance().getInventory().getItems().get(i).getId(),
                                                         (short) selectedItemAmounts[i].get(),
                                                         Game.getInstance().getInventory().getItems().get(i).getBarterValue()));
                        }
                    }
                    if (isTradeSuccess(Game.getInstance().getMembers().get(memberIndex.get()), tradeOffer)) {
                        // Complete the exchange
                        for (int i = 0; i < inventorySize; i++) {
                            String inventoryItemId = Game.getInstance().getInventory().getItems().get(i).getId();

                            // Add 1 of the traded item to inventory
                            if (inventoryItemId.equals(traderItem.id())) {
                                Game.getInstance().getInventory().addItemStack(new ItemStack(traderItem.id(), (short) 1, traderItem.barterValue()));
                            }
                            // Skip removal of item if 0 selected
                            if (selectedItemAmounts[i].get() == 0) {
                                continue;
                            }

                            ItemStack toRemove = new ItemStack(inventoryItemId, (short) selectedItemAmounts[i].get());
                            Game.getInstance().getInventory().removeItemStack(toRemove);
                        }

                        // Show trade success screen
                        ConsequenceScreen screen = (ConsequenceScreen) Registry.getAsset(Registry.AssetType.SCREEN, "consequence");
                        screen.setData(Translations.getTranslatedText("tradingScreen.success", Translations.getTranslatedText(traderItem.id())));
                        Renderer.RENDER_QUEUE.add("consequence");
                        // Reset game
                        tradeAttemptsLeft = 3;
                        firstStage = true;
                        this.close();
                    } else {
                        switch (--tradeAttemptsLeft) {
                            case 2: {
                                ConsequenceScreen screen = (ConsequenceScreen) Registry.getAsset(Registry.AssetType.SCREEN, "consequence");
                                screen.setData(Translations.getTranslatedText("tradingScreen.pluralFail"));
                                // Go to consequence screen then return to trading
                                Renderer.RENDER_QUEUE.add("consequence");
                                Renderer.RENDER_QUEUE.add(this.getId());
                                this.close();
                                break;
                            }
                            case 1: {
                                ConsequenceScreen screen = (ConsequenceScreen) Registry.getAsset(Registry.AssetType.SCREEN, "consequence");
                                screen.setData(Translations.getTranslatedText("tradingScreen.singularFail"));
                                // Go to consequence screen then return to trading
                                Renderer.RENDER_QUEUE.add("consequence");
                                Renderer.RENDER_QUEUE.add(this.getId());
                                this.close();
                                break;
                            }
                            case 0: {
                                int itemsToTake = (int) ThreadLocalRandom.current().nextGaussian(3, 1);
                                Game.getInstance().getInventory().removeRandomItems(itemsToTake);

                                Renderer.addConsequence("tradingScreen.finalFail", Integer.toString(itemsToTake));
                                firstStage = true;
                                this.close();
                            }
                        }
                    }
                }
                ImGui.setCursorPos(leaveButtonPos);
                if (ImGui.button(Translations.getTranslatedText("tradingScreen.leave"), buttonSize.x, buttonSize.y)) {
                    SoundEngine.loadSFX(Audio.CLICK_2);
                    firstStage = true;
                    this.close();
                }
        }
        Format.clearFontSize();
    }

    private boolean isTradeSuccess(Member player, List<ItemStack> playerOffer) {
        float playerTradeValue = 0F;
        for (ItemStack item : playerOffer) {
            playerTradeValue += item.getBarterValue() * item.getCount();
        }

        // Make trading more difficult with a higher difficulty setting
        float tradeSuccessMultiplier = switch (Game.getInstance().getDifficulty()) {
            case 1 -> 0.5F;
            case 2 -> 0.375F;
            case 3 -> 0.25F;
            default -> 1F;
        };
        // Women were historically more successful at trading
        if (player.getGender() == Gender.FEMALE) {
            tradeSuccessMultiplier *= 2F;
        }

        float chanceOfSuccess = tradeSuccessMultiplier * playerTradeValue / this.traderItem.barterValue();
        return ThreadLocalRandom.current().nextFloat() <= chanceOfSuccess;
    }
}
