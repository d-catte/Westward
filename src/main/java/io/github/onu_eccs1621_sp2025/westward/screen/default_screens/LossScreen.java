package io.github.onu_eccs1621_sp2025.westward.screen.default_screens;

import imgui.ImGui;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

/**
 * Displayed when all the Members die
 * @author Dylan Catte
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public class LossScreen extends Screen {
    /**
     * Displayed when all the Members die
     */
    public LossScreen() {
        super("loss");
    }

    @Override
    public void render() {
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        RenderUtils.textWrapScaling();
        ImGui.textWrapped(Translations.getTranslatedText("game.loss"));
        ImGui.popTextWrapPos();
        Format.clearFontSize();
        RenderUtils.closeButtonCentered(this);
    }
}
