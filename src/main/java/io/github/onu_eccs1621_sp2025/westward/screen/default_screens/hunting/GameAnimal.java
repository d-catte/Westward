package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.hunting;

import com.google.gson.annotations.Expose;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImInt;
import io.github.onu_eccs1621_sp2025.westward.data.loot_table.LootTable;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.game.ItemStack;
import io.github.onu_eccs1621_sp2025.westward.utils.math.IntegerRange;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Contains the data for the animals shown in the HuntingGame
 * @author Dylan Catte
 * @since 1.0.0 Beta 1
 * @version 1.0
 */
public class GameAnimal {
    @Expose
    private final String animalId;
    @Expose
    private final IntegerRange movementSpeedRange;
    @Expose
    private final LootTable drop;
    @Expose
    private int health;
    @Expose
    private final int score;
    @Expose
    private final int maxCount;
    @Expose
    private final float size;
    private final ImVec2 pos;
    private final ImVec2 targetPos;
    private final float movementSpeed;
    private static final HashMap<String, List<GameAnimal>> ANIMALS = new HashMap<>();
    private MovementDirection direction = MovementDirection.UP;


    private GameAnimal(String animalId, IntegerRange range, LootTable drop, int health, int score, int maxCount, float size) {
        this.animalId = animalId;
        this.movementSpeedRange = range;
        this.drop = drop;
        this.maxCount = maxCount;
        this.health = health;
        this.score = score;
        this.movementSpeed = ThreadLocalRandom.current().nextInt(movementSpeedRange.min(), movementSpeedRange.max() + 1);
        pos = getRandomPos();
        targetPos = getRandomPos();
        this.size = size;
    }

    private boolean clickedOn(ImVec2 mouseClickPos) {
        ImVec2 size = getSize();
        float halfWidth = size.x / 2F;
        float halfHeight = size.y / 2F;
        float dx = Math.abs(mouseClickPos.x - pos.x);
        float dy = Math.abs(mouseClickPos.y - pos.y);

        return dx <= halfWidth && dy <= halfHeight;
    }

    private ImVec2 getSize() {
        return RenderUtils.getItemScaleMaintainAspectRatio(this.size, this.size);
    }

    private static ImVec2 getArenaSize() {
        return RenderUtils.getCursorRelative(0.75F, 0.75F);
    }

    private void tickMovement() {
        float deltaX = targetPos.x - pos.x;
        float deltaY = targetPos.y - pos.y;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance > 0) {
            if (distance <= movementSpeed) {
                pos.x = targetPos.x;
                pos.y = targetPos.y;
                getNewTargetPos();
            } else {
                float moveX = (deltaX / distance) * movementSpeed;
                float moveY = (deltaY / distance) * movementSpeed;

                pos.x += moveX;
                pos.y += moveY;

                // Ensure the animal stays within the arena boundaries
                ImVec2 arenaSize = getArenaSize();
                pos.x = Math.max(0, Math.min(pos.x, arenaSize.x));
                pos.y = Math.max(0, Math.min(pos.y, arenaSize.y));

                // Update direction based on movement
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    direction = deltaX > 0 ? MovementDirection.RIGHT : MovementDirection.LEFT;
                } else {
                    direction = deltaY > 0 ? MovementDirection.DOWN : MovementDirection.UP;
                }
            }
        }
    }

    private void render(int index) {
        ImVec2 imageScale = getSize();
        ImGui.setCursorPos(this.pos.x - imageScale.x / 2.0F, this.pos.y - imageScale.y / 2.0F);
        direction.createImage(index, this);
    }

    private ImVec2 getRandomPos() {
        float x = ThreadLocalRandom.current().nextInt(0, (int) getArenaSize().x);
        float y = ThreadLocalRandom.current().nextInt(0, (int) getArenaSize().y);
        return new ImVec2(x, y);
    }

    private void getNewTargetPos() {
        this.targetPos.set(getRandomPos());
    }

    /**
     * Creates empty Lists for the animals to be placed in
     * @param config The config for the game
     */
    public static void setUp(HuntingConfig config) {
        for (GameAnimal animal : config.animals()) {
            ANIMALS.put(animal.animalId, new ArrayList<>());
        }
    }

    private static void spawnAnimal(HuntingConfig config) {
        for (GameAnimal animal : config.animals()) {
            if (ANIMALS.isEmpty() || (ANIMALS.get(animal.animalId).size() < animal.maxCount && ThreadLocalRandom.current().nextFloat() < 0.1F)) {
                ANIMALS.get(animal.animalId).add(new GameAnimal(animal.animalId, animal.movementSpeedRange, animal.drop, animal.health, animal.score, animal.maxCount, animal.size));
            }
        }
    }

    /**
     * Ticks all animals' position, target position, velocity, and rendering. It also attempts to spawn new animals if needed.
     * @param score The current score in the game
     * @param config The currently used HuntingConfig
     * @return The new score for the game
     */
    public static int tickAnimals(ImInt score, HuntingConfig config) {
        int shotsMade = 0;
        for (List<GameAnimal> animalList : ANIMALS.values()) {
            Iterator<GameAnimal> animalIterator = animalList.listIterator();
            int i = 0;
            while (animalIterator.hasNext()) {
                GameAnimal animal = animalIterator.next();
                animal.render(i);
                i++;
                if (ImGui.isMouseClicked(ImGuiMouseButton.Left) && animal.clickedOn(ImGui.getMousePos())) {
                    shotsMade++;
                    if (--animal.health <= 0) {
                        animal.drop.run().forEach(itemStack -> Game.getInstance().getInventory().addItemStack((ItemStack) itemStack));
                        score.set(score.get() + animal.score);
                        animalIterator.remove();
                        continue;
                    }
                }
                animal.tickMovement();
            }
        }
        GameAnimal.spawnAnimal(config);
        return shotsMade;
    }

    /**
     * Removes all animals from the game
     */
    public static void killAllAnimals() {
        ANIMALS.clear();
    }

    enum MovementDirection {
        UP,
        DOWN,
        LEFT,
        RIGHT;

        private void createImage(int index, GameAnimal animal) {
            ImGui.pushID(animal.animalId + index);
            switch (this) {
                case UP, LEFT -> ImGui.image((long) Registry.getAsset(Registry.AssetType.ASSET, animal.animalId + "-left"), animal.getSize());
                case DOWN, RIGHT -> ImGui.image((long) Registry.getAsset(Registry.AssetType.ASSET, animal.animalId + "-right"), animal.getSize());
            }
            ImGui.popID();
        }
    }
}
