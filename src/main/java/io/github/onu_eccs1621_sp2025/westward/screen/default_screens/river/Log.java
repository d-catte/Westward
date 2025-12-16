package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.river;

import imgui.ImGui;
import imgui.ImVec2;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;

/**
 * Contains data for the floating logs in River Crossing game
 */
public class Log {
    private final ImVec2 position = new ImVec2();
    private final float velocityRelative;
    private final float relativeY;

    /**
     * Creates a Log for the River Crossing game
     * @param yPos The y position of the log
     * @param velocityRelative The percent of the screen the log should cover each frame
     * @param centerX The center of the log
     * @param relativeY The relative y percentage
     */
    public Log(float yPos, float velocityRelative, float centerX, float relativeY) {
        position.set(centerX, yPos);
        this.velocityRelative = velocityRelative;
        this.relativeY = relativeY;
    }

    /**
     * Ticks the movement of the log and rendering
     * @param logSize The size (x, y) of the log
     * @param centerX The center of the map
     * @param offsetX The horizontal radius of the map
     */
    public void tickMovement(ImVec2 logSize, float centerX, float offsetX) {
        float movementAmount = RenderUtils.getCursorRelative(this.velocityRelative, 0.0F).x;
        this.position.set(this.position.plus(movementAmount, 0.0F));
        this.position.set(this.position.x, RenderUtils.getCursorRelative(0.0F, this.relativeY).y);
        if (centerX + offsetX + logSize.x <= this.position.x) {
            this.position.set(centerX - logSize.x, this.position.y);
        }
        ImGui.setCursorPos(position.x - logSize.x / 2F, position.y - logSize.y / 2F);
        ImGui.image((long) Registry.getAsset(Registry.AssetType.ASSET, "log"), logSize);
    }

    /**
     * If the wagon collided with a log
     * @param wagonPos The wagon position
     * @param wagonSize The size of the wagon
     * @param logSize The size of the log
     * @return If the wagon is intersecting with the log hitbox
     */
    public boolean collidedWith(ImVec2 wagonPos, ImVec2 wagonSize, ImVec2 logSize) {
        float halfLogWidth = logSize.x / 2F;
        float halfLogHeight = logSize.y / 2F;
        float halfWagonWidth = wagonSize.x / 2F;
        float halfWagonHeight = wagonSize.y / 2F;

        float dx = Math.abs(wagonPos.x - this.position.x);
        float dy = Math.abs(wagonPos.y - this.position.y);

        return dx <= halfLogWidth + halfWagonWidth && dy <= halfLogHeight + halfWagonHeight;
    }
}
