package ai.idealistic.vacan.abstraction.check;

import ai.idealistic.vacan.abstraction.Enums;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.compatibility.Compatibility;
import ai.idealistic.vacan.compatibility.manual.abilities.ItemsAdder;
import ai.idealistic.vacan.compatibility.manual.building.MythicMobs;
import ai.idealistic.vacan.compatibility.manual.enchants.CustomEnchantsPlus;
import ai.idealistic.vacan.compatibility.manual.enchants.EcoEnchants;
import ai.idealistic.vacan.compatibility.manual.vanilla.Attributes;
import ai.idealistic.vacan.compatibility.necessary.protocollib.ProtocolLib;
import ai.idealistic.vacan.functionality.moderation.DetectionNotifications;
import ai.idealistic.vacan.functionality.server.MultiVersion;
import ai.idealistic.vacan.functionality.server.Permissions;
import ai.idealistic.vacan.functionality.server.PluginBase;
import ai.idealistic.vacan.functionality.server.TPS;
import ai.idealistic.vacan.utils.math.AlgebraUtils;
import ai.idealistic.vacan.utils.minecraft.entity.PlayerUtils;
import org.bukkit.GameMode;
import org.bukkit.event.Cancellable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CheckRunner extends CheckProcess {

    private static final boolean v1_8 = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_8);

    final long creation;
    private final Collection<CheckCancellation> disableCauses, silentCauses;
    private boolean cancelled;
    private final Map<String, CheckDetection> detections;

    public CheckRunner(Enums.HackType hackType, PlayerProtocol protocol) {
        super(hackType, protocol);
        this.creation = System.currentTimeMillis();
        this.detections = new ConcurrentHashMap<>(2);
        this.disableCauses = Collections.synchronizedList(new ArrayList<>(1));
        this.silentCauses = Collections.synchronizedList(new ArrayList<>(1));
    }

    // Detections

    public final CheckDetection getDetection(String detection) {
        return this.detections.get(detection);
    }

    public final Collection<CheckDetection> getDetections() {
        return this.detections.values();
    }

    protected final CheckDetection addDetection(String name, CheckDetection detection) {
        return this.detections.putIfAbsent(name, detection);
    }

    public final void removeDetection(CheckDetection detection) {
        this.detections.remove(detection.name);
    }

    // Handle

    public final void handle(Object cancelled, Object object) {
        if (this.protocol != null) {
            boolean result;

            if (cancelled == null) {
                if (object instanceof Cancellable) {
                    result = ((Cancellable) object).isCancelled();
                } else {
                    result = false;
                }
            } else if (cancelled instanceof Boolean) {
                result = (Boolean) cancelled;
            } else {
                if (cancelled instanceof Cancellable) {
                    result = ((Cancellable) cancelled).isCancelled();
                } else {
                    result = false;
                }
            }
            this.cancelled = result;
            this.handleInternal(result, object);
        }
    }

    protected void handleInternal(boolean cancelled, Object object) {

    }

    // Separator

    protected boolean canRun() {
        return true;
    }

    // Separator

    final boolean canCall() {
        return this.protocol != null
                && !this.protocol.npc
                && hackType.getCheck().isEnabled(this.protocol.getDataType(), this.protocol.getWorld().getName())
                && (!cancelled || hackType.getCheck().handleCancelledEvents)
                && (!v1_8 || this.protocol.getGameMode() != GameMode.SPECTATOR)
                && Attributes.getAmount(this.protocol, Attributes.GENERIC_SCALE) == 0.0;
    }

    final boolean canCancel() {
        return this.protocol != null
                && (System.currentTimeMillis() - this.creation) > TPS.maximum * TPS.tickTime
                && !ProtocolLib.isTemporary(this.protocol.bukkit())
                && !Permissions.isBypassing(this.protocol.bukkit(), hackType);
    }

    public final List<String> getEvidence() {
        if (this.protocol == null) {
            return new ArrayList<>();
        }
        List<String> evidence = new ArrayList<>();

        for (CheckDetection detection : this.getDetections()) {
            if (detection.getLevel(this.protocol.getDataType())
                    >= detection.getNotificationLevel(this.protocol.getDataType())) {
                evidence.add(detection.name);
            }
        }
        return evidence;
    }

    // Causes

    private CheckCancellation getLastCause(Collection<CheckCancellation> collection) {
        CheckCancellation lastCause = null;
        Iterator<CheckCancellation> iterator = collection.iterator();

        while (iterator.hasNext()) {
            CheckCancellation cause = iterator.next();

            if (cause.hasExpired()) {
                iterator.remove();
            } else {
                lastCause = cause;
                break;
            }
        }
        return lastCause;
    }

    public final CheckCancellation getDisableCause() {
        CheckCancellation disableCause = this.getLastCause(this.disableCauses);

        if (disableCause == null) {
            if (this.protocol != null) {
                return MythicMobs.is(this.protocol)
                        ? new CheckCancellation(Compatibility.CompatibilityType.MYTHIC_MOBS)
                        : ItemsAdder.is(this.protocol)
                        ? new CheckCancellation(Compatibility.CompatibilityType.ITEMS_ADDER)
                        : CustomEnchantsPlus.has(this.protocol)
                        ? new CheckCancellation(Compatibility.CompatibilityType.CUSTOM_ENCHANTS_PLUS)
                        : EcoEnchants.has(this.protocol)
                        ? new CheckCancellation(Compatibility.CompatibilityType.ECO_ENCHANTS)
                        : null;
            } else {
                return null;
            }
        } else {
            return disableCause;
        }
    }

    public final CheckCancellation getSilentCause() {
        return this.getLastCause(this.silentCauses);
    }

    public final void addDisableCause(String reason, String pointer, int ticks) {
        if (reason == null) {
            reason = this.hackType.getCheck().getName();
        }
        this.disableCauses.add(new CheckCancellation(reason, pointer, ticks));

        if (this.protocol != null) {
            PluginBase.playerInfo.refresh(this.protocol.bukkit().getName());
        }
    }

    public final void addSilentCause(String reason, String pointer, int ticks) {
        this.silentCauses.add(new CheckCancellation(reason, pointer, ticks));

        if (this.protocol != null) {
            PluginBase.playerInfo.refresh(this.protocol.bukkit().getName());
        }
    }

    public final void removeDisableCause() {
        this.disableCauses.clear();

        if (this.protocol != null) {
            PluginBase.playerInfo.refresh(this.protocol.bukkit().getName());
        }
    }

    public final void removeSilentCause() {
        this.silentCauses.clear();

        if (this.protocol != null) {
            PluginBase.playerInfo.refresh(this.protocol.bukkit().getName());
        }
    }

    // Prevention

    public final boolean prevent() {
        if (!this.detections.isEmpty()) {
            for (CheckDetection detection : this.getDetections()) {
                if (detection.prevent()) {
                    return true;
                }
            }
        }
        return false;
    }

    // Notification

    final int getNotificationTicksCooldown(PlayerProtocol detected) {
        Integer frequency = DetectionNotifications.getFrequency(this.protocol);

        if (frequency != null
                && frequency != DetectionNotifications.defaultFrequency) {
            return frequency;
        } else if (detected != null
                && (detected.equals(this.protocol)
                || detected.getWorld().equals(this.protocol.getWorld())
                && detected.getLocation().distance(this.protocol.getLocation()) <= PlayerUtils.chunk)) {
            return AlgebraUtils.integerRound(Math.sqrt(TPS.maximum));
        } else {
            return AlgebraUtils.integerRound(TPS.maximum);
        }
    }

}
