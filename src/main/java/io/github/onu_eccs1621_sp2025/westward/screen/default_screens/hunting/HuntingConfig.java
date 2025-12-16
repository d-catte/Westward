package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.hunting;

import java.util.List;

/**
 * All configuration options for the Hunting minigame
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.1
 * @param id The screen ID for the hunting screen
 * @param animals The types of animals to spawn in the hunting screen
 */
public record HuntingConfig(String id, List<GameAnimal> animals) {
}
