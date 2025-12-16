package io.github.onu_eccs1621_sp2025.westward.game.event;

import io.github.onu_eccs1621_sp2025.westward.screen.Renderer;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Events are random actions that occur throughout the journey.
 * They can be a member dying, a status being applied, or another random trail event
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.1
 * @param name The name of the event
 * @param screenId The screen identifier to open
 * @param chance The chance that the event is selected randomly
 * @param action The action to perform when the event is executed
 */
public record Event(String name, String screenId, float chance, EventAction action) {

    /**
     * Selects a random event and randomly determines if it should be applied
     */
    public static void selectRandomEvent() {
        String[] events = Registry.getAssetIdentifiers(Registry.AssetType.EVENT);
        int index = ThreadLocalRandom.current().nextInt(0, events.length);
        Event event = (Event) Registry.getAsset(Registry.AssetType.EVENT, events[index]);
        if (ThreadLocalRandom.current().nextFloat() < event.chance) {
            event.run();
        }
    }

    /**
     * Executes the event
     */
    public void run() {
        if (this.action.execute(this.name)) {
            Renderer.RENDER_QUEUE.add(this.screenId);
            this.action.getConsequence().run();
        }
    }
}
