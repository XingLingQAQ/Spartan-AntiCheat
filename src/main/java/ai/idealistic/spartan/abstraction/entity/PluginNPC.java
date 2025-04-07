package ai.idealistic.spartan.abstraction.entity;

import ai.idealistic.spartan.Register;
import ai.idealistic.spartan.abstraction.protocol.PlayerProtocol;
import ai.idealistic.spartan.abstraction.world.ServerLocation;
import ai.idealistic.spartan.functionality.concurrent.CheckThread;
import ai.idealistic.spartan.functionality.server.MultiVersion;
import ai.idealistic.spartan.utils.minecraft.entity.PlayerUtils;
import ai.idealistic.spartan.utils.minecraft.inventory.EnchantmentUtils;
import ai.idealistic.spartan.utils.minecraft.inventory.InventoryUtils;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class PluginNPC {

    public static final boolean
            hasSecondHand = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_9),
            hasLockType = MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_16);
    private static final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(
            UUID.fromString("0e6f8837-3b61-4e42-b91c-81c0222bafd9")
    );
    private static final String backupName = "IdealisticAI";

    public final ArmorStand armorStand;
    public final Location location;
    private double handPose, headPose;

    public PluginNPC(Location location) {
        Location loc = location.clone();
        loc.setX(loc.getBlockX() + 0.5);
        loc.setZ(loc.getBlockZ() + 0.5);
        this.location = loc;
        this.armorStand = (ArmorStand) loc.getWorld().spawnEntity(
                loc,
                EntityType.ARMOR_STAND
        );
        armorStand.setGravity(false);
        armorStand.setSmall(false);
        armorStand.setVisible(false);
        armorStand.setCustomName("§2" + Register.pluginName + " §cAnti§4Cheat");
        armorStand.setCustomNameVisible(true);
        armorStand.setArms(true);

        if (hasLockType) {
            armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.OFF_HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);
        }
        if (hasSecondHand) {
            armorStand.getEquipment().setItemInOffHand(new ItemStack(Material.SHIELD));
        }
        this.updateBodyAndLegs();
        this.updateHead();

        ItemStack itemStack = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(Color.RED);
        itemStack.setItemMeta(meta);
        armorStand.getEquipment().setChestplate(itemStack);

        armorStand.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));

        itemStack = new ItemStack(Material.LEATHER_BOOTS);
        meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(Color.GRAY);
        itemStack.setItemMeta(meta);
        armorStand.getEquipment().setBoots(itemStack);

        itemStack = new ItemStack(Material.IRON_SWORD);
        itemStack.addEnchantment(EnchantmentUtils.DURABILITY, 1);
        armorStand.getEquipment().setItemInHand(itemStack);
    }

    private void updateBodyAndLegs() {
        armorStand.setBodyPose(new EulerAngle(
                0,
                0,
                0
        ));
        armorStand.setRightLegPose(new EulerAngle(
                0,
                0,
                Math.toRadians(5.0)
        ));
        armorStand.setLeftLegPose(new EulerAngle(
                0,
                0,
                -Math.toRadians(5.0)
        ));
    }

    public boolean animate(List<PlayerProtocol> protocols) {
        if (armorStand.isDead()) {
            return false;
        } else {
            this.updateBodyAndLegs();
            double headMax = 5.0, handMax = 15.0;

            if (headPose >= headMax) {
                headPose = -headMax;
            }
            headPose += 0.1;

            if (handPose >= handMax) {
                handPose = -handMax;
            }
            handPose += 0.25;

            armorStand.getWorld().playEffect(
                    armorStand.getLocation().clone()
                            .add(0.0, Math.ceil(armorStand.getEyeHeight()) * 2.0, 0.0),
                    Effect.ENDER_SIGNAL,
                    (int) PlayerUtils.chunk
            );
            armorStand.setHeadPose(
                    new EulerAngle(
                            Math.toRadians(Math.abs(headPose)),
                            0.0,
                            0.0
                    )
            );
            armorStand.setRightArmPose(new EulerAngle(
                    0.0,
                    0.0,
                    Math.toRadians(Math.abs(handPose))
            ));
            armorStand.setLeftArmPose(new EulerAngle(
                    0.0,
                    0.0,
                    -Math.toRadians(Math.abs(handPose))
            ));
            PlayerProtocol closest = null;

            for (PlayerProtocol protocol : protocols) {
                if (closest == null
                        || ServerLocation.distance(protocol.getLocationOrVehicle(), location)
                        < ServerLocation.distance(closest.getLocationOrVehicle(), location)) {
                    closest = protocol;
                }
            }
            Vector playerVec = closest.getLocation().toVector();
            Vector sheepVec = armorStand.getLocation().toVector();
            Vector toLookAtVec = playerVec.subtract(sheepVec);
            armorStand.teleport(armorStand.getLocation().setDirection(toLookAtVec));
            return true;
        }
    }

    public void updateHead() {
        CheckThread.run(
                () -> armorStand.getEquipment().setHelmet(
                        InventoryUtils.getSkull(offlinePlayer, backupName, true)
                )
        );
    }

    public void remove() {
        armorStand.remove();
    }

    public UUID getUniqueId() {
        return armorStand.getUniqueId();
    }

}
