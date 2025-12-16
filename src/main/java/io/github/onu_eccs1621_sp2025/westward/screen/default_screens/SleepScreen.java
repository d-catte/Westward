package io.github.onu_eccs1621_sp2025.westward.screen.default_screens;

import imgui.ImColor;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import io.github.onu_eccs1621_sp2025.westward.data.Audio;
import io.github.onu_eccs1621_sp2025.westward.data.StatusContainer;
import io.github.onu_eccs1621_sp2025.westward.data.member.Member;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.game.ItemStack;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.utils.ListUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.sound.SoundEngine;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SleepScreen extends Screen {
    private final ImInt memberIndex = new ImInt(0);
    private boolean playerDied;
    private Boolean successfulHeal = null;
    private boolean removedStatus;
    private String statusAltered;
    /**
     * Allows the player to make decisions when they sleep
     */
    public SleepScreen() {
        super("sleep");
    }

    @Override
    public void render() {
        Format.setFontSize(Format.TITLE_FONT_SIZE);
        RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("sleep.title")), RenderUtils.getCursorRelative(0.0F, 0.05F).y);
        ImGui.text(Translations.getTranslatedText("sleep.title"));
        Format.clearFontSize();
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        renderPlayerPanels();
        ImVec2 healButtonSize = RenderUtils.getCursorRelative(0.25F, 0.05F);
        RenderUtils.centeredHorizontal(healButtonSize.x, RenderUtils.getCursorRelative(0.0F, 0.6F).y);
        Format.clearFontSize();
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        if (Game.getInstance().hasHealers()) {
            if (Game.getInstance().getInventory().countOfType(ItemStack.ItemType.MEDICINE) > 0 &&
                    ImGui.button(Translations.getTranslatedText("sleep.heal") + " (" + Game.getInstance().getInventory().countOfType(ItemStack.ItemType.MEDICINE) + ")", healButtonSize)) {
                SoundEngine.loadSFX(Audio.CLICK_1);
                Member member = Game.getInstance().getMembers().get(this.memberIndex.get());
                // Don't heal if at full health
                if (member.getHealth() == member.getMaxHealth() && member.getHealableStatuses().isEmpty()) {
                    Format.clearFontSize();
                    RenderUtils.closeButtonCentered(this);
                    return;
                }
                float healPercent = switch (Game.getInstance().getDifficulty()) {
                    case 1 -> 0.6F;
                    case 2 -> 0.4F;
                    default -> 0.2F;
                };
                this.successfulHeal = ThreadLocalRandom.current().nextFloat() < healPercent;
                List<StatusContainer> healableStatuses = member.getHealableStatuses();
                if (healableStatuses.isEmpty()) {
                    int healthChanged = (int) (member.getMaxHealth() * 0.2F);
                    if (this.successfulHeal) {
                        member.modifyHealth(healthChanged);
                    } else {
                        member.modifyHealth(-healthChanged);
                    }
                    this.removedStatus = false;
                } else {
                    StatusContainer status = (StatusContainer) ListUtils.getRandomElement(healableStatuses);

                    if (this.successfulHeal) {
                        if (!status.decreaseLevel()) {
                            member.getStatuses().remove(status);
                        }
                    } else {
                        if (status.increaseLevel()) {
                            member.onDeath();
                        }
                    }
                    this.removedStatus = true;
                    this.statusAltered = status.getName();
                }

                Game.getInstance().getInventory().removeOfType(ItemStack.ItemType.MEDICINE, 1.0F, 0.0F);
                this.playerDied = !Game.getInstance().getMembers().contains(member);
            }
        } else {
            RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("sleep.heal.noHealers")), RenderUtils.getCursorRelative(0.0F, 0.65F).y);
            ImGui.textColored(ImColor.rgb(255, 30, 30), Translations.getTranslatedText("sleep.heal.noHealers"));
        }
        if (Game.getInstance().getMembers().isEmpty()) {
            this.close();
            this.reset();
            Format.clearFontSize();
            return;
        }
        if (this.successfulHeal != null && !playerDied) {
            Member member = Game.getInstance().getMembers().get(this.memberIndex.get());
            int healthChanged = (int) (member.getMaxHealth() * 0.2F);
            if (this.successfulHeal) {
                if (this.removedStatus) {
                    RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("sleep.heal.statusGood", member.getName(), this.statusAltered)), RenderUtils.getCursorRelative(0.0F, 0.65F).y);
                    ImGui.textColored(ImColor.rgb(30, 255, 30), Translations.getTranslatedText("sleep.heal.statusGood", member.getName(), this.statusAltered));
                } else {
                    RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("sleep.heal.good", member.getName(), String.valueOf(healthChanged))), RenderUtils.getCursorRelative(0.0F, 0.65F).y);
                    ImGui.textColored(ImColor.rgb(30, 255, 30), Translations.getTranslatedText("sleep.heal.good", member.getName(), String.valueOf(healthChanged)));
                }
            } else {
                if (this.removedStatus) {
                    RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("sleep.heal.statusBad", member.getName(), this.statusAltered)), RenderUtils.getCursorRelative(0.0F, 0.65F).y);
                    ImGui.textColored(ImColor.rgb(255, 30, 30), Translations.getTranslatedText("sleep.heal.statusBad", member.getName(), this.statusAltered));
                } else {
                    RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("sleep.heal.bad", member.getName(), String.valueOf(healthChanged))), RenderUtils.getCursorRelative(0.0F, 0.65F).y);
                    ImGui.textColored(ImColor.rgb(255, 30, 30), Translations.getTranslatedText("sleep.heal.bad", member.getName(), String.valueOf(healthChanged)));
                }
            }
        }
        Format.clearFontSize();
        RenderUtils.closeButtonCentered(this);
    }

    private void renderPlayerPanels() {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, ImColor.rgb(50, 50, 50));
        ImVec2 playerPanelsSize = RenderUtils.getCursorRelative(0.3F, 0.3F);
        ImVec2 playerPanelsPos = RenderUtils.getCursorRelative(0.0F, 0.15F);
        RenderUtils.centeredHorizontal(playerPanelsSize.x, playerPanelsPos.y);
        ImGui.beginChild("playerPanels", playerPanelsSize, false, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        renderPlayerPanel(Game.getInstance().getMembers().get(this.memberIndex.get()), playerPanelsSize);
        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private void renderPlayerPanel(Member member, ImVec2 panelSize) {
        ImGui.setCursorPos(panelSize.x * 0.5F - ImGui.calcTextSizeX(member.getName()) / 2F, panelSize.y * 0.02F);
        ImGui.text(member.getName());

        ImGui.setCursorPos(panelSize.x * 0.1F, panelSize.y * 0.15F);
        ImVec2 playerPanelsSize = new ImVec2(panelSize.x * 0.8F, panelSize.y * 0.6F);
        ImGui.beginChild("playerStatuses", playerPanelsSize, false, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        for (StatusContainer status : member.getStatuses()) {
            if (status.getLevel() + 1 >= status.getMaxLevel()) {
                ImGui.textColored(ImColor.rgb(255, 0, 0), status.toString());
            } else {
                ImGui.text(status.toString());
            }
        }
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
        if (this.memberIndex.get() >= memberCount) {
            this.memberIndex.set(memberCount - 1);
        }
        if (ImGui.button("<-##memberPanel", buttonSize)) {
            SoundEngine.loadSFX(Audio.CLICK_1);
            this.memberIndex.set(this.memberIndex.get() - 1 >= 0 ? this.memberIndex.get() - 1 : memberCount - 1);
            this.successfulHeal = null;
        }

        ImGui.sameLine();
        ImGui.setCursorPosX(panelSize.x * 0.9F);
        if (ImGui.button("->##memberPanel", buttonSize)) {
            SoundEngine.loadSFX(Audio.CLICK_1);
            this.memberIndex.set((this.memberIndex.get() + 1) % memberCount);
            this.successfulHeal = null;
        }

        Format.clearFontSize();
    }

    private void reset() {
        this.memberIndex.set(0);
        this.successfulHeal = null;
        this.playerDied = false;
        this.removedStatus = true;
        this.statusAltered = RenderUtils.EMPTY_STR;
    }
}
