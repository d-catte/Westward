package io.github.onu_eccs1621_sp2025.westward.utils.text;

import imgui.ImGui;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;

/**
 * Utilities for changing font sizes
 * @author Dylan Catte
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public final class Format {
    // Fonts
    /**
     * The largest font size supported (48pt)
     */
    public static final float MAX_FONT_SIZE = 48F;
    /**
     * The font size for screen titles (24pt)
     */
    public static final float TITLE_FONT_SIZE = 24F / MAX_FONT_SIZE;
    /**
     * The font size for screen descriptions (18pt)
     */
    public static final float DESCRIPTION_FONT_SIZE = 18F / MAX_FONT_SIZE;
    /**
     * The font size for small menu items (12pt)
     */
    public static final float MENU_FONT_SIZE = 12F / MAX_FONT_SIZE;

    /**
     * Sets the font size multiplier
     * @param size Size Multiplier
     */
    public static void setFontSize(final float size) {
        ImGui.getFont().setScale(size * RenderUtils.getFontScaleFactor());
        ImGui.pushFont(ImGui.getFont());
    }

    /**
     * Resets the font size
     */
    public static void clearFontSize() {
        ImGui.getFont().setScale(RenderUtils.getFontScaleFactor());
        ImGui.popFont();
    }
}
