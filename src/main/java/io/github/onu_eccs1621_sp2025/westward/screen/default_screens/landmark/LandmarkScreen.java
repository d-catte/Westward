package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.landmark;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import io.github.onu_eccs1621_sp2025.westward.data.Audio;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.utils.WebUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.sound.SoundEngine;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

/**
 * The screen that displays landmarks when they are reached.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public class LandmarkScreen extends Screen {
    private final String image;
    private final String translationKey;
    private final String learnMoreUrl;

    /**
     * Creates a Screen for a landmark.
     * @param data The data that contains info about the image and translations
     */
    public LandmarkScreen(LandmarkScreenData data) {
        super(data.id());
        this.image = data.imagePath();
        this.translationKey = data.translationKey();
        this.learnMoreUrl = data.learnMoreUrl();
    }

    @Override
    public void render() {
        // Image
        ImVec2 imagePos = RenderUtils.getCursorRelative(0, 0);
        ImVec2 imageScale = RenderUtils.getItemScaleMaintainAspectRatio(1.1F, 0.7F);
        RenderUtils.centeredHorizontal(imageScale.x, imagePos.y);
        ImGui.image((Long) Registry.getAsset(Registry.AssetType.ASSET, this.image), imageScale);
        ImVec2 separatorPos = RenderUtils.getCursorRelative(0, 0.71F);
        ImGui.setCursorPosY(separatorPos.y);
        ImGui.separator();

        // Description
        ImVec2 descriptionSize = RenderUtils.getCursorRelative(0.95F, 0.17F);
        ImVec2 descriptionPosition = RenderUtils.getCursorRelative(0.05F, 0.73F);
        RenderUtils.centeredHorizontal(descriptionSize.x, descriptionPosition.y);
        ImGui.beginChild("landmarkDescription", descriptionSize, false, ImGuiWindowFlags.HorizontalScrollbar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        RenderUtils.textWrapScaling(958);
        ImGui.textWrapped(Translations.getTranslatedText(this.translationKey));
        ImGui.popTextWrapPos();
        Format.clearFontSize();
        ImGui.endChild();

        // Learn More Button
        if (this.learnMoreUrl != null) {
            ImVec2 learnMoreButtonSize = RenderUtils.getCursorRelative(0.3F, 0.05F);
            ImVec2 learnMoreButtonPos = RenderUtils.getCursorRelative(0.55F, 0.9F);
            ImGui.setCursorPos(learnMoreButtonPos);
            Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
            if (ImGui.button(Translations.getTranslatedText("landmark.learn"), learnMoreButtonSize)) {
                SoundEngine.loadSFX(Audio.CLICK_2);
                WebUtils.openWebPage(this.learnMoreUrl);
            }
            Format.clearFontSize();

            // Close Button
            ImVec2 closeButtonPos = RenderUtils.getCursorRelative(0.15F, 0.9F);
            ImVec2 closeButtonSize = RenderUtils.getCursorRelative(0.3F, 0.05F);
            RenderUtils.closeButton(this, closeButtonPos, closeButtonSize);
        } else {
            // Centered Close Button
            RenderUtils.closeButtonCentered(this);
        }
    }

    @Override
    public void setVisible() {
        super.setVisible();

        SoundEngine.loadRandomSong(true);
    }

    @Override
    public void close() {
        super.close();
        SoundEngine.stopMusic();
    }
}
