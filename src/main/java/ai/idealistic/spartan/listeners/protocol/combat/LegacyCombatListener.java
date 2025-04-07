package ai.idealistic.spartan.listeners.protocol.combat;

import ai.idealistic.spartan.Register;
import ai.idealistic.spartan.abstraction.event.PlayerUseEvent;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.functionality.moderation.AwarenessNotifications;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.listeners.bukkit.CombatEvent;
import ai.idealistic.spartan.utils.java.OverflowMap;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LegacyCombatListener extends PacketAdapter {

    private final Map<UUID, Integer> pendingAttacks = new OverflowMap<>(
                    new ConcurrentHashMap<>(),
                    1_024
    );

    public LegacyCombatListener() {
        super(
                        Register.plugin,
                        ListenerPriority.HIGHEST,
                        PacketType.Play.Client.USE_ENTITY,
                        PacketType.Play.Server.ENTITY_STATUS
        );
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
            PlayerProtocol protocol = PluginBase.getProtocol(event.getPlayer());

            if (protocol.isBedrockPlayer()) {
                return;
            }
            PacketContainer packet = event.getPacket();
            int entityId = packet.getIntegers().read(0);

            Player target = null;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getEntityId() == entityId) {
                    target = player;
                    break;
                }
            }

            if ((!packet.getEntityUseActions().getValues().isEmpty()) ?
                            packet.getEntityUseActions().read(0).equals(EnumWrappers.EntityUseAction.ATTACK)
                            : packet.getEnumEntityUseActions().read(0).getAction().equals(
                            EnumWrappers.EntityUseAction.ATTACK)) {
                if (protocol.isSDesync()) {
                    AwarenessNotifications.optionallySend(protocol.bukkit().getName()
                                    + " attack faster than the transaction response.");
                    //protocol.teleport(protocol.getLocation());
                    //event.setCancelled(true);
                    //return;
                }
                if (target != null) {
                    CombatEvent.use(
                                    new PlayerUseEvent(
                                                    event.getPlayer(),
                                                    target,
                                                    false
                                    )
                    );
                }
                pendingAttacks.put(protocol.getUUID(), entityId);
            }
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_STATUS) {
            PlayerProtocol protocol = PluginBase.getProtocol(event.getPlayer());

            if (protocol.isBedrockPlayer()) {
                return;
            }
            int entityId = event.getPacket().getIntegers().read(0);
            byte status = event.getPacket().getBytes().read(0);
            if (status != 2) return;
            pendingAttacks.entrySet().removeIf(entry -> {
                UUID playerUUID = entry.getKey();
                int pendingEntityId = entry.getValue();

                if (pendingEntityId == entityId) {
                    Player attacker = plugin.getServer().getPlayer(playerUUID);
                    Entity target = ProtocolLibrary.getProtocolManager().
                                    getEntityFromID(protocol.getWorld(), entityId);
                    if (attacker != null && target != null) {
                        CombatEvent.event(
                                        new EntityDamageByEntityEvent(
                                                        attacker,
                                                        target,
                                                        EntityDamageByEntityEvent.DamageCause.ENTITY_ATTACK,
                                                        0.0D
                                        ),
                                        true
                        );
                    }
                    return true;
                }
                return false;
            });
        }
    }

}