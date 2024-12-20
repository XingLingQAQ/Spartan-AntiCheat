package com.vagdedes.spartan.abstraction.check;

import com.vagdedes.spartan.abstraction.protocol.SpartanProtocol;
import com.vagdedes.spartan.functionality.server.Config;
import com.vagdedes.spartan.functionality.server.SpartanBukkit;
import com.vagdedes.spartan.functionality.server.TPS;
import org.bukkit.Location;

public class CheckPrevention {

    boolean canPrevent;
    private final Location location;
    private final boolean groundTeleport;
    private final double damage;
    private final long expiration;

    CheckPrevention() {
        this.canPrevent = false;
        this.location = null;
        this.groundTeleport = false;
        this.damage = 0.0;
        this.expiration = 0L;
    }

    CheckPrevention(Location location, int cancelTicks, boolean groundTeleport, double damage) {
        this.canPrevent = false;
        this.location = location;
        this.groundTeleport = groundTeleport;
        this.damage = damage;
        this.expiration = cancelTicks <= 1
                ? Long.MAX_VALUE
                : System.currentTimeMillis() + (cancelTicks * TPS.tickTime);
    }

    boolean complete() {
        return this.canPrevent && System.currentTimeMillis() <= this.expiration;
    }

    void handle(SpartanProtocol protocol) {
        Runnable runnable = () -> {
            if (this.location != null
                    && protocol.packetsEnabled()) {
                protocol.teleport(this.location);
            }
            if (this.groundTeleport) {
                protocol.spartan.groundTeleport();
            }
            if (this.damage > 0.0
                    && (this.location == null && !this.groundTeleport
                    || Config.settings.getBoolean("Detections.fall_damage_on_teleport"))) {
                protocol.spartan.damage(this.damage);
            }
        };

        if (SpartanBukkit.isSynchronised()) {
            runnable.run();
        } else {
            SpartanBukkit.transferTask(protocol, runnable);
        }
    }

}
