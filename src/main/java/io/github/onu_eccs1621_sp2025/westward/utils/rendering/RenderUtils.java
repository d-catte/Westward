package io.github.onu_eccs1621_sp2025.westward.utils.rendering;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.app.Window;
import io.github.onu_eccs1621_sp2025.westward.data.Audio;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.utils.DebugLogger;
import io.github.onu_eccs1621_sp2025.westward.utils.sound.SoundEngine;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.system.MemoryUtil.memAllocInt;

/**
 * Contains utilities for the ImGUI renderer.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.2
 */
public final class RenderUtils {
    /**
     * Cached empty String to reduce unnecessary allocations
     */
    public static final String EMPTY_STR = "";
    /**
     * The starting Window height
     */
    public static final int DEFAULT_WINDOW_HEIGHT = 800;
    /**
     * The starting Window width
     */
    public static final int DEFAULT_WINDOW_WIDTH = 1000;
    /**
     * The OS window handle id
     */
    private static long windowHandle;

    /**
     * Gets the position the cursor to the relative (%) location specified based on the window size.<p>
     * It can also be used to get the relative scale of an object
     * @param relativeX The relative x percentage of the object
     * @param relativeY The relative y percentage of the object
     * @return A 2D vector containing the x and y coordinates for the object
     */
    public static ImVec2 getCursorRelative(final float relativeX, final float relativeY) {
        final ImVec2 vec2 = ImGui.getIO().getDisplaySize();
        return vec2.times(relativeX, relativeY);
    }

    /**
     * Gets the coordinates of a scaled object based on the screen size while maintaining the same aspect ratio
     * @param relativeX The relative x percentage of the object
     * @param relativeY The relative y percentage of the object
     * @return A 2D vector containing the x and y coordinates for the object
     */
    public static ImVec2 getItemScaleMaintainAspectRatio(final float relativeX, final float relativeY) {
        final ImVec2 displaySize = ImGui.getIO().getDisplaySize();
        final float aspectRatio = relativeX / relativeY;

        float width = displaySize.x * relativeX;
        float height = width / aspectRatio;

        if (height > displaySize.y * relativeY) {
            height = displaySize.y * relativeY;
            width = height * aspectRatio;
        }

        return new ImVec2(width, height);
    }

    /**
     * Sets the center position of the screen for an object
     * @param width Width of the object
     * @param y Y coordinate
     */
    public static void centeredHorizontal(final float width, final float y) {
        final ImVec2 vec = new ImVec2(ImGui.getIO().getDisplaySize().x / 2 - width / 2, y);
        ImGui.setCursorPos(vec);
    }

    /**
     * Gets how much fonts should be influenced by screen size changes.<p>
     * The code ensures that the font scale factor will never be 0
     * @return The new scale of fonts based on the screen size
     */
    public static float getFontScaleFactor() {
        final ImVec2 vec2 = ImGui.getIO().getDisplaySize();
        final float xInfluence = vec2.x / DEFAULT_WINDOW_WIDTH;
        final float yInfluence = vec2.y / DEFAULT_WINDOW_HEIGHT;
        return Math.max(Math.min(xInfluence, yInfluence), 0.01F);
    }

    /**
     * Sets the specified Window's icon in the taskbar and on the Window itself.
     * This method contains very low-level LWJGL code for grabbing the icon's bytes, creating a buffer,
     * and setting the WindowIcon.
     */
    public static void setIcon() {
        final ByteBuffer icon;
        try {
            icon = ioResourceToByteBuffer("defaultData/asset/cursor.png", 4096);
        } catch (Exception ignored) {
            DebugLogger.warn("Failed to Load Icon");
            return;
        }

        // Allocate buffers
        final IntBuffer w = memAllocInt(1);
        final IntBuffer h = memAllocInt(1);
        final IntBuffer comp = memAllocInt(1);

        // Read pixels from buffers and set icon
        try (GLFWImage.Buffer icons = GLFWImage.malloc(1)) {
            final ByteBuffer pixels = stbi_load_from_memory(icon, w, h, comp, 4);
            icons.position(0).width(w.get(0)).height(h.get(0)).pixels(pixels);
            icons.position(0);
            glfwSetWindowIcon(windowHandle, icons);
            stbi_image_free(pixels);
        }
    }

    private static ByteBuffer resizeBuffer(final ByteBuffer buffer, final int newCapacity) {
        final ByteBuffer newBuffer = createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    /**
     * Reads the icon file and returns a buffer.
     * @param resource the path to the icon.
     * @param bufferSize the initial buffer size.
     * @return The resource as a ByteBuffer.
     * @throws IOException if the resource does not exist.
     */
    public static ByteBuffer ioResourceToByteBuffer(final String resource, final int bufferSize) throws IOException {
        ByteBuffer buffer;

        final Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = createByteBuffer((int)fc.size() + 1);
                while (fc.read(buffer) != -1) {}
            }
        } else {
            try (
                    InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
                    ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = createByteBuffer(bufferSize);

                while (true) {
                    final int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if ( buffer.remaining() == 0 ) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                    }
                }
            }
        }

        buffer.flip();
        return buffer;
    }

    /**
     * Loads a texture from a file path and uploads it to the GPU.
     * @param filePath Path to the image file.
     * @return The texture ID.
     * @throws IOException If the file cannot be read.
     */
    public static int loadTextureFromFile(final String filePath) throws IOException {
        // Ensure OpenGL context is initialized
        if (!GL.getCapabilities().OpenGL20) {
            DebugLogger.error("OpenGL " + GL11.glGetString(GL11.GL_VERSION) + " Has Had An Error: OpenGL 2.0 is not supported on this device");
            throw new IllegalStateException("OpenGL 2.0 is not supported");
        }

        final ByteBuffer imageBuffer;
        try (FileChannel fc = (FileChannel) Files.newByteChannel(Path.of(filePath), StandardOpenOption.READ)) {
            imageBuffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
            while (fc.read(imageBuffer) != -1) {}
        }
        imageBuffer.flip();

        final IntBuffer width = BufferUtils.createIntBuffer(1);
        final IntBuffer height = BufferUtils.createIntBuffer(1);
        final IntBuffer channels = BufferUtils.createIntBuffer(1);

        // Load image data from memory
        final ByteBuffer imageData = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 4);
        if (imageData == null) {
            throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
        }

        // Generate a new OpenGL texture ID
        final int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        // Set texture parameters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        // Upload the texture data
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(0), height.get(0), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageData);

        // Free the image memory
        STBImage.stbi_image_free(imageData);

        return textureId;
    }

    /**
     * Renders the button that closes the current screen.
     * This button is centered on the screen.
     * @param screen Screen that is currently open
     */
    public static void closeButtonCentered(final Screen screen) {
        final ImVec2 closeButtonPos = RenderUtils.getCursorRelative(0, 0.90F);
        final ImVec2 buttonSize = getCursorRelative(0.2F, 0.05F);
        RenderUtils.centeredHorizontal(buttonSize.x, closeButtonPos.y);
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        if (ImGui.button(Translations.getTranslatedText("menu.trailSelection.continue"), buttonSize)) {
            SoundEngine.loadSFX(Audio.CLICK_2);
            screen.close();
        }
        Format.clearFontSize();
    }

    /**
     * Renders the button that closes the current screen
     * @param screen Screen that is currently open
     * @param closeButtonPos The position to place the button
     * @param closeButtonSize The size of the button
     */
    public static void closeButton(final Screen screen, final ImVec2 closeButtonPos, final ImVec2 closeButtonSize) {
        ImGui.setCursorPos(closeButtonPos);
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        if (ImGui.button(Translations.getTranslatedText("menu.trailSelection.continue"), closeButtonSize)) {
            SoundEngine.loadSFX(Audio.CLICK_2);
            screen.close();
        }
        Format.clearFontSize();
    }

    /**
     * Scales the textWrapping with the screen
     */
    public static void textWrapScaling() {
        textWrapScaling(1000);
    }

    /**
     * Scales the textWrapping with the screen
     * @param factor The width of the wrap
     */
    public static void textWrapScaling(final float factor) {
        ImGui.pushTextWrapPos(factor * ImGui.getIO().getDisplaySize().x / DEFAULT_WINDOW_WIDTH);
    }

    /**
     * Shifts the color from green to yellow to red based on the health of a Member
     * @param healthFraction Member's health percentage
     * @return The RGB value to correspond to the health
     */
    public static float[] interpolateColor(final float healthFraction) {
        final float[] color = new float[3];
        final float t;
        if (healthFraction > 0.5) {
            t = (healthFraction - 0.5f) * 2;
            color[0] = 1 - t;
            color[1] = 1;
        } else {
            t = healthFraction * 2;
            color[0] = 1;
            color[1] = t;
        }
        color[2] = 0;
        return color;
    }

    /**
     * Caches the current Window's handle
     * @param handle Window's handle from {@link Window#getHandle()}
     */
    public static void setWindowHandle(final long handle) {
        windowHandle = handle;
    }

    /**
     * Sets the LWJGL window's resizable attribute
     * @param resizable If the window can be resized
     */
    public static void setWindowResizable(final boolean resizable) {
        glfwSetWindowAttrib(windowHandle, GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
    }
}
