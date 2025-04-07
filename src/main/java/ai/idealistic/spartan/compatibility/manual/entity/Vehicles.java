package ai.idealistic.spartan.compatibility.manual.entity;

import ai.idealistic.spartan.abstraction.data.Cooldowns;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.compatibility.Compatibility;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.utils.java.OverflowMap;
import es.pollitoyeye.vehicles.enums.VehicleType;
import es.pollitoyeye.vehicles.events.VehicleEnterEvent;
import es.pollitoyeye.vehicles.events.VehicleExitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.LinkedHashMap;

public class Vehicles implements Listener {

    private static final Cooldowns cooldowns = new Cooldowns(
            new OverflowMap<>(new LinkedHashMap<>(), 512)
    );
    private static final String key = Compatibility.CompatibilityType.VEHICLES + "=compatibility=";
    public static final String
            DRILL = "drill",
            TRACTOR = "tractor";
    private static final String[] types = new String[]{
            DRILL,
            TRACTOR
    };

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void Enter(VehicleEnterEvent e) {
        if (Compatibility.CompatibilityType.VEHICLES.isFunctional()) {
            PlayerProtocol p = PluginBase.getProtocol(e.getPlayer());
            VehicleType vehicleType = e.getVehicleType();

            if (vehicleType == VehicleType.DRILL) {
                add(p, Vehicles.DRILL);
            } else if (vehicleType == VehicleType.TRACTOR) {
                add(p, Vehicles.TRACTOR);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void Exit(VehicleExitEvent e) {
        if (Compatibility.CompatibilityType.VEHICLES.isFunctional()) {
            PlayerProtocol p = PluginBase.getProtocol(e.getPlayer());

            for (String type : types) {
                cooldowns.remove(key(p, type));
            }
        }
    }

    private static String key(PlayerProtocol p, String type) {
        return p.getUUID() + "=" + key + type;
    }

    private static void add(PlayerProtocol p, String type) {
        cooldowns.add(key(p, type), 20);
    }

    public static boolean has(PlayerProtocol p, String type) {
        return !cooldowns.canDo(key(p, type));
    }

    public static boolean has(PlayerProtocol p, String[] types) {
        for (String type : types) {
            if (has(p, type)) {
                return true;
            }
        }
        return false;
    }
}
