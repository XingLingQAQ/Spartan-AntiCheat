package ai.idealistic.vacan.listeners.protocol.standalone;

import ai.idealistic.vacan.Register;
import ai.idealistic.vacan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.vacan.functionality.server.PluginBase;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class JoinListener extends PacketAdapter {

    public JoinListener() {
        super(
                Register.plugin,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.LOGIN
        );
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        Player player = event.getPlayer();
        PlayerProtocol protocol = PluginBase.getProtocol(player);

        if (protocol.isBedrockPlayer()) {
            return;
        }
        LegacyLagCompensationListener.newPacket(protocol.getEntityId());
        protocol.transactionBoot = true;
        Bukkit.getScheduler().runTaskLater(Register.plugin, () -> {
            if (protocol.getVehicle() != null) {
                protocol.entityHandle = true;
            }
        }, 1L);
    }

}