package ai.idealistic.vacan.functionality.server;

import ai.idealistic.vacan.Register;
import ai.idealistic.vacan.abstraction.check.CheckEnums;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.api.Permission;
import ai.idealistic.vacan.compatibility.necessary.protocollib.ProtocolLib;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Permissions {

    private static final String alternativeAdminKey = Register.command + ".*";
    private static final Permission[] staffPermissions = new Permission[]{
            Permission.WAVE,
            Permission.WARN,
            Permission.ADMIN,
            Permission.KICK,
            Permission.NOTIFICATIONS,
            Permission.USE_BYPASS,
            Permission.MANAGE,
            Permission.INFO,
    };

    public static boolean has(Player p) {
        for (Permission permission : Permission.values()) {
            if (has(p, permission)) {
                return true;
            }
        }
        return false;
    }

    public static boolean has(Player p, Permission permission) {
        if (ProtocolLib.hasPermission(p, permission.getKey())) {
            return true;
        } else {
            return permission != Permission.ADMIN
                    ? ProtocolLib.hasPermission(p, Permission.ADMIN.getKey())
                    || ProtocolLib.hasPermission(p, alternativeAdminKey)
                    : ProtocolLib.hasPermission(p, alternativeAdminKey);
        }
    }

    public static boolean onlyHas(Player p, Permission permission) {
        return ProtocolLib.hasPermission(p, permission.getKey());
    }

    // Separator

    public static boolean isBypassing(Player p, CheckEnums.HackType hackType) {
        if (p.isOp()) {
            return Config.settings.getBoolean("Important.op_bypass");
        } else {
            String key = Permission.BYPASS.getKey();

            if (ProtocolLib.hasPermission(p, key)) {
                return true;
            } else if (hackType != null) {
                return ProtocolLib.hasPermission(p, key + "." + hackType.toString().toLowerCase());
            } else {
                return false;
            }
        }
    }

    // Separator

    public static boolean isStaff(Player player) {
        if (player.isOp()) {
            return true;
        } else {
            for (Permission permission : staffPermissions) {
                if (has(player, permission)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static List<PlayerProtocol> getStaff() {
        Collection<PlayerProtocol> protocols = PluginBase.getProtocols();
        int size = protocols.size();

        if (size > 0) {
            List<PlayerProtocol> array = new ArrayList<>(size);

            for (PlayerProtocol protocol : protocols) {
                if (isStaff(protocol.bukkit())) {
                    array.add(protocol);
                }
            }
            return array;
        }
        return new ArrayList<>(0);
    }

}
