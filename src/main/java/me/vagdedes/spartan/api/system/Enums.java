package me.vagdedes.spartan.api.system;

import ai.idealistic.vacan.abstraction.check.CheckEnums;
import lombok.Getter;

public class Enums {

    // API Use
    // Should have used capital letters but won't change them now so to not break the dozen APIs who use these enums
    public enum HackType {
        XRay(CheckEnums.HackType.X_RAY),
        Exploits(CheckEnums.HackType.EXPLOITS),
        NoSwing(CheckEnums.HackType.NO_SWING),
        IrregularMovements(CheckEnums.HackType.IRREGULAR_MOVEMENTS),
        ImpossibleActions(CheckEnums.HackType.IMPOSSIBLE_ACTIONS),
        AutoRespawn(CheckEnums.HackType.AUTO_RESPAWN),
        InventoryClicks(CheckEnums.HackType.INVENTORY_CLICKS),
        Criticals(CheckEnums.HackType.CRITICALS),
        GhostHand(CheckEnums.HackType.GHOST_HAND),
        BlockReach(CheckEnums.HackType.BLOCK_REACH),
        FastBow(CheckEnums.HackType.FAST_BOW),
        FastClicks(CheckEnums.HackType.FAST_CLICKS),
        FastHeal(CheckEnums.HackType.FAST_HEAL),
        ImpossibleInventory(CheckEnums.HackType.IMPOSSIBLE_INVENTORY),
        HitReach(CheckEnums.HackType.HIT_REACH),
        FastBreak(CheckEnums.HackType.FAST_BREAK),
        FastPlace(CheckEnums.HackType.FAST_PLACE),
        MorePackets(CheckEnums.HackType.MORE_PACKETS),
        FastEat(CheckEnums.HackType.FAST_EAT),
        Velocity(CheckEnums.HackType.VELOCITY),
        KillAura(CheckEnums.HackType.KILL_AURA),
        ;

        @Getter
        private final CheckEnums.HackType hackType;

        HackType(CheckEnums.HackType hackType) {
            this.hackType = hackType;
        }
    }

    // API Use
    public enum Permission {
        CONDITION(ai.idealistic.vacan.api.Permission.CONDITION),
        WAVE(ai.idealistic.vacan.api.Permission.WAVE),
        RECONNECT(ai.idealistic.vacan.api.Permission.RECONNECT),
        ADMIN(ai.idealistic.vacan.api.Permission.ADMIN),
        RELOAD(ai.idealistic.vacan.api.Permission.RELOAD),
        KICK(ai.idealistic.vacan.api.Permission.KICK),
        BYPASS(ai.idealistic.vacan.api.Permission.BYPASS),
        MANAGE(ai.idealistic.vacan.api.Permission.MANAGE),
        INFO(ai.idealistic.vacan.api.Permission.INFO),
        CHAT_PROTECTION(ai.idealistic.vacan.api.Permission.CHAT_PROTECTION),
        WARN(ai.idealistic.vacan.api.Permission.WARN),
        USE_BYPASS(ai.idealistic.vacan.api.Permission.USE_BYPASS),
        NOTIFICATIONS(ai.idealistic.vacan.api.Permission.NOTIFICATIONS),
        BEDROCK(ai.idealistic.vacan.api.Permission.BEDROCK);

        @Getter
        private final ai.idealistic.vacan.api.Permission permission;

        Permission(ai.idealistic.vacan.api.Permission permission) {
            this.permission = permission;
        }
    }

    // API Use
    public enum ToggleAction {
        ENABLE(ai.idealistic.vacan.api.ToggleAction.ENABLE),
        DISABLE(ai.idealistic.vacan.api.ToggleAction.DISABLE);

        @Getter
        private final ai.idealistic.vacan.api.ToggleAction toggleAction;

        ToggleAction(ai.idealistic.vacan.api.ToggleAction toggleAction) {
            this.toggleAction = toggleAction;
        }
    }

}
