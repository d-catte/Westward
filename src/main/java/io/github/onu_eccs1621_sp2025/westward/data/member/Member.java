package io.github.onu_eccs1621_sp2025.westward.data.member;

import io.github.onu_eccs1621_sp2025.westward.data.StatusContainer;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.screen.Dashboard;
import io.github.onu_eccs1621_sp2025.westward.screen.Renderer;
import io.github.onu_eccs1621_sp2025.westward.utils.Config;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Contains all the data for a member of the party.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @version 1.0
 * @since 1.0.0 Alpha 1
 */
public class Member {
    private final String name;
    private final List<StatusContainer> statuses = new ArrayList<>();
    private final Role role;
    private final Gender gender;
    private int health;
    private int maxHealth;

    /**
     * Used to load Members from JSONs
     * @param name Name of the member of the party
     * @param role What role the party member is
     * @param gender The gender of the member
     * @param health The health of the member
     * @param maxHealth The max health the member can have
     */
    public Member(String name, Role role, Gender gender, int health, int maxHealth) {
        this.name = name;
        this.role = role;
        this.gender = gender;
        this.health = health;
        this.maxHealth = maxHealth;
    }

    /**
     * Creates the minimum amount of data required for a Member
     * @param name Name of the member of the party
     * @param role What role the party member is
     * @param gender The gender of the member
     */
    public Member(String name, Role role, Gender gender) {
        this(name, role, gender, 100 + role.bonusHealth(), 100 + role.bonusHealth());
    }

    /**
     * Gets the name of the Member
     * @return Member's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the Role of the Member
     * @return Member's Role
     */
    public Role getRole() {
        return this.role;
    }

    /**
     * Gets the Gender of the Member
     * @return Member's Gender
     */
    public Gender getGender() {
        return this.gender;
    }

    /**
     * Get the health of the Member
     * @return Member's health
     */
    public int getHealth() {
        return this.health;
    }

    /**
     * Changes the Member's health by an amount
     * @param by The amount to add to the health
     */
    public void modifyHealth(int by) {
        this.health = Math.min(this.maxHealth, this.health + by);
        if (this.health <= 0) {
            this.onDeath();
        }
    }

    /**
     * Gets the maximum amount of health the Member can have
     * @return Max health of the Member
     */
    public int getMaxHealth() {
        return this.maxHealth;
    }

    /**
     * Get the status of the party member
     * @return return the status of the party member
     */
    public List<StatusContainer> getStatuses() {
        return statuses;
    }

    /**
     * Gets a list of only healable statuses
     * @return List of StatusContainer that are healable
     */
    public List<StatusContainer> getHealableStatuses() {
        return this.statuses.stream().filter(StatusContainer::isHealable).toList();
    }

    /**
     * Updates the party member's status
     * @param status What status is being applied or taken away from the party member
     * @return If the member died
     */
    public boolean addStatus(StatusContainer status) {
        if (this.statuses.contains(status)) {
            StatusContainer statusInstance = this.statuses.get(this.statuses.indexOf(status));
            if (statusInstance.increaseLevel()) {
                Renderer.addConsequence( "notification.deathFromIllness", this.name, status.getName());
                this.onDeath();
                return true;
            }
        } else {
            this.statuses.add(status);
        }
        return false;
    }

    /**
     * Updates the party member's status
     * @param status What status is being applied or taken away from the party member
     * @param iterator Iterator from the Members list
     * @return If the member died
     */
    public boolean addStatus(StatusContainer status, ListIterator<Member> iterator) {
        if (this.statuses.contains(status)) {
            StatusContainer statusInstance = this.statuses.get(this.statuses.indexOf(status));
            if (statusInstance.increaseLevel()) {
                this.onDeath(iterator);
                return true;
            }
        } else {
            this.statuses.add(status);
        }
        return false;
    }

    /**
     * Runs the final code when a Member dies
     */
    public void onDeath() {
        checkMemberBounds();
        Game.getInstance().getMembers().remove(this);
    }

    private void onDeath(ListIterator<Member> iterator) {
        Renderer.addConsequence( "notification.deathWithoutCause", this.name);
        checkMemberBounds();
        iterator.remove();
    }

    private void checkMemberBounds() {
        if (Dashboard.getMemberIndex().get() >= Game.getInstance().getMembers().size() - 1) {
            Dashboard.getMemberIndex().set(Math.max(0, Game.getInstance().getMembers().size() - 2));
        }
    }

    /**
     * Ticks the member's status effects
     */
    public void tick(ListIterator<Member> iterator) {
        for (StatusContainer status : this.statuses) {
            if (status.chance()) {
                this.health -= 10;
            }
            if (this.health <= 0) {
                this.onDeath(iterator);
                return;
            }
        }
    }

    /**
     * Creates a randomized member
     * @return Randomized gender, role, and name of a member
     */
    public static Member randomize() {
        Gender gender = getRandomGender();
        Role role = getRandomRole(gender);
        String name = getRandomName(gender);
        return new Member(name, role, gender);
    }

    private static Role getRandomRole(Gender gender) {
        String[] roles = Role.getRoleIds(gender);
        int i = ThreadLocalRandom.current().nextInt(0, roles.length);
        return (Role) Registry.getAsset(Registry.AssetType.ROLE, roles[i]);
    }

    private static Gender getRandomGender() {
        return ThreadLocalRandom.current().nextBoolean() ? Gender.MALE : Gender.FEMALE;
    }

    private static String getRandomName(Gender gender) {
        String[] names = gender == Gender.MALE ? Config.getConfig().getDefaultMaleNames() : Config.getConfig().getDefaultFemaleNames();
        int i = ThreadLocalRandom.current().nextInt(0, names.length);
        String firstName = names[i];
        return getRandomFinalName(firstName);
    }

    private static String getRandomFinalName(String firstName) {
        int character = ThreadLocalRandom.current().nextInt(0, 26);
        char lastInitial = (char) ('A' + character);
        String finalName = firstName + " " + lastInitial + ".";
        if (Renderer.containsName(finalName)) {
            return getRandomFinalName(firstName);
        }
        return finalName;
    }
}
