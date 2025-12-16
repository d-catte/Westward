package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.hunting;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImInt;
import io.github.onu_eccs1621_sp2025.westward.TrailApplication;
import io.github.onu_eccs1621_sp2025.westward.data.Audio;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.game.ItemStack;
import io.github.onu_eccs1621_sp2025.westward.screen.Renderer;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.sound.SoundEngine;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

/**
 * The screen for the Hunting minigame
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.1
 */
public class HuntingGameScreen extends Screen {
    private final HuntingConfig config;
    private boolean running;
    private final ImInt score = new ImInt(0);
    private int completedShots;
    private int totalShots;

    /**
     * Creates an instance of the Hunting minigame
     * @param config The config for the Hunting minigame
     */
    public HuntingGameScreen(HuntingConfig config) {
        super(config.id());
        this.config = config;
        reset();
    }

    @Override
    public void setVisible() {
        super.setVisible();
        // Do not allow resizing games
        RenderUtils.setWindowResizable(false);
        // TODO Improve this
        TrailApplication.setFPSLimit(30);
    }

    @Override
    public void render() {
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        if(running) {
            ImGui.setCursorPos(RenderUtils.getCursorRelative(0.01F, 0.01F));
            ImGui.image((long) Registry.getAsset(Registry.AssetType.ASSET, "grass"), RenderUtils.getCursorRelative(0.98F, 0.98F));
            this.completedShots += GameAnimal.tickAnimals(this.score, this.config);

            String remainingAmmo = String.valueOf(Game.getInstance().getInventory().countOfType(ItemStack.ItemType.AMMUNITION));
            RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("hunting.ammo", remainingAmmo)), RenderUtils.getCursorRelative(0, 0.84F).y);
            ImGui.text(Translations.getTranslatedText("hunting.ammo", remainingAmmo));
            String scoreStr = String.valueOf(this.score.get());
            RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("hunting.score", scoreStr)), RenderUtils.getCursorRelative(0, 0.88F).y);
            ImGui.text(Translations.getTranslatedText("hunting.score", scoreStr));
            ImVec2 buttonSize = RenderUtils.getCursorRelative(0.3F, 0.05F);
            RenderUtils.centeredHorizontal(buttonSize.x, RenderUtils.getCursorRelative(0, 0.93F).y);
            if (ImGui.button(Translations.getTranslatedText("hunting.end"), buttonSize)) {
                SoundEngine.loadSFX(Audio.CLICK_2);
                this.running = false;
                GameAnimal.killAllAnimals();
                Format.clearFontSize();
                return;
            }

            boolean endHovered = ImGui.isItemHovered();
            if (endHovered) {
                TrailApplication.getRenderer().setCursorType(Renderer.CursorType.FINGER);
            } else {
                TrailApplication.getRenderer().setCursorType(Renderer.CursorType.RIFLE);
            }

            if (Game.getInstance().getInventory().countOfType(ItemStack.ItemType.AMMUNITION) <= 0) {
                this.running = false;
            }

            if (!endHovered && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
                SoundEngine.loadSFX(Audio.SHOOT);
                Game.getInstance().getInventory().removeOfType(ItemStack.ItemType.AMMUNITION, 1.0, 0.0);
                this.totalShots++;
            }
        } else {
            TrailApplication.getRenderer().setCursorType(Renderer.CursorType.WAGON);
            RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("hunting.summary")), RenderUtils.getCursorRelative(0, 0.1F).y);
            ImGui.text(Translations.getTranslatedText("hunting.summary"));
            RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("hunting.shotsMade", String.valueOf(this.completedShots))), RenderUtils.getCursorRelative(0, 0.2F).y);
            ImGui.text(Translations.getTranslatedText("hunting.shotsMade", String.valueOf(this.completedShots)));
            RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("hunting.shotsTotal", String.valueOf(this.totalShots))), RenderUtils.getCursorRelative(0, 0.3F).y);
            ImGui.text(Translations.getTranslatedText("hunting.shotsTotal", String.valueOf(this.totalShots)));
            float accuracy;
            if (this.totalShots == 0) {
                accuracy = 0;
            } else {
                accuracy = ((float) this.completedShots / this.totalShots) * 100;
            }
            String accuracyStr = String.format("%.2f", accuracy) + "%";
            RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("hunting.accuracy", accuracyStr)), RenderUtils.getCursorRelative(0, 0.4F).y);
            ImGui.text(Translations.getTranslatedText("hunting.accuracy", accuracyStr));
            RenderUtils.centeredHorizontal(ImGui.calcTextSizeX(Translations.getTranslatedText("hunting.score", String.valueOf(this.score.get()))), RenderUtils.getCursorRelative(0, 0.5F).y);
            ImGui.text(Translations.getTranslatedText("hunting.score", String.valueOf(this.score.get())));
            RenderUtils.closeButtonCentered(this);
        }
        Format.clearFontSize();
    }

    private void reset() {
        this.running = true;
        this.completedShots = 0;
        this.totalShots = 0;
        this.score.set(0);
        GameAnimal.setUp(config);
    }

    @Override
    public void close() {
        reset();
        super.close();
        RenderUtils.setWindowResizable(true);
        // TODO Improve this
        TrailApplication.setFPSLimit(TrailApplication.getRenderer().getFPS());
    }
}
