package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.travel;

import imgui.ImGui;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

/**
 * The screen where players can monitor their travel.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public class TravelingScreen extends Screen {
    private int milesTravelled;

    /**
     * Creates the traveling screen.
     */
    public TravelingScreen() {
        super("travel");
    }

    /**
     * Sets the number of miles travelled on the travelling screen
     * @param milesTravelled The number of miles travelled
     */
    public void setData(int milesTravelled) {
        this.milesTravelled = milesTravelled;
    }

    @Override
    public void render() {
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        RenderUtils.textWrapScaling();
        ImGui.textWrapped(Translations.getTranslatedText("travelScreen.travel", String.valueOf(this.milesTravelled)));
        ImGui.popTextWrapPos();
        Format.clearFontSize();
        RenderUtils.closeButtonCentered(this);
    }
}
