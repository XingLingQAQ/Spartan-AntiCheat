package ai.idealistic.spartan.functionality.connection;

import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.functionality.moderation.AwarenessNotifications;
import ai.idealistic.spartan.functionality.server.Permissions;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.utils.java.RequestUtils;
import ai.idealistic.spartan.utils.java.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class JarVerification {

    static final String website = "aHR0cHM6Ly93d3cudmFnZGVkZXMuY29tL21pbmVjcmFmdC9jbG91ZC8=";
    private static boolean valid = true;

    public static void run(Plugin plugin) {
        if (!IDs.enabled) {
            PluginBase.connectionThread.execute(() -> {
                int userID = CloudConnections.getUserIdentification();

                if (userID <= 0) {
                    valid = false;
                }
            });
        }

        if (IDs.enabled) {
            if (isValid(plugin)) {
                PluginAddons.refresh();
            } else {
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        } else {
            PluginAddons.refresh();

            if (!isValid(plugin)) {
                String message = "This version of " + plugin.getName() + " does not have a license."
                        + " If this download is pirated, please consider purchasing the plugin"
                        + " when your server starts making enough money. We also sell on BuiltByBit"
                        + " which supports many payment methods for all countries including yours.";
                List<PlayerProtocol> staff = Permissions.getStaff();

                if (!staff.isEmpty()) {
                    for (PlayerProtocol protocol : staff) {
                        protocol.sendImportantMessage(AwarenessNotifications.getNotification(message));
                    }
                }
                AwarenessNotifications.forcefullySend(message);
            }
        }
    }

    private static boolean isValid(Plugin plugin) {
        boolean b = valid
                && plugin.getDescription().getAuthors().toString().startsWith("[Evangelos Dedes @Vagdedes");

        try {
            String[] results = RequestUtils.get(StringUtils.decodeBase64(website)
                    + "?" + CloudBase.identification() + "&action=add&data=userVerification");

            if (results.length > 0) {
                String line = results[0];

                if (line.equalsIgnoreCase(String.valueOf(false))) {
                    valid = false;
                    return false;
                }
            }
        } catch (Exception e) {
            if (IDs.canAdvertise()) {
                AwarenessNotifications.forcefullySend(e.getMessage());
            }
        }
        return b;
    }

}