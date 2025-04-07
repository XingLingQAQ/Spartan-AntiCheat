package ai.idealistic.spartan.abstraction.configuration.implementation;

import ai.idealistic.spartan.abstraction.configuration.ConfigurationBuilder;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.functionality.moderation.DetectionNotifications;
import ai.idealistic.spartan.functionality.server.PluginBase;

import java.util.Collection;

public class Settings extends ConfigurationBuilder {

    public Settings() {
        super("settings");
    }

    public static final String crossServerNotificationsName = "Important.server_name";

    @Override
    public void create() {
        addOption("Logs.log_file", true);
        addOption("Logs.log_console", true);

        addOption("Notifications.individual_only_notifications", false);
        addOption("Notifications.enable_notifications_on_login", true);
        addOption("Notifications.awareness_notifications", true);
        addOption("Notifications.message_clickable_command", "/teleport {player}");

        addOption("Important.op_bypass", false);
        addOption(crossServerNotificationsName, "");
        addOption("Important.bedrock_client_permission", false);
        addOption("Important.bedrock_player_prefix", ".");
        addOption("Important.enable_developer_api", true);
        addOption("Important.enable_npc", false);
        addOption("Important.enable_watermark", true);

        addOption("Detections.ground_teleport_on_detection", true);
        addOption("Detections.damage_on_detection", false);

        addOption("Discord.webhook_hex_color", "4caf50");
        addOption("Discord.checks_webhook_url", "");
    }

    public void runOnLogin(PlayerProtocol p) {
        if (getBoolean("Notifications.enable_notifications_on_login")
                && DetectionNotifications.hasPermission(p)
                && !DetectionNotifications.isEnabled(p)) {
            DetectionNotifications.set(p, DetectionNotifications.defaultFrequency);
        }
    }

    public void runOnLogin() {
        Collection<PlayerProtocol> protocols = PluginBase.getProtocols();

        if (!protocols.isEmpty()) {
            for (PlayerProtocol protocol : protocols) {
                runOnLogin(protocol);
            }
        }
    }

}
