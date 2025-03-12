package ai.idealistic.vacan.compatibility.necessary;

import ai.idealistic.vacan.abstraction.Enums;
import ai.idealistic.vacan.compatibility.necessary.protocollib.ProtocolLib;
import ai.idealistic.vacan.functionality.server.Config;
import ai.idealistic.vacan.functionality.server.Permissions;
import org.bukkit.entity.Player;

public class BedrockCompatibility {

    public static boolean isPlayer(Player p) {
        return ProtocolSupport.isBedrockPlayer(p)
                
                || !ProtocolLib.isTemporary(p)
                && Floodgate.isBedrockPlayer(p.getUniqueId(), p.getName())

                || Config.settings.getBoolean("Important.bedrock_client_permission")
                && Permissions.onlyHas(p, Enums.Permission.BEDROCK);
    }

}
