package ai.idealistic.spartan.abstraction.data;

import ai.idealistic.spartan.abstraction.event.PlayerTickEvent;
import ai.idealistic.spartan.abstraction.event.ServerBlockChange;
import ai.idealistic.spartan.listeners.bukkit.standalone.ChunksEvent;
import ai.idealistic.spartan.utils.java.ConcurrentList;
import ai.idealistic.spartan.utils.minecraft.world.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class PacketWorld {

    private final List<ServerBlockChange> query;
    private final Player player;
    private int lagTick;
    public boolean transactionLock;

    public PacketWorld(Player player) {
        this.player = player;
        this.query = new ConcurrentList<>();
        this.lagTick = 0;
    }


    public void tick(PlayerTickEvent tickEvent) {
        if (tickEvent.getDelay() < 12) {
            this.lagTick = 3;
        } else if (lagTick > 0) {
            if (!this.transactionLock) this.lagTick--;
        } else {
            this.query.removeIf(change -> --change.tick == 0);
        }
    }

    public List<ServerBlockChange> getLocalWorld() {
        return new ArrayList<>(this.query);
    }

    public Material getBlock(Location location) {
        for (ServerBlockChange change : this.query) {
            Location lL = change.position.toLocation(this.player.getWorld());

            if (Math.abs(lL.getX() - location.getX()) <= 1.3 &&
                    Math.abs(lL.getY() - location.getY()) <= 1.3 &&
                    Math.abs(lL.getZ() - location.getZ()) <= 1.3) {
                return change.getData();
            }
        }

        Block b = ChunksEvent.getBlockAsync(location);
        return (b == null) ? null : b.getType();
    }

    public void worldChange(ServerBlockChange blockChange) {
        if (BlockUtils.areAir(blockChange.getData())) {
            Block b = ChunksEvent.getBlockAsync(
                    blockChange.position.toLocation(this.player.getWorld())
            );
            if (b == null || BlockUtils.areAir(b.getType())) return;
            blockChange.setData(b.getType());
        }
        long hash = blockChange.hashCode();

        for (ServerBlockChange c : this.query) {
            if (c.generateHash() == hash) {
                this.query.remove(c); // to delete
                break;
            }
        }
        this.query.add(blockChange);
        this.transactionLock = true;
        this.lagTick = 3;
    }

}