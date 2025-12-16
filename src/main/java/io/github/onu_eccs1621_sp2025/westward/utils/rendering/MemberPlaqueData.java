package io.github.onu_eccs1621_sp2025.westward.utils.rendering;

import imgui.type.ImInt;
import imgui.type.ImString;
import io.github.onu_eccs1621_sp2025.westward.data.member.Gender;
import io.github.onu_eccs1621_sp2025.westward.data.member.Member;
import io.github.onu_eccs1621_sp2025.westward.data.member.Role;
import io.github.onu_eccs1621_sp2025.westward.utils.registry.Registry;

/**
 * Contains data for displaying Member stats
 * @param name The name of the Member
 * @param gender The gender index of the Member
 * @param role The role index of the Member
 * @author Dylan Catte
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public record MemberPlaqueData(ImString name, ImInt gender, ImInt role) {

    /**
     * Converts MemberPlaqueData into a Member instance
     * @return Member instance
     */
    public Member getMember() {
        final Role role = (Role) Registry.getAssets(Registry.AssetType.ROLE)[this.role.get()];
        return new Member(this.name.get(), role, this.gender.get() == 0 ? Gender.MALE : Gender.FEMALE);
    }

    /**
     * Ensures that the member data is valid
     * @return if the member data is valid
     */
    public boolean isValid() {
        return this.name.isNotEmpty();
    }
}
