package ai.idealistic.spartan.abstraction.check.implementation.combat.killaura;

import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.event.PlayerAttackEvent;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.functionality.server.MultiVersion;
import ai.idealistic.spartan.functionality.server.PluginBase;
import ai.idealistic.spartan.utils.java.ConcurrentList;
import ai.idealistic.spartan.utils.math.RayUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class KABackTrack extends CheckDetection {

    KABackTrack(CheckRunner executor) {
        super(executor, null, null, "back_track", true);
    }

    private List<Integer> analysis = null;

    void run(PlayerAttackEvent event) {
        this.call(() -> {
            if (!(event.target instanceof Player)) return;
            if (analysis == null) analysis = new ConcurrentList<>();
            PlayerProtocol target
                    = PluginBase.getProtocol((Player) event.target);
            List<Location> h = target.getPositionHistory();
            boolean is1_8 = this.protocol.isUsingVersion(MultiVersion.MCVersion.V1_8);
            float hitbox = (is1_8) ? 0.4F : 0.3F;
            int i = 0;
            for (Location l : h) {
                if (RayUtils.inHitbox(this.protocol, l, event.target, hitbox, 3.0F)) {
                    break;
                } else {
                    i++;
                }
            }
            this.analysis.add(i);
            if (this.analysis.size() >= 15)
                this.total();
        });
    }

    private void total() {
        { // MATH MNATHHM MAHTh
            int o = 0, f = 0, c = 0, a = 0;
            for (int i : this.analysis) {
                a += i;
                if (i > 0 && o == 0) f++;
                if (i == 0) c++;
                o = i;
            }
            a /= this.analysis.size();
            a *= 50;
            //this.player.getInstance().sendMessage("(approximately " + a + "ms) [f: " + f + ", c: " + c + "]");
            if (c > 5 && f > 2) {
                this.cancel("BackTrack [or past locations abuse] (approximately " + a + "ms) [f: " + f + ", c: " + c + "]");
            }
        }
        this.analysis.clear();
    }
}
