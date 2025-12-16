package io.github.onu_eccs1621_sp2025.westward.screen.default_screens;

import imgui.ImGui;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

/**
 * Gives background about the trail when starting a game
 * @author Dylan Catte
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public class IntroScreen extends Screen {

    /**
     * Gives background about the trail when starting a game
     */
    public IntroScreen() {
        super("intro");
    }

    @Override
    public void render() {
        RenderUtils.textWrapScaling();
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        ImGui.textWrapped(Translations.getTranslatedText(
                "intro.description",
                Game.getInstance().getStartingTrail(),
                Game.getInstance().getDate().toString(),
                String.format("%.2f", Game.getInstance().getMoney())
        ));
        ImGui.popTextWrapPos();
        Format.clearFontSize();
        RenderUtils.closeButtonCentered(this);
    }
}
