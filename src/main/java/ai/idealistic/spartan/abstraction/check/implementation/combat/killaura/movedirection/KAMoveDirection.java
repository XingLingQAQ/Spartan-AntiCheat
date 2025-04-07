package ai.idealistic.spartan.abstraction.check.implementation.combat.killaura.movedirection;

import ai.idealistic.spartan.abstraction.check.CheckDetection;
import ai.idealistic.spartan.abstraction.check.CheckRunner;
import ai.idealistic.spartan.abstraction.check.definition.ImplementedDetection;
import ai.idealistic.spartan.abstraction.world.ServerLocation;
import ai.idealistic.spartan.compatibility.necessary.protocollib.ProtocolLib;
import ai.idealistic.spartan.utils.java.ConcurrentList;
import ai.idealistic.spartan.utils.math.RayUtils;
import ai.idealistic.spartan.utils.minecraft.vector.CVector2F;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

import static java.lang.Math.PI;

public class KAMoveDirection extends CheckDetection {

    private final CheckDetection layer1, layer2;
    private AttackHandle attackHandle;
    private final List<AttackData> aimAnalysis;
    private float vl, vlL2;
    private String punish, punishL2;
    private double lastSwitchYaw;
    private final List<UUID> targetsOnTheRow;
    private final List<Double> rayTraceAnalysis;

    public KAMoveDirection(CheckRunner executor) {
        super(executor, null, null, null, true);
        this.layer1 = new ImplementedDetection(this, "move_direction_1", true,
                DEFAULT_AVERAGE_TIME * 4L,
                TIME_TO_NOTIFY,
                TIME_TO_PREVENT,
                TIME_TO_PUNISH);
        this.layer2 = new ImplementedDetection(this, "move_direction_2", true,
                DEFAULT_AVERAGE_TIME * 4L,
                TIME_TO_NOTIFY,
                TIME_TO_PREVENT,
                TIME_TO_PUNISH);
        this.aimAnalysis = new ConcurrentList<>();
        this.attackHandle = new AttackHandle(null, System.currentTimeMillis());

        this.vl = 0;
        this.vlL2 = 0;
        this.punish = "";
        this.punishL2 = "";
        this.targetsOnTheRow = new ConcurrentList<>();
        this.rayTraceAnalysis = new ConcurrentList<>();
    }

    public void run() {
        this.call(() -> {
            if (attackHandle.time + 3000 > System.currentTimeMillis()) {
                // main
                Entity target = attackHandle.target;

                if (target != null) {
                    Location targetLoc = ProtocolLib.getLocationOrNull(target);

                    if (targetLoc == null) {
                        return;
                    }
                    double bruteforceRayTrace = RayUtils.bruteforceRayTrace(this.protocol, target);
                    double diff = bruteforceRayTrace - 0.4;
                    double predict = RayUtils.castTo360(
                            calculate(
                                    this.protocol.getLocation().toVector(),
                                    new ServerLocation(targetLoc).toVector()
                            ).x
                    );
                    double yaw360 = RayUtils.castTo360(this.protocol.getLocation().getYaw());
                    rayTraceAnalysis.add(bruteforceRayTrace);
                    aimAnalysis.add(
                            new AttackData(
                                    Math.abs(predict - yaw360),
                                    this.protocol.getLocation().getYaw(),
                                    this.protocol.getLocation().getPitch(),
                                    System.currentTimeMillis(),
                                    ProtocolLib.getUUID(target)
                            )
                    );
                    if (rayTraceAnalysis.size() >= 10) {
                        analyze();
                    }
                }
            }
        });
    }

    private void analyze() {
        targetsOnTheRow.clear();
        punish = "";
        punishL2 = "";
        double globalValue = 0;
        double changeYaw = 0;
        double oldYaw = aimAnalysis.get(0).pitch;
        double oldYawResult = aimAnalysis.get(0).yaw;
        double oldPitchResult = aimAnalysis.get(0).pitch;
        double yawChangeFirst = Math.abs(aimAnalysis.get(0).yaw - aimAnalysis.get(1).yaw);
        int basicHeuristic = 0;
        int lowHeuristic = 0;
        int machineKnownMovement = 0;
        int constantRotations = 0;
        int gcd = 0;
        int aggressivePatternI = 0;
        int aggressivePatternD = 0;
        int aggressivePatternI2 = 0;
        int aggressivePatternD2 = 0;
        int robotizedAmount = 0;
        int aggressiveAim = 0;
        int infinitives = 0;
        for (Double value : rayTraceAnalysis) {
            globalValue += value;
        }
        for (AttackData value : aimAnalysis) {
            changeYaw += Math.abs(value.yaw - oldYaw);
            oldYaw = value.yaw;
        }
        double oldYawChange = Math.abs(aimAnalysis.get(0).yaw - oldYawResult);
        double oldPitchChange = Math.abs(aimAnalysis.get(0).pitch - oldPitchResult);
        for (int i = 0; i < aimAnalysis.size(); i++) {
            if (!targetsOnTheRow.contains(aimAnalysis.get(i).target))
                targetsOnTheRow.add(aimAnalysis.get(i).target);
            double yawChange = Math.abs(aimAnalysis.get(i).yaw - oldYawResult);
            double pitchChange = Math.abs(aimAnalysis.get(i).pitch - oldPitchResult);
            double expectedYaw = yawChange * 1.073742f + (float) (yawChange + 0.15);
            double expectedPitch = pitchChange * 1.073742f - (float) (pitchChange - 0.15);
            double yawDiff = Math.abs(yawChange - expectedYaw);
            double pitchDiff = Math.abs(pitchChange - expectedPitch);
            double switchYaw = Math.abs(yawChange - yawDiff);
            double switchPitch = Math.abs(pitchChange - pitchDiff);
            double switchDiffOnYaw = Math.abs(switchYaw - lastSwitchYaw);
            double interpolation;

            double robotized = Math.abs(yawChange - yawChangeFirst);
            double diffBetweenYawChanges = yawChange - oldYawChange;
            double diffBetweenPitchChanges = pitchChange - oldPitchChange;

            if (robotized < 2 && yawChange > 2.5
                    && aimAnalysis.get(i).accuracy < 10) robotizedAmount += 1;

            if (rayTraceAnalysis.get(i) < 0.1 && yawChange > 5) basicHeuristic++;

            if (rayTraceAnalysis.get(i) < 0.03 && yawChange > 1) lowHeuristic++;


            if (robotized < 0.99 && yawChange > 3) machineKnownMovement++;
            if (robotized < 0.02 && yawChange > 2) constantRotations++;
            if (robotized < 2 && yawChange > 3) aggressiveAim++;
            interpolation = RayUtils.scaleVal(yawChange / robotized, 2);
            if (Double.isInfinite(interpolation)) infinitives++;
            if (RayUtils.scaleVal(yawChange, 2) == 0.1 || RayUtils.scaleVal(pitchChange, 2) == 0.1) gcd++;
            if (RayUtils.scaleVal(yawChange, 2) == 0.01 || RayUtils.scaleVal(pitchChange, 2) == 0.01) gcd++;
            if ((diffBetweenYawChanges > 0.01 && diffBetweenYawChanges < 2)) aggressivePatternI++;
            if ((diffBetweenYawChanges < -0.01 && diffBetweenYawChanges > -2)) aggressivePatternD++;
            if (diffBetweenYawChanges > 2) aggressivePatternI2++;
            if (diffBetweenYawChanges < -2) aggressivePatternD2++;
            oldYawResult = aimAnalysis.get(i).yaw;
            oldPitchResult = aimAnalysis.get(i).pitch;
            oldYawChange = yawChange;
            oldPitchChange = pitchChange;
            lastSwitchYaw = switchYaw;
        }
        // player.sendMessage("huge: " + hugeMoveAmount + ", low: " + lowMoveAmount);
        double result = globalValue / rayTraceAnalysis.size();

        if (targetsOnTheRow.size() < 3) {
            if (basicHeuristic > 6) addNewPunish("heuristic(basic)", 210);
            if (lowHeuristic > 7) addNewPunish("heuristic(low)", 210);
            if (robotizedAmount > 7) addNewPunish("heuristic(sync)", 170);
            if (machineKnownMovement > 4) addNewPunish("heuristic(aim)", 100);
            if (aggressiveAim > 8) addNewPunish("heuristic(aggressive)", 65);
            if (constantRotations > 2) addNewPunish("heuristic(constant)", 60);
            if (infinitives > 1) addNewPunishL2("heuristic(interpolation)", (infinitives > 2) ? 40 : 35);
            if (gcd > 2) addNewPunish("pattern(gcd)", 210);
            if (aggressivePatternI > 3 && aggressivePatternD > 3) addNewPunish("pattern(random)", 40);
            if (aggressivePatternI2 > 3 && aggressivePatternD2 > 3
                    && (aggressivePatternI2 + aggressivePatternD2) > 8 && targetsOnTheRow.size() < 2) {
                addNewPunish("pattern(snap)", 40);
            }
        }

        double punishValue = Math.floor(vl / 200);
        if (!punish.isEmpty() && vl > priorityData(priorityType()).vlLimit) {
            this.layer1.cancel(
                    "(layer1) type: " + punish + ", bH: " + basicHeuristic
                            + ", lH: " + lowHeuristic + ", rA: "
                            + robotizedAmount + ", mKM: " + machineKnownMovement
                            + ", aA: " + aggressiveAim + ", cR: " + constantRotations
                            + ", gcd: " + gcd + ", aPI: " + aggressivePatternI
                            + ", aPD: " + aggressivePatternD + ", aPI2: " + aggressivePatternI2
                            + ", aPD2: " + aggressivePatternD2,
                    this.protocol.getFromLocation()
            );
            vl -= 10;
        }

        if (!punishL2.isEmpty() && vlL2 > (priorityData(priorityType()).vlLimit / 2) + 50) {
            this.layer2.cancel(
                    "(layer2) type: " + punishL2 + ", infs: " + infinitives,
                    this.protocol.getFromLocation()
            );
            vlL2 -= 10;
        }
        //player.protocol.player.sendMessage("vl: " + vl);
        if (vl > 0) vl -= priorityData(priorityType()).vlFader;
        if (vl > priorityData(priorityType()).vlLimit) vl -= 10;
        if (vlL2 > 0) vlL2 -= (float) (priorityData(priorityType()).vlFader * 1.5);
        if (vlL2 > priorityData(priorityType()).vlLimit * 1.5) vlL2 -= 10;
        rayTraceAnalysis.clear();
        aimAnalysis.clear();

    }

    public void run(Entity targetHandle) {
        this.call(() -> {
            if (targetHandle instanceof Player
                    || targetHandle instanceof Zombie
                    || targetHandle instanceof Creeper
                    || targetHandle instanceof Skeleton) {
                attackHandle = new AttackHandle(targetHandle, System.currentTimeMillis());
            }
        });
    }

    private static CVector2F calculate(final Vector from, final Vector to) {
        Vector diff = to.subtract(from);
        double distance = Math.hypot(diff.getX(), diff.getZ());
        float yaw = (float) (Math.atan2(diff.getZ(), diff.getX()) * 180.0F / PI) - 90.0F,
                pitch = (float) (-(Math.atan2(diff.getY(), distance) * 180.0F / PI));
        return new CVector2F(yaw, pitch);
    }

    private void addNewPunish(String reason, float vlAdd) {
        vl += vlAdd / targetsOnTheRow.size();
        if (punish.isEmpty()) {
            punish = reason;
        } else {
            punish += ", " + reason;
        }
    }

    private void addNewPunishL2(String reason, float vlAdd) {
        vlL2 += vlAdd;
        if (punishL2.isEmpty()) {
            punishL2 = reason;
        } else if (!punishL2.contains(reason)) {
            punishL2 += ", " + reason;
        }
    }

    private static PriorityType priorityType() {
        return PriorityType.CAREFUL;
    }

    private static PriorityData priorityData(PriorityType priorityType) {
        switch (priorityType) {
            case AGGRESSIVE:
                return new PriorityData(200, 4);
            case LENIENT:
                return new PriorityData(900, 6);
            case SILENT:
                return new PriorityData(1500, 8);
            default: // CAREFUL
                return new PriorityData(400, 5);
        }
    }


}