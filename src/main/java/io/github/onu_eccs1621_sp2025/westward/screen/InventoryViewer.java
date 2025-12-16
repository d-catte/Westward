package io.github.onu_eccs1621_sp2025.westward.screen;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import io.github.onu_eccs1621_sp2025.westward.TrailApplication;
import io.github.onu_eccs1621_sp2025.westward.data.Audio;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.game.ItemStack;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.sound.SoundEngine;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

import java.util.ArrayList;
import java.util.List;

public class InventoryViewer extends Screen {
    private final ImInt inventoryFilterIndex = new ImInt(0);
    private List<ItemStack> listCache = new ArrayList<>();
    private String[] filters = new String[6];
    /**
     * Displays the members' inventory
     */
    public InventoryViewer() {
        super("inventory");
    }

    @Override
    public void setVisible() {
        super.setVisible();
        reloadTranslations();
        if (listCache.isEmpty()) {
            listCache = Game.getInstance().getInventory().getItems();
        }
    }

    @Override
    public void render() {
        // Exit button
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        ImVec2 closeButtonPos = RenderUtils.getCursorRelative(0, 0.92F);
        ImVec2 buttonSize = RenderUtils.getCursorRelative(0.2F, 0.05F);
        RenderUtils.centeredHorizontal(buttonSize.x, closeButtonPos.y);
        if (ImGui.button(Translations.getTranslatedText("gamePlaque.exit"), buttonSize.x, buttonSize.y)) {
            SoundEngine.loadSFX(Audio.CLICK_2);
            this.close();
        }
        ImVec2 filterPos = RenderUtils.getCursorRelative(0.02F, 0.02F);
        ImGui.setCursorPos(filterPos);
        ImGui.text(Translations.getTranslatedText("inventory.filter"));
        ImGui.setCursorPos(filterPos.x + ImGui.calcTextSizeX(Translations.getTranslatedText("inventory.filter") + 20.0F), filterPos.y);
        if (ImGui.combo("##Filter", inventoryFilterIndex, filters)) {
            SoundEngine.loadSFX(Audio.CLICK_2);
            switch (inventoryFilterIndex.get()) {
                case 0 -> listCache = Game.getInstance().getInventory().getItems();
                case 1 -> listCache = Game.getInstance().getInventory().getOfType(ItemStack.ItemType.AMMUNITION);
                case 2 -> listCache = Game.getInstance().getInventory().getOfType(ItemStack.ItemType.FOOD);
                case 3 -> listCache = Game.getInstance().getInventory().getOfType(ItemStack.ItemType.MEDICINE);
                case 4 -> listCache = Game.getInstance().getInventory().getOfType(ItemStack.ItemType.WAGON_PARTS);
                case 5 -> listCache = Game.getInstance().getInventory().getOfType(ItemStack.ItemType.WEAPON);
                case 6 -> listCache = Game.getInstance().getInventory().getOfType(ItemStack.ItemType.CLOTHES);
            }
        }

        ImGui.setCursorPos(RenderUtils.getCursorRelative(0.02F, 0.18F));
        ImVec2 inventoryPanelSize = RenderUtils.getCursorRelative(0.95F, 0.7F);
        ImGui.beginChild("inventory", inventoryPanelSize, false, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        for (int i = 0; i < listCache.size(); i++) {
            ItemStack stack = listCache.get(i);
            ImGui.setCursorPos(inventoryPanelSize.x * 0.02F, inventoryPanelSize.y * 0.08F * i);
            ImGui.text(stack.getName() + "  ( " + stack.getCount() + " )");
        }
        ImGui.endChild();
        Format.clearFontSize();
    }

    /**
     * Reloads the translations with updated ones
     */
    public void reloadTranslations() {
        filters = new String[] {
                Translations.getTranslatedText("itemType.none"),
                Translations.getTranslatedText("itemType.ammunition"),
                Translations.getTranslatedText("itemType.food"),
                Translations.getTranslatedText("itemType.medicine"),
                Translations.getTranslatedText("itemType.wagonParts"),
                Translations.getTranslatedText("itemType.weapon"),
                Translations.getTranslatedText("itemType.clothes")
        };
    }
}
