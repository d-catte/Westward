package io.github.onu_eccs1621_sp2025.westward.data.member;

import io.github.onu_eccs1621_sp2025.westward.utils.ShallowClone;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

import java.util.Arrays;

/**
 * Contains all the data for a member role.<p>
 * Roles can add custom abilities/perks to a member.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @version 1.0
 * @since 1.0.0 Alpha 1
 * @param id The identifier for the role (also the translation key for its name)
 * @param preferredGender The preferred gender for the profession (leave blank if none)
 * @param bonusMoney Optional: Additional money from picking the role
 * @param bonusPoints Optional: Additional points awarded at the end from picking the role
 * @param bonusHealth Optional: Additional health from picking the role
 * @param canHealPlayers Optional: If the role can heal players
 */
public record Role(
        String id,
        Gender preferredGender,
        Integer bonusMoney,
        Integer bonusPoints,
        int bonusHealth,
        boolean canHealPlayers) implements ShallowClone<Role> {
    @Override
    public Role shallowClone() {
        return new Role(id, preferredGender, bonusMoney, bonusPoints, bonusHealth, canHealPlayers);
    }

    /**
     * Gets all roles as translated text
     * @param filter The gender filter to be applied (if any)
     * @return Roles as translated text
     */
    public static String[] getRoles(Gender filter) {
        Role[] roles = (Role[]) Registry.getAssets(Registry.AssetType.ROLE);
        String[] rolesTranslated;

        if (filter != null) {
            rolesTranslated = Arrays.stream(roles)
                    .filter(role -> role.preferredGender == null || role.preferredGender() == filter)
                    .map(role -> Translations.getTranslatedText(role.id()))
                    .toArray(String[]::new);
        } else {
            rolesTranslated = Arrays.stream(roles)
                    .map(role -> Translations.getTranslatedText(role.id()))
                    .toArray(String[]::new);
        }

        return rolesTranslated;
    }

    /**
     * Gets all roles' ids
     * @param filter The gender filter to be applied (if any)
     * @return Role ids
     */
    public static String[] getRoleIds(Gender filter) {
        Role[] roles = (Role[]) Registry.getAssets(Registry.AssetType.ROLE);
        String[] rolesFiltered;

        if (filter != null) {
            rolesFiltered = Arrays.stream(roles)
                    .filter(role -> role.preferredGender == null || role.preferredGender() == filter)
                    .map(Role::id)
                    .toArray(String[]::new);
        } else {
            rolesFiltered = Arrays.stream(roles)
                    .map(Role::id)
                    .toArray(String[]::new);
        }

        return rolesFiltered;
    }
}
