package ai.idealistic.spartan.listeners;

import ai.idealistic.spartan.abstraction.entity.PluginNPC;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.abstraction.world.ServerLocation;
import ai.idealistic.spartan.functionality.server.MultiVersion;
import ai.idealistic.spartan.functionality.server.Permissions;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.utils.minecraft.entity.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.*;

public class NPCManager implements Listener {

    public static final boolean supported = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_8);
    private static final List<PluginNPC> list = Collections.synchronizedList(new ArrayList<>());

    static {
        PluginBase.runRepeatingTask(() -> {
            if (!list.isEmpty()) {
                List<PlayerProtocol> staff = Permissions.getStaff();

                if (staff.isEmpty()) {
                    clear();
                } else {
                    synchronized (list) {
                        Iterator<PluginNPC> iterator = list.iterator();

                        while (iterator.hasNext()) {
                            PluginNPC npc = iterator.next();
                            boolean animate = false;

                            for (PlayerProtocol player : staff) {
                                if (ServerLocation.distance(npc.location, player.getLocationOrVehicle()) <= PlayerUtils.chunk) {
                                    animate = true;
                                    break;
                                }
                            }

                            if (animate) {
                                if (!npc.animate(staff)) {
                                    iterator.remove();
                                }
                            } else {
                                npc.remove();
                                iterator.remove();
                            }
                        }
                    }
                }
            }
        }, 1L, 1L);
    }

    public static void clear() {
        if (!list.isEmpty()) {
            synchronized (list) {
                for (PluginNPC npc : list) {
                    npc.remove();
                }
                list.clear();
            }
        }
    }

    public static void clear(World world) {
        if (!list.isEmpty()) {
            synchronized (list) {
                for (PluginNPC npc : list) {
                    if (world.equals(npc.armorStand.getWorld())) {
                        npc.remove();
                    }
                }
                list.clear();
            }
        }
    }

    public static void create(PlayerProtocol protocol) {
        if (supported) {
            Location location = protocol.getLocationOrVehicle();

            if (!list.isEmpty()) {
                synchronized (list) {
                    for (PluginNPC npc : list) {
                        if (ServerLocation.distance(npc.location, location) <= PlayerUtils.chunk) {
                            protocol.teleport(npc.location);
                            return;
                        }
                    }
                    list.add(new PluginNPC(location));
                }
            } else {
                synchronized (list) {
                    list.add(new PluginNPC(location));
                }
            }
        }
    }

    @EventHandler
    public void Interact(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();

        if (entity instanceof ArmorStand) {
            UUID uuid = entity.getUniqueId();

            if (!list.isEmpty()) {
                synchronized (list) {
                    for (PluginNPC npc : list) {
                        if (npc.getUniqueId().equals(uuid)) {
                            e.setCancelled(true);
                            npc.updateHead();
                            PlayerProtocol protocol = PluginBase.getProtocol(e.getPlayer());
                            PluginBase.playerInfo.open(protocol, protocol.bukkit().getName());
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void Damage(EntityDamageByEntityEvent e) {
        Entity entity = e.getEntity();

        if (entity instanceof ArmorStand) {
            UUID uuid = entity.getUniqueId();

            if (!list.isEmpty()) {
                synchronized (list) {
                    for (PluginNPC npc : list) {
                        if (npc.getUniqueId().equals(uuid)) {
                            e.setCancelled(true);
                            npc.updateHead();
                            Entity damager = e.getDamager();

                            if (damager instanceof Player) {
                                PlayerProtocol protocol = PluginBase.getProtocol((Player) damager);
                                PluginBase.playerInfo.open(protocol, protocol.bukkit().getName());
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

}
