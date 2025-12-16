package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.accident;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

/**
 * Screen used to show if the player encounters a peril
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public class PerilScreen extends Screen {
    private final String traslationKey;

    /**
     * Creates a PerilScreen instance
     * @param data The data associated with the screen
     */
    public PerilScreen(PerilScreenData data) {
        super(data.id());
        this.traslationKey = data.translationKey();
    }

    @Override
    public void render() {
        ImVec2 separatorPos = RenderUtils.getCursorRelative(0, 0.60F);
        ImGui.setCursorPosY(separatorPos.y);
        ImGui.separator();
        ImVec2 descriptionSize = RenderUtils.getCursorRelative(1.0F, 0.30F);
        ImVec2 descriptionPosition = RenderUtils.getCursorRelative(0, 0.60F);
        ImGui.setCursorPosY(descriptionPosition.y);
        ImGui.beginChild("landmarkDescription", descriptionSize, false, ImGuiWindowFlags.HorizontalScrollbar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        RenderUtils.textWrapScaling();
        ImGui.textWrapped(Translations.getTranslatedText(this.traslationKey));
        ImGui.popTextWrapPos();
        Format.clearFontSize();
        ImGui.endChild();
        RenderUtils.closeButtonCentered(this);
    }
}
