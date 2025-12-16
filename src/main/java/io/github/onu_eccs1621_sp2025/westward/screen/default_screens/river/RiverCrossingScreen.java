package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.river;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiKey;
import io.github.onu_eccs1621_sp2025.westward.TrailApplication;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The screen that displays the river crossing minigame
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public class RiverCrossingScreen extends Screen {
    private final List<Log> logs = new ArrayList<>();
    private  boolean startUp = true;
    private final ImVec2 wagonPos = new ImVec2();
    private boolean crossed;
    private boolean gameOver;

    /**
     * Creates an instance of the river crossing minigame
     */
    public RiverCrossingScreen() {
        super("river");
    }

    @Override
    public void render() {
        ImVec2 backgroundSize = RenderUtils.getItemScaleMaintainAspectRatio(1.25F, 0.98F);
        RenderUtils.centeredHorizontal(backgroundSize.x, RenderUtils.getCursorRelative(0.0F, 0.01F).y);
        ImVec2 centeredCoordinate = ImGui.getCursorPos();
        ImGui.image((long) Registry.getAsset(Registry.AssetType.ASSET, "river"), backgroundSize);
        ImVec2 wagonSize = RenderUtils.getItemScaleMaintainAspectRatio(0.08F, 0.08F);

        if (this.startUp) {
            RenderUtils.centeredHorizontal(wagonSize.x, RenderUtils.getCursorRelative(0.0F, 0.83F).y);
            ImVec2 centerPos = ImGui.getCursorPos();
            this.wagonPos.set(centerPos);
            ImVec2 textPos = RenderUtils.getCursorRelative(0.0F, 0.45F);
            ImGui.setCursorPos(centerPos.x - ImGui.calcTextSizeX(Translations.getTranslatedText("riverCrossing.controls")) / 8F, textPos.y);
            Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
            ImGui.text(Translations.getTranslatedText("riverCrossing.controls"));
            Format.clearFontSize();
        } else {
            if (this.gameOver) {
                endScreen(wagonSize);
                return;
            }

            ImVec2 logSize = RenderUtils.getItemScaleMaintainAspectRatio(0.1F, 0.04F);
            for (Log log : this.logs) {
                log.tickMovement(logSize, centeredCoordinate.x, backgroundSize.x);
                if (log.collidedWith(this.wagonPos, wagonSize, logSize)) {
                    this.gameOver = true;
                }
            }

            ImGui.setCursorPos(this.wagonPos.x - wagonSize.x / 2F, this.wagonPos.y - wagonSize.y / 2F);
            ImGui.image((long) Registry.getAsset(Registry.AssetType.ASSET, "river-wagon"), wagonSize);

            if (this.wagonPos.y <= RenderUtils.getCursorRelative(0.0F, 0.2F).y) {
                this.crossed = true;
                this.gameOver = true;
            }
        }
        wagonMove();
    }

    @Override
    public void setVisible() {
        super.setVisible();
        // TODO Improve this
        TrailApplication.setFPSLimit(30);
        // Do not allow resizing games
        RenderUtils.setWindowResizable(false);
        ImVec2 backgroundSize = RenderUtils.getItemScaleMaintainAspectRatio(1.25F, 0.98F);
        RenderUtils.centeredHorizontal(backgroundSize.x, RenderUtils.getCursorRelative(0.0F, 0.01F).y);
        ImVec2 centeredCoordinate = ImGui.getCursorPos();

        // Set velocities
        float minVelocity = switch (Game.getInstance().getDifficulty()) {
            case 2 -> 0.005F;
            case 3 -> 0.006F;
            default -> 0.004F;
        };
        float maxVelocity = switch (Game.getInstance().getDifficulty()) {
            case 2 -> 0.012F;
            case 3 -> 0.014F;
            default -> 0.01F;
        };

        this.logs.add(new Log(RenderUtils.getCursorRelative(0.0F, 0.30F).y, ThreadLocalRandom.current().nextFloat(minVelocity, maxVelocity), centeredCoordinate.x + ThreadLocalRandom.current().nextFloat(-50.0F, 400.0F), 0.30F));
        this.logs.add(new Log(RenderUtils.getCursorRelative(0.0F, 0.45F).y, ThreadLocalRandom.current().nextFloat(minVelocity, maxVelocity), centeredCoordinate.x + ThreadLocalRandom.current().nextFloat(-50.0F, 400.0F), 0.41F));
        this.logs.add(new Log(RenderUtils.getCursorRelative(0.0F, 0.45F).y, ThreadLocalRandom.current().nextFloat(minVelocity, maxVelocity), centeredCoordinate.x + ThreadLocalRandom.current().nextFloat(-50.0F, 400.0F), 0.47F));
        this.logs.add(new Log(RenderUtils.getCursorRelative(0.0F, 0.58F).y, ThreadLocalRandom.current().nextFloat(minVelocity, maxVelocity), centeredCoordinate.x + ThreadLocalRandom.current().nextFloat(-50.0F, 400.0F), 0.53F));
        this.logs.add(new Log(RenderUtils.getCursorRelative(0.0F, 0.58F).y, ThreadLocalRandom.current().nextFloat(minVelocity, maxVelocity), centeredCoordinate.x + ThreadLocalRandom.current().nextFloat(-50.0F, 400.0F), 0.59F));
    }

    @Override
    public void close() {
        super.close();
        if (crossed) {
            Game.getInstance().modifyCurrentMile(15);
        } else {
            Game.getInstance().modifyCurrentMile(-15);
        }
        this.crossed = false;
        this.startUp = true;
        this.gameOver = false;
        this.logs.clear();
        RenderUtils.setWindowResizable(true);
        // TODO Improve this
        TrailApplication.setFPSLimit(TrailApplication.getRenderer().getFPS());
    }

    private void wagonMove() {
        if (ImGui.isKeyPressed(ImGuiKey.UpArrow)) {
            startUp = false;
            this.wagonPos.set(this.wagonPos.plus(RenderUtils.getCursorRelative(0.0F, -0.007F)));
        } else if (ImGui.isKeyPressed(ImGuiKey.DownArrow)) {
            startUp = false;
            this.wagonPos.set(this.wagonPos.plus(RenderUtils.getCursorRelative(0.0F, 0.007F)));
        } else if (ImGui.isKeyPressed(ImGuiKey.LeftArrow)) {
            startUp = false;
            this.wagonPos.set(this.wagonPos.plus(RenderUtils.getCursorRelative(-0.007F, 0.0F)));
        } else if (ImGui.isKeyPressed(ImGuiKey.RightArrow)) {
            startUp = false;
            this.wagonPos.set(this.wagonPos.plus(RenderUtils.getCursorRelative(0.007F, 0.0F)));
        }
    }

    private void endScreen(ImVec2 wagonSize) {
        RenderUtils.centeredHorizontal(wagonSize.x, RenderUtils.getCursorRelative(0.0F, 0.0F).y);
        ImVec2 centerPos = ImGui.getCursorPos();
        ImVec2 textPos = RenderUtils.getCursorRelative(0.0F, 0.45F);
        if (this.crossed) {
            ImGui.setCursorPos(centerPos.x - ImGui.calcTextSizeX(Translations.getTranslatedText("riverCrossing.success")) / 8F, textPos.y);
            Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
            ImGui.text(Translations.getTranslatedText("riverCrossing.success"));
        } else {
            ImGui.setCursorPos(centerPos.x - ImGui.calcTextSizeX(Translations.getTranslatedText("riverCrossing.fail")) / 8F, textPos.y);
            Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
            ImGui.text(Translations.getTranslatedText("riverCrossing.fail"));
        }
        Format.clearFontSize();
        RenderUtils.closeButtonCentered(this);
    }


}
