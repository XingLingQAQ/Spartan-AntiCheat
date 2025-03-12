package ai.idealistic.vacan.functionality.connection;

import ai.idealistic.vacan.Register;
import ai.idealistic.vacan.abstraction.check.Check;
import ai.idealistic.vacan.abstraction.profiling.PlayerProfile;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.moderation.AwarenessNotifications;
import ai.idealistic.vacan.functionality.server.PluginBase;
import ai.idealistic.vacan.functionality.tracking.ResearchEngine;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PluginEdition {

    private static final Check.DataType
            currentType = false
            || IDs.resource.equals("25638")
            || IDs.resource.equals("11196")
            || IDs.resource.equals("350")
            || Bukkit.getMotd().contains("Vacan")
            ? Check.DataType.JAVA
            : Check.DataType.BEDROCK,
            alternativeType = currentType == Check.DataType.JAVA
                    ? Check.DataType.BEDROCK
                    : Check.DataType.JAVA;

    private static long checkTime = 0L;
    public static final String patreonURL = "https://www.patreon.com/VacanAntiCheat";
    private static final int notificationCooldown = 60 * 60;
    private static final String
            type = "{type}",
            product = "{product}",
            versionNotificationMessage = "\n§cHey, just a heads up!"
                    + " You have " + type + " players which cannot be checked by the anti-cheat due to missing " + type + " detections.",
            noVersionNotificationMessage = "\n§cHey, just a heads up!"
                    + " Your owned editions of " + Register.pluginName + " could not be verified."
                    + " Visit §n" + DiscordServer.url + "§r§c §lfix this§r§c.",
            hasAccountNotificationMessage = "\n§cHey, just a heads up!"
                    + " You do not seem to have an account paired with your " + Register.pluginName + " AntiCheat license."
                    + " Visit §n" + DiscordServer.url + "§r§c §lfix this§r§c.";
    private static boolean
            hasAccount = true,
            notifyCache = false,
            alternativeVersion = false,
            oneVersion = false;
    private static Boolean
            currentVersion = null;

    // Verification

    public static void refresh() {
        if (currentVersion == null) {
            currentVersion = !IDs.hasToken();
        }
        PluginBase.connectionThread.executeIfUnknownThreadElseHere(() -> {
            if (!oneVersion
                    && CloudConnections.ownsProduct("26")) {
                oneVersion = true;
            }
            if (!currentVersion
                    && (!alternativeVersion || oneVersion)
                    && CloudConnections.ownsProduct(
                    getProductID(currentType)
            )) {
                currentVersion = true;
            }
            if (!alternativeVersion
                    && (!currentVersion || oneVersion)
                    && CloudConnections.ownsProduct(
                    getProductID(alternativeType)
            )) {
                alternativeVersion = true;
            }
            hasAccount = !IDs.hasToken() || CloudConnections.hasAccount();
        });
    }

    // Detections

    public static boolean hasDetectionsPurchased(Check.DataType dataType) {
        return PluginEdition.currentType == dataType
                ? currentVersion != null && currentVersion
                : alternativeVersion;
    }

    // Product

    private static String getProductID(Check.DataType dataType) {
        return dataType == null || dataType == Check.DataType.JAVA
                ? "1"
                : "16";
    }

    public static String getProductID() {
        return getProductID(
                currentVersion != null && currentVersion
                        ? currentType
                        : (alternativeVersion ? alternativeType : null)
        );
    }

    public static String getProductName(String context) {
        String lastColor = null;
        boolean color = false;

        for (char c : context.toCharArray()) {
            if (c == '§') {
                color = true;
            } else if (color) {
                lastColor = Character.toString(c);
                color = false;
            }
        }
        return Register.pluginName
                + (currentVersion != null && currentVersion && alternativeVersion
                ? " One"
                : ": §a" + (currentVersion != null && currentVersion ? "§a" : "§c") + currentType
                + "§8/" + (alternativeVersion ? "§a" : "§c") + alternativeType)
                + (lastColor != null ? "§" + lastColor : "");
    }

    // Notifications

    public static void attemptNotifications(PlayerProtocol protocol) {
        Check.DataType[] missingDetections =
                !(currentVersion != null && currentVersion) && !alternativeVersion
                        ? new Check.DataType[]{currentType, alternativeType}
                        : !(currentVersion != null && currentVersion)
                        ? new Check.DataType[]{currentType}
                        : !alternativeVersion
                        ? new Check.DataType[]{alternativeType}
                        : new Check.DataType[]{};

        if (missingDetections.length == Check.DataType.values().length) {
            attemptVersionNotification(protocol, null);
            return;
        }

        if (missingDetections.length > 0) {
            if (notifyCache) {
                attemptVersionNotification(protocol, missingDetections[0]);
                return;
            }
            long time = System.currentTimeMillis();

            if ((time - checkTime) >= 60_000L) {
                checkTime = time;

                if (missingDetections[0] == protocol.getDataType()) {
                    notifyCache = true;
                    attemptVersionNotification(protocol, missingDetections[0]);
                    return;
                }
                Collection<PlayerProtocol> players = PluginBase.getProtocols();
                players.remove(protocol);
                int size = players.size();
                List<PlayerProfile> checkedProfiles;

                if (size > 0) {
                    checkedProfiles = new ArrayList<>(size);

                    for (PlayerProtocol otherProtocol : players) {
                        if (missingDetections[0] == otherProtocol.getDataType()) {
                            notifyCache = true;
                            attemptVersionNotification(otherProtocol, missingDetections[0]);
                            return;
                        } else {
                            checkedProfiles.add(otherProtocol.profile());
                        }
                    }
                } else {
                    checkedProfiles = new ArrayList<>(0);
                }

                // Separator

                Collection<PlayerProfile> playerProfiles = ResearchEngine.getPlayerProfiles();

                if (!playerProfiles.isEmpty()) {
                    playerProfiles.remove(protocol.profile());
                    playerProfiles.removeAll(checkedProfiles);

                    if (!playerProfiles.isEmpty()) {
                        for (PlayerProfile profile : playerProfiles) {
                            if (profile.getLastDataType() == missingDetections[0]) {
                                notifyCache = true;
                                attemptVersionNotification(protocol, missingDetections[0]);
                                return;
                            }
                        }
                    }
                }
            }
        }
        attemptNoAccountNotification(protocol);
    }

    private static void attemptVersionNotification(PlayerProtocol protocol, Check.DataType dataType) {
        String message;

        if (dataType == null) {
            message = AwarenessNotifications.getNotification(noVersionNotificationMessage);
        } else {
            message = AwarenessNotifications.getOptionalNotification(
                    (versionNotificationMessage
                            + (IDs.canAdvertise()
                            ? " Click §n" + product + "§r§c to §lfix this§r§c."
                            : " Visit §n" + DiscordServer.url + "§r§c to §lfix this§r§c."))
                            .replace(type, dataType.toString())
                            .replace(
                                    product,
                                    IDs.isBuiltByBit()
                                            ? (dataType == Check.DataType.JAVA
                                            ? "https://builtbybit.com/resources/12832"
                                            : "https://builtbybit.com/resources/11196")
                                            : (dataType == Check.DataType.JAVA
                                            ? "https://polymart.org/resource/3600"
                                            : "https://polymart.org/resource/350")
                            )
            );
        }

        if (message != null
                && AwarenessNotifications.canSend(protocol.getUUID(), "alternative-version", notificationCooldown)) {
            protocol.sendImportantMessage(message);
        }
    }

    private static void attemptNoAccountNotification(PlayerProtocol protocol) {
        if (!hasAccount) {
            String message = AwarenessNotifications.getOptionalNotification(hasAccountNotificationMessage);

            if (message != null
                    && AwarenessNotifications.canSend(protocol.getUUID(), "has-account", notificationCooldown)) {
                protocol.sendImportantMessage(message);
            }
        }
    }

}
