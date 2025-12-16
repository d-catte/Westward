package io.github.onu_eccs1621_sp2025.westward.screen;

/**
 * The foundation for all Screens
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public abstract class Screen {

    /**
     * Constructor for the Screen class
     * @param id The Screen ID
     */
    public Screen(String id) {
        this.id = id;
    }

    boolean visible;
    String id;

    /**
     * Closes the UI
     */
    public void close() {
        this.visible = false;
        Renderer.RENDER_QUEUE.pop();
    }

    /**
     * Returns the ID of the Screen.
     * @return the ID of the Screen
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets whether the Screen should be visible.
     * @return if the Screen should be visible
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Sets the visibility of the Screen to true
     */
    public void setVisible() {
        this.visible = true;
    }

    /**
     * Renders the screen
     */
    public abstract void render();
}
