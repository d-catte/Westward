package io.github.onu_eccs1621_sp2025.westward.screen;

import imgui.ImColor;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import io.github.onu_eccs1621_sp2025.westward.TrailApplication;
import io.github.onu_eccs1621_sp2025.westward.data.Audio;
import io.github.onu_eccs1621_sp2025.westward.data.Landmark;
import io.github.onu_eccs1621_sp2025.westward.data.StatusContainer;
import io.github.onu_eccs1621_sp2025.westward.data.member.Member;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.game.ItemStack;
import io.github.onu_eccs1621_sp2025.westward.utils.Config;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.sound.SoundEngine;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Screen that shows all the stats of the current game including the date, money, and miles travelled
 * @author Dylan Catte
 * @author Ben Westover
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public class Dashboard {
    private static final AtomicInteger MEMBER_INDEX = new AtomicInteger(0);

    /**
     * Renders the Dashboard
     */
    public static void render() {
        if (Game.getInstance() != null) {
            renderGameplayOptionBar();
            renderDataPlaque();
            if (!Game.getInstance().getMembers().isEmpty()) {
                renderPlayerPanels();
            }
            if (Game.getInstance().isWaitingForUserInput()) {
                renderSaveGameButton();
            }
            renderProgressBar();
        }
    }

    /**
     * Gets a Thread-safe index for the Members
     * @return Atomic Member index
     */
    public static AtomicInteger getMemberIndex() {
        return MEMBER_INDEX;
    }

    private static void renderProgressBar() {
        ImVec2 topCorner = RenderUtils.getCursorRelative(0.1F, 0.9F);
        ImVec2 bottomCorner = RenderUtils.getCursorRelative(0.9F, 0.95F);
        float progressBarLength = bottomCorner.x - topCorner.x;
        float markerPositionX = progressBarLength - (Game.getInstance().percentageToNextLandmark() * progressBarLength) + topCorner.x;
        ImGui.getForegroundDrawList().addRectFilled(topCorner, bottomCorner, ImColor.rgb(100, 100, 255), 8F);

        ImVec2 markerRadius = RenderUtils.getItemScaleMaintainAspectRatio(0.04F, 0.04F);
        ImVec2 markerMinPos = new ImVec2(markerPositionX - markerRadius.x, topCorner.y - markerRadius.y * 2.0F);
        ImVec2 markerMaxPos = new ImVec2(markerPositionX + markerRadius.x, topCorner.y);
        String landmarkSpritePath = Registry.getLandmarkAsset(Game.getInstance().getNextLandmarkIndex()).spritePath();
        ImGui.getForegroundDrawList().addImage((Long) Registry.getAsset(Registry.AssetType.ASSET, landmarkSpritePath), markerMinPos, markerMaxPos);

        ImVec2 wagonPosCenter = RenderUtils.getCursorRelative(0.1F, 0.85F);
        ImVec2 wagonSize = RenderUtils.getItemScaleMaintainAspectRatio(0.064F, 0.04F).times(2, 2);
        ImGui.setCursorPos(wagonPosCenter.x, topCorner.y - wagonSize.y);
        if (Game.getInstance().getCurrentMile() % 2 == 0) {
            ImGui.image((Long) Registry.getAsset(Registry.AssetType.ASSET, "wagonMove"), wagonSize);
        } else {
            ImGui.image((Long) Registry.getAsset(Registry.AssetType.ASSET, "wagonMove1"), wagonSize);
        }
    }

    private static void renderSaveGameButton() {
        ImVec2 saveGameButtonPos = RenderUtils.getCursorRelative(0.95F, 0.02F);
        ImVec2 saveGameButtonSize = RenderUtils.getItemScaleMaintainAspectRatio(0.032F, 0.04F);
        ImGui.setCursorPos(saveGameButtonPos);
        if (ImGui.imageButton("##saveButton", (Long) Registry.getAsset(Registry.AssetType.ASSET, "save"), saveGameButtonSize)) {
            SoundEngine.loadSFX(Audio.CLICK_2);
            TrailApplication.returnToMainMenu(true);
        }
    }

    private static void renderDataPlaque() {
        ImVec2 dataPlaquePos = RenderUtils.getCursorRelative(0.1F, 0.1F);
        ImVec2 dataPlaqueSize = RenderUtils.getCursorRelative(0.3F, 0.25F);
        ImGui.setCursorPos(dataPlaquePos);
        ImGui.beginChild("dataPlaque", dataPlaqueSize, false, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        ImGui.text(Translations.getTranslatedText("gamePlaque.date", Game.getInstance().getDate().toString()));
        ImGui.text(Translations.getTranslatedText("gamePlaque.miles", String.valueOf((Game.getInstance().getCurrentMile() + Config.getConfig().getPreviousMiles(Game.getInstance().getDifficulty())))));
        ImGui.text(Translations.getTranslatedText("gamePlaque.money", String.format("%.2f", Game.getInstance().getMoney())));
        int foodLbs = Game.getInstance().getInventory().countOfType(ItemStack.ItemType.FOOD);
        // Render food count as red if there is no food left
        if (foodLbs == 0) {
            ImGui.textColored(ImColor.rgb(255, 0, 0), Translations.getTranslatedText("gamePlaque.food", String.valueOf(foodLbs)));
        } else {
            ImGui.text(Translations.getTranslatedText("gamePlaque.food", String.valueOf(foodLbs)));
        }
        ImVec2 inventorySize = RenderUtils.getCursorRelative(0.15F, 0.05F);
        if (ImGui.button(Translations.getTranslatedText("menu.trailSelection.inventory"), inventorySize)) {
            SoundEngine.loadSFX(Audio.CLICK_1);
            Renderer.RENDER_QUEUE.add("inventory");
        }
        ImGui.endChild();
    }

    private static void renderPlayerPanels() {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, ImColor.rgb(50, 50, 50));
        ImVec2 playerPanelsSize = RenderUtils.getCursorRelative(0.3F, 0.3F);
        ImVec2 playerPanelsPos = RenderUtils.getCursorRelative(0.55F, 0.05F);
        ImGui.setCursorPos(playerPanelsPos);
        ImGui.beginChild("playerPanels", playerPanelsSize, false, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        renderPlayerPanel(Game.getInstance().getMembers().get(MEMBER_INDEX.get()), playerPanelsSize);
        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private static void renderPlayerPanel(Member member, ImVec2 panelSize) {
        ImGui.setCursorPos(panelSize.x * 0.5F - ImGui.calcTextSizeX(member.getName()) / 2F, panelSize.y * 0.02F);
        ImGui.text(member.getName());
        String role = Translations.getTranslatedText(member.getRole().id());
        Format.setFontSize(Format.MENU_FONT_SIZE);
        ImGui.setCursorPos(panelSize.x * 0.5F - ImGui.calcTextSizeX(role) / 2F, panelSize.y * 0.14F);
        ImGui.textColored(ImColor.rgb(0, 222, 255), role);

        ImGui.setCursorPos(panelSize.x * 0.1F, panelSize.y * 0.24F);
        ImVec2 playerPanelsSize = new ImVec2(panelSize.x * 0.8F, panelSize.y * 0.5F);
        ImGui.beginChild("playerStatuses", playerPanelsSize, false, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        for (StatusContainer status : member.getStatuses()) {
            if (status.getLevel() + 1 >= status.getMaxLevel()) {
                ImGui.textColored(ImColor.rgb(255, 0, 0), status.toString());
            } else {
                ImGui.text(status.toString());
            }
        }
        Format.clearFontSize();
        ImGui.endChild();

        ImGui.setCursorPos(panelSize.x * 0.1F, panelSize.y * 0.88F);
        Format.setFontSize(Format.MENU_FONT_SIZE);
        float healthFraction = (float) member.getHealth() / member.getMaxHealth();
        float[] color = RenderUtils.interpolateColor(healthFraction);
        ImGui.pushStyleColor(ImGuiCol.PlotHistogram, ImColor.rgb(color[0], color[1], color[2]));
        ImGui.progressBar(healthFraction, panelSize.x * 0.8F, panelSize.y * 0.12F);
        ImGui.popStyleColor();
        ImGui.sameLine();

        ImGui.setCursorPosX(0);
        ImVec2 buttonSize = new ImVec2(panelSize.x * 0.1F, panelSize.y * 0.12F);
        int memberCount = Game.getInstance().getMembers().size();
        if (MEMBER_INDEX.get() >= memberCount) {
            MEMBER_INDEX.set(memberCount - 1);
        }
        if (ImGui.button("<-##memberPanel", buttonSize)) {
            SoundEngine.loadSFX(Audio.CLICK_1);
            MEMBER_INDEX.set(MEMBER_INDEX.get() - 1 >= 0 ? MEMBER_INDEX.get() - 1 : memberCount - 1);
        }

        ImGui.sameLine();
        ImGui.setCursorPosX(panelSize.x * 0.9F);
        if (ImGui.button("->##memberPanel", buttonSize)) {
            SoundEngine.loadSFX(Audio.CLICK_1);
            MEMBER_INDEX.set((MEMBER_INDEX.get() + 1) % memberCount);
        }

        Format.clearFontSize();
    }

    private static void renderGameplayOptionBar() {
        List<String> availableButtons = new ArrayList<>();
        List<Integer> buttonIds = new ArrayList<>();

        if (Game.getInstance().isWaitingForUserInput()) {
            if (Game.getInstance().getCurrentMile() == 0) {
                availableButtons.add("menu.trailSelection.begin");
            } else {
                availableButtons.add("menu.trailSelection.continue");
            }
            buttonIds.add(0);
            if (Game.getInstance().canSleep()) {
                availableButtons.add("game.button.rest");
                buttonIds.add(1);
            }
            if (Game.getInstance().isAtCivilization()) {
                availableButtons.add("game.button.shop");
                buttonIds.add(2);
            }
            if (Game.getInstance().hasMinigame()) {
                availableButtons.add(Game.getInstance().getCurrentMinigame().getTranslationKey());
                buttonIds.add(3);
            }
        }

        ImVec2 buttonSize = RenderUtils.getCursorRelative((1.0F - 0.1F) / availableButtons.size(), 0.1F);
        ImVec2 centerPos = RenderUtils.getCursorRelative(0.5F, 0.65F);
        ImVec2 pacePos = RenderUtils.getCursorRelative(0, 0.65F);
        ImVec2 startingPosX = centerPos.minus((buttonSize.x * (availableButtons.size() / 2F)), 0);

        for (int i = 0; i < availableButtons.size(); i++) {
            if (buttonIds.get(i) == 0) {
                ImGui.setCursorPosX(startingPosX.x + i * buttonSize.x);
                ImGui.setCursorPosY(pacePos.y - buttonSize.y);
                ImGui.setNextItemWidth(buttonSize.x);
                if (ImGui.combo("##pace", Game.getInstance().getPace(), Game.getInstance().getPacesTranslations())) {
                    SoundEngine.loadSFX(Audio.CLICK_2);
                }
            }
            ImGui.setCursorPosX(startingPosX.x + i * (buttonSize.x + 15));
            ImGui.setCursorPosY(centerPos.y);
            if (ImGui.button(Translations.getTranslatedText(availableButtons.get(i)), buttonSize)) {
                SoundEngine.loadSFX(Audio.CLICK_1);
                switch (buttonIds.get(i)) {
                    case 0 -> Game.getInstance().setNotWaiting();
                    case 1 -> {
                        Game.getInstance().sleep();
                        Renderer.RENDER_QUEUE.add("sleep");
                    }
                    case 2 -> {
                        Landmark current = Registry.getLandmarkAsset(Game.getInstance().getLastLandmarkIndex());
                        Renderer.RENDER_QUEUE.add(current.shopScreenId());
                        SoundEngine.loadSFX(Audio.SHOP_BELL);
                    }
                    case 3 -> {
                        switch (Game.getInstance().getCurrentMinigame()) {
                            case HUNTING -> Renderer.RENDER_QUEUE.add(((Screen) Registry.randomAsset(Registry.AssetType.HUNTING_SCREEN)).getId());
                            case RIVER -> Renderer.RENDER_QUEUE.add("river");
                            case NONE -> {}
                        }
                        Game.getInstance().playMinigame();
                    }
                }
            }
        }
    }
}
