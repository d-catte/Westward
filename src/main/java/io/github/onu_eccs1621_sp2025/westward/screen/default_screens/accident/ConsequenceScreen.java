package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.accident;

import imgui.ImGui;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Shows more details from an Event's results
 * @author Dylan Catte
 * @since 1.0.0 Alpha 1
 * @version 1.1
 */
public class ConsequenceScreen extends Screen {
    private final Deque<String> dataQueue = new ArrayDeque<>();

    /**
     * Shows more details from an Event's results
     */
    public ConsequenceScreen() {
        super("consequence");
    }

    /**
     * Sets the <b>post-translated</b> text for the screen.
     * @param text Post translated text
     */
    public void setData(String text) {
        this.dataQueue.add(text);
    }

    @Override
    public void render() {
        ImGui.pushTextWrapPos(1000);
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        ImGui.textWrapped(this.dataQueue.peek());
        Format.clearFontSize();
        ImGui.popTextWrapPos();
        RenderUtils.closeButtonCentered(this);
    }

    @Override
    public void close() {
        super.close();
        dataQueue.poll();
    }
}
