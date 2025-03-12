package ai.idealistic.vacan.abstraction.protocol;

import ai.idealistic.vacan.abstraction.check.Check;
import ai.idealistic.vacan.abstraction.data.CheckBoundData;
import ai.idealistic.vacan.abstraction.data.EnvironmentData;
import ai.idealistic.vacan.abstraction.data.PacketWorld;
import ai.idealistic.vacan.abstraction.data.TimerBalancer;
import ai.idealistic.vacan.abstraction.data.component.ComponentXZ;
import ai.idealistic.vacan.abstraction.data.component.ComponentY;
import ai.idealistic.vacan.abstraction.event.CPlayerVelocityEvent;
import ai.idealistic.vacan.abstraction.event.PlayerTickEvent;
import ai.idealistic.vacan.abstraction.profiling.PlayerProfile;
import ai.idealistic.vacan.abstraction.world.ServerLocation;
import ai.idealistic.vacan.compatibility.Compatibility;
import ai.idealistic.vacan.compatibility.necessary.BedrockCompatibility;
import ai.idealistic.vacan.compatibility.necessary.protocollib.ProtocolLib;
import ai.idealistic.vacan.functionality.server.Config;
import ai.idealistic.vacan.functionality.server.MultiVersion;
import ai.idealistic.vacan.functionality.server.PluginBase;
import ai.idealistic.vacan.functionality.server.TPS;
import ai.idealistic.vacan.functionality.tracking.ResearchEngine;
import ai.idealistic.vacan.utils.java.StringUtils;
import ai.idealistic.vacan.utils.math.AlgebraUtils;
import ai.idealistic.vacan.utils.math.MathHelper;
import ai.idealistic.vacan.utils.minecraft.entity.AxisAlignedBB;
import ai.idealistic.vacan.utils.minecraft.entity.PlayerUtils;
import ai.idealistic.vacan.utils.minecraft.entity.PotionEffectUtils;
import ai.idealistic.vacan.utils.minecraft.protocol.ProtocolTools;
import ai.idealistic.vacan.utils.minecraft.server.PluginUtils;
import ai.idealistic.vacan.utils.minecraft.world.BlockUtils;
import ai.idealistic.vacan.utils.minecraft.world.GroundUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerProtocol {

    public long activeCreationTime;
    private Player bukkit;
    @Getter
    private EnvironmentData environment;
    public final PlayerPunishments punishments;
    public int
            slime0Tick,
            rightClickCounter,
            transactionVl,
            flyingTicks,
            predictedSlimeTicks;
    public final boolean npc;
    public boolean
            onGround,
            onGroundFrom,
            sprinting,
            sneaking,
            pistonTick,
            transactionBoot,
            clickBlocker,
            transactionSentKeep,
            useItemPacket,
            useItemPacketReset;
    @Setter
    @Getter
    private Location
            location,
            from,
            teleport,
            schedulerFrom;
    public String fromWorld;
    private final List<Location> positionHistory;
    public CPlayerVelocityEvent claimedVelocity;
    public final List<CPlayerVelocityEvent>
            claimedVeloGravity,
            claimedVeloSpeed;
    public long tickTime;
    public final MultiVersion.MCVersion version;
    public final TimerBalancer timerBalancer;
    public final Check.DetectionType detectionType;
    @Getter
    private Check.DataType dataType;
    private Set<AxisAlignedBB> axisMatrixCache;
    @Setter
    private CheckBoundData checkBoundData;
    public final PacketWorld packetWorld;
    public PlayerTickEvent lastTickEvent;
    public short transactionId;
    public long
            transactionTime,
            transactionLastTime,
            transactionPing,
            lagTick,
            oldClickTime,
            soulSandWater,
            magmaCubeWater,
            lastVelocity;
    private long
            lastInteraction,
            lastFlight,
            lastGlide;
    public boolean entityHandle;
    private ComponentY componentY;
    private ComponentXZ componentXZ;
    private final Map<Long, ServerLocation> locations;
    private Vector clampVector;
    private final Map<PotionEffectType, ExtendedPotionEffect> potionEffects;
    private boolean afk;
    Entity[] nearbyEntities;
    final double[] maxNearbyEntitiesCoordinate;

    public PlayerProtocol(Player player) {
        long time = System.currentTimeMillis();
        this.bukkit = player;
        this.activeCreationTime = time;
        this.version = MultiVersion.get(player);
        this.packetWorld = new PacketWorld(player);
        this.npc = player.getAddress() == null;
        this.punishments = new PlayerPunishments(this);
        this.dataType = BedrockCompatibility.isPlayer(player)
                ? Check.DataType.BEDROCK
                : Check.DataType.JAVA;
        this.detectionType = this.packetsEnabled()
                ? Check.DetectionType.PACKETS
                : Check.DetectionType.BUKKIT;
        this.onGround = false;
        this.onGroundFrom = false;
        this.sprinting = false;
        this.sneaking = false;
        this.location = ProtocolTools.getLoadLocation(player);
        this.from = null;
        this.fromWorld = "";
        this.teleport = null;
        this.transactionVl = 0;
        this.timerBalancer = new TimerBalancer();
        this.flyingTicks = 0;
        this.oldClickTime = System.currentTimeMillis();
        this.clickBlocker = false;
        this.tickTime = time;
        this.environment = new EnvironmentData();
        this.rightClickCounter = 0;
        this.positionHistory = Collections.synchronizedList(new LinkedList<>());
        this.claimedVelocity = null;
        this.claimedVeloGravity = new CopyOnWriteArrayList<>();
        this.claimedVeloSpeed = new CopyOnWriteArrayList<>();
        this.entityHandle = false;
        this.axisMatrixCache = new HashSet<>();
        this.checkBoundData = null;
        this.pistonTick = false;
        this.lastTickEvent = null;
        this.transactionId = (short) -1939;
        this.transactionTime = System.currentTimeMillis();
        this.transactionLastTime = System.currentTimeMillis();
        this.transactionPing = 0;
        this.transactionBoot = false;
        this.transactionSentKeep = false;
        this.lagTick = 0;
        this.predictedSlimeTicks = 0;
        this.componentY = new ComponentY();
        this.componentXZ = new ComponentXZ();
        this.useItemPacket = false;
        this.useItemPacketReset = false;
        this.slime0Tick = 0;
        this.locations = Collections.synchronizedMap(new LinkedHashMap<>());
        this.schedulerFrom = this.location;
        this.clampVector = new Vector();
        this.potionEffects = new ConcurrentHashMap<>(2);
        this.setStoredPotionEffects();
        this.lastInteraction = System.currentTimeMillis();
        this.afk = false;
        this.nearbyEntities = new Entity[0];
        this.maxNearbyEntitiesCoordinate = new double[3];

        // Always last
        this.profile().update(this);
    }

    static {
        PluginBase.runRepeatingTask(() -> {
            Collection<PlayerProtocol> protocols = PluginBase.getProtocols();

            if (!protocols.isEmpty()) {
                for (PlayerProtocol protocol : protocols) {
                    Check.DataType previousDataType = protocol.dataType;
                    protocol.dataType = BedrockCompatibility.isPlayer(protocol.bukkit())
                            ? Check.DataType.BEDROCK
                            : Check.DataType.JAVA;

                    if (protocol.dataType != previousDataType) {
                        protocol.profile().update(protocol);
                    }
                    if (!MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13)) {
                        protocol.setStoredPotionEffects();
                    }
                    if (protocol.maxNearbyEntitiesCoordinate[0] > 0.0
                            || protocol.maxNearbyEntitiesCoordinate[1] > 0.0
                            || protocol.maxNearbyEntitiesCoordinate[2] > 0.0) {
                        protocol.nearbyEntities = protocol.bukkit().getNearbyEntities(
                                protocol.maxNearbyEntitiesCoordinate[0],
                                protocol.maxNearbyEntitiesCoordinate[1],
                                protocol.maxNearbyEntitiesCoordinate[2]
                        ).toArray(new Entity[0]);
                    }
                    protocol.checkForAFK();
                    protocol.profile().executeRunners(false, null);
                    protocol.schedulerFrom = protocol.getLocation();
                }
            }
        }, 1L, 1L);
    }

    public final Player bukkit() {
        return this.bukkit;
    }

    public void updateBukkit(Player player) {
        this.bukkit = player;
    }

    private void resetActiveCreationTime() {
        this.activeCreationTime = System.currentTimeMillis();
    }

    public long getTimePassed(long time) {
        return System.currentTimeMillis() - this.activeCreationTime;
    }

    public long getActiveTimePlayed() {
        return System.currentTimeMillis() - this.activeCreationTime;
    }

    public boolean isUsingVersion(MultiVersion.MCVersion trialVersion) {
        return version.ordinal() == trialVersion.ordinal();
    }

    public boolean isDesync() {
        return this.transactionSentKeep && (System.currentTimeMillis() - this.transactionTime > 55);
    }

    public boolean isBlatantDesync() {
        return this.transactionSentKeep && (System.currentTimeMillis() - this.transactionTime > 150);
    }

    public boolean isSDesync() {
        return this.transactionSentKeep && (System.currentTimeMillis() - this.transactionTime > 400);
    }

    public boolean isUsingVersionOrGreater(MultiVersion.MCVersion trialVersion) {
        return version.ordinal() >= trialVersion.ordinal();
    }

    public boolean isOnGround() {
        if (packetsEnabled()) {
            return this.onGround;
        } else {
            Entity vehicle = this.getVehicle();

            if (vehicle != null) {
                return vehicle.isOnGround();
            } else {
                return this.bukkit().isOnGround();
            }
        }
    }

    public boolean isOnGroundFrom() {
        return packetsEnabled()
                ? this.onGroundFrom
                : this.isOnGround();
    }

    public boolean isSprinting() {
        return packetsEnabled()
                ? this.sprinting
                : this.bukkit().isSprinting();
    }

    public boolean isSneaking() {
        return packetsEnabled()
                ? this.sneaking
                : this.bukkit().isSneaking();
    }

    public Location getLocation() {
        Location loc = this.packetsEnabled()
                ? this.location
                : ProtocolLib.getLocationOrNull(this.bukkit());
        return loc != null
                ? loc
                : ServerLocation.bukkitDefault.clone();
    }

    public Location getFromLocation() {
        return this.from != null
                ? this.from
                : this.getLocation();
    }

    public Location getVehicleLocation() {
        Entity vehicle = this.getVehicle();

        if (vehicle instanceof LivingEntity || vehicle instanceof Vehicle) {
            return ProtocolLib.getLocationOrNull(vehicle);
        } else {
            return null;
        }
    }

    public Location getLocationOrVehicle() {
        Location vehicleLocation = getVehicleLocation();

        if (vehicleLocation == null) {
            return this.getLocation();
        } else {
            return vehicleLocation;
        }
    }

    public List<Location> getPositionHistory() {
        synchronized (this.positionHistory) {
            return new ArrayList<>(this.positionHistory);
        }
    }

    public ServerLocation getPastTickRotation() {
        Location l = this.getLocation().clone();
        l.setYaw(this.from.getYaw());
        l.setPitch(this.from.getPitch());
        return new ServerLocation(l);
    }

    public boolean teleport(Location location) {
        if (location.getX() == ServerLocation.bukkitDefault.getX()
                && location.getY() == ServerLocation.bukkitDefault.getY()
                && location.getZ() == ServerLocation.bukkitDefault.getZ()) {
            return false;
        }
        boolean sync = PluginBase.isSynchronised();

        if (MultiVersion.folia) {
            if (sync) {
                this.bukkit().leaveVehicle();
            }
            this.bukkit().teleportAsync(location);
        } else if (sync) {
            this.bukkit().leaveVehicle();
            this.bukkit().teleport(location);
        } else {
            PluginBase.transferTask(
                    this,
                    () -> {
                        Player bukkit = this.bukkit();

                        if (bukkit.isOnline()) {
                            bukkit.teleport(location);
                        }
                    }
            );
        }
        return true;
    }

    public int getPing() {
        if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_17)) {
            return this.bukkit().isOnline() ? this.bukkit().getPing() : 0;
        } else {
            try {
                Object obj = PluginBase.getCraftPlayerMethod(this.bukkit(), "ping");
                return obj instanceof Integer ? Math.max((int) obj, 0) : 0;
            } catch (Exception ignored) {
                return 0;
            }
        }
    }

    public UUID getUUID() {
        return ProtocolLib.isTemporary(this.bukkit())
                ? UUID.randomUUID()
                : this.bukkit().getUniqueId();
    }

    public PlayerProfile profile() {
        return ResearchEngine.getPlayerProfile(this);
    }

    public void setOnGround(boolean isOnGround) {
        this.onGroundFrom = this.onGround;
        this.onGround = isOnGround;
    }

    public void addRawLocation(Location location) {
        synchronized (this.positionHistory) {
            this.positionHistory.add(location.clone());

            if (this.positionHistory.size() > 20) {
                Iterator<Location> iterator = this.positionHistory.iterator();
                iterator.next();
                iterator.remove();
            }
        }
    }

    public World getWorld() {
        return this.getLocation().getWorld();
    }

    public boolean packetsEnabled() {
        return PluginBase.packetsEnabled() && !this.isBedrockPlayer();
    }

    public boolean isBedrockPlayer() {
        return this.dataType == Check.DataType.BEDROCK;
    }

    public boolean isLowEyeHeight() {
        return this.getEyeHeight() < 1.0;
    }

    public boolean isJumping(double d) {
        return PlayerUtils.isJumping(
                d,
                PlayerUtils.getPotionLevel(this, PotionEffectUtils.JUMP) + 1,
                GroundUtils.maxHeightLengthRatio
        );
    }

    public boolean justJumped(double d) {
        return PlayerUtils.justJumped(
                d,
                PlayerUtils.getPotionLevel(this, PotionEffectUtils.JUMP) + 1,
                GroundUtils.maxHeightLengthRatio
        );
    }

    public List<ServerLocation> getLocations() {
        return new ArrayList<>(this.locations.values());
    }

    public Set<Map.Entry<Long, ServerLocation>> getLocationEntries() {
        synchronized (this.locations) {
            return new HashSet<>(locations.entrySet());
        }
    }

    public boolean processLastMoveEvent(
            Location originalTo,
            Location vehicle,
            ServerLocation to,
            Location from,
            boolean packets
    ) {
        if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_17)
                || this.isUsingVersionOrGreater(MultiVersion.MCVersion.V1_17)) {
            double x = clampMin(to.getX(), -3.0E7D, 3.0E7D),
                    y = clampMin(to.getY(), -2.0E7D, 2.0E7D),
                    z = clampMin(to.getZ(), -3.0E7D, 3.0E7D);
            org.bukkit.util.Vector vector = new Vector(x, y, z);
            double d = this.clampVector.distanceSquared(vector);
            this.clampVector = vector;

            if (d < 4e-8) {
                return false;
            }
        }
        if (vehicle == null) {
            Location known = this.getLocation();

            if (known.getX() != originalTo.getX()
                    || known.getY() != originalTo.getY()
                    || known.getZ() != originalTo.getZ()
                    || known.getYaw() != originalTo.getYaw()
                    || known.getPitch() != originalTo.getPitch()) {
                ServerLocation clocation = new ServerLocation(originalTo);

                synchronized (this.locations) {
                    if (this.locations.size() == TPS.maximum) {
                        Iterator<Long> iterator = this.locations.keySet().iterator();
                        iterator.next();
                        iterator.remove();
                    }
                    this.locations.put(System.currentTimeMillis(), clocation);
                }
            }
        }
        if (!packets) {
            this.setFrom(from);
        }
        return true;
    }

    private double clampMin(double d, double d2, double d3) {
        return d < d2 ? d2 : Math.min(d, d3);
    }

    public void setLastInteraction() {
        this.lastInteraction = System.currentTimeMillis();
    }

    private void checkForAFK() {
        long lastInteraction = System.currentTimeMillis() - this.lastInteraction;

        if (lastInteraction >= 20_000L) {
            if (!afk) {
                this.profile().getContinuity().setActiveTime(
                        System.currentTimeMillis(),
                        this.getActiveTimePlayed() - lastInteraction,
                        true
                );
                this.afk = true;
            }
        } else if (this.afk) {
            this.resetActiveCreationTime();
            this.afk = false;
        }
    }

    public boolean isAFK() {
        return this.afk || this.npc;
    }

    @Override
    public String toString() {
        return this.bukkit().getName();
    }

    public boolean debug(boolean function, boolean broadcast, boolean cutDecimals, Object... message) {
        if (function) {
            if (this.bukkit().isWhitelisted()) {
                String string = "§f" + this.bukkit().getName() + " ";

                if (message == null || message.length == 0) {
                    string += new Random().nextInt();
                } else {
                    int i = 0;

                    if (cutDecimals) {
                        for (Object object : message) {
                            if (object instanceof Double) {
                                message[i] = ((i + 1) % 2 == 0 ? "§c" : "§7")
                                        + AlgebraUtils.cut((double) object, GroundUtils.maxHeightLength);
                            } else {
                                message[i] = ((i + 1) % 2 == 0 ? "§c" : "§7") + object;
                            }
                            i++;
                        }
                    } else {
                        for (Object object : message) {
                            message[i] = ((i + 1) % 2 == 0 ? "§c" : "§7") + object;
                            i++;
                        }
                    }
                    string += StringUtils.toString(message, " ");
                }
                Collection<PlayerProtocol> protocols = PluginBase.getProtocols();

                if (!protocols.isEmpty()) {
                    if (broadcast) {
                        Bukkit.broadcastMessage(string);
                    } else {
                        for (PlayerProtocol protocol : protocols) {
                            if (protocol.bukkit().isOp()) {
                                protocol.bukkit().sendMessage(string);
                            }
                        }
                    }
                }
                Bukkit.getConsoleSender().sendMessage(string);
                return true;
            }
        }
        return false;
    }

    public int getMaxChatLength() {
        return this.isBedrockPlayer() ? 512 :
                MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_11)
                        || this.isUsingVersionOrGreater(MultiVersion.MCVersion.V1_11)
                        || PluginUtils.exists("viaversion")
                        || Compatibility.CompatibilityType.PROTOCOL_SUPPORT.isFunctional() ? 256 :
                        100;
    }

    public void sendImportantMessage(String message) {
        this.bukkit().sendMessage("");
        this.bukkit().sendMessage(message);
        this.bukkit().sendMessage("");
    }

    public Inventory createInventory(int size, String title) {
        return Bukkit.createInventory(this.bukkit(), size, title);
    }

    public ItemStack getItemInHand() {
        return this.bukkit().getInventory().getItemInHand();
    }

    public ServerLocation getTargetBlock(double distance, double limit) {
        for (int i = 0; i < AlgebraUtils.integerCeil(Math.min(distance, limit)); i++) {
            ServerLocation location = new ServerLocation(
                    this.getLocation().clone().add(
                            0,
                            this.getEyeHeight(),
                            0
                    ).add(
                            this.getLocation().getDirection().multiply(i)
                    )
            );

            if (BlockUtils.isFullSolid(location.getBlock().getType())) {
                return location.getBlockLocation();
            }
        }
        return null;
    }

    public boolean isFrozen() {
        if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_17)) {
            return this.bukkit().isFrozen();
        } else {
            return false;
        }
    }

    public float getAttackCooldown() {
        if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_17)) {
            return this.bukkit().getAttackCooldown();
        } else {
            return 1.0f;
        }
    }

    public Entity getVehicle() {
        return ProtocolLib.getVehicle(this.bukkit());
    }

    public double getHealth() {
        return ProtocolLib.isTemporary(this.bukkit())
                ? 0.0
                : this.bukkit().getHealth();
    }

    public int getEntityId() {
        return ProtocolLib.isTemporary(this.bukkit())
                ? new Random().nextInt()
                : this.bukkit().getEntityId();
    }

    private void setStoredPotionEffects() {
        if (!ProtocolLib.isTemporary(this.bukkit())) {
            PluginBase.transferTask(
                    this,
                    () -> {
                        for (PotionEffect effect : this.bukkit().getActivePotionEffects()) {
                            this.potionEffects.put(effect.getType(), new ExtendedPotionEffect(effect));
                        }
                    }
            );
        }
    }

    public Collection<PotionEffect> getActivePotionEffects() {
        return ProtocolLib.isTemporary(this.bukkit())
                ? new ArrayList<>(0)
                : this.bukkit().getActivePotionEffects();
    }

    public ExtendedPotionEffect getPotionEffect(PotionEffectType type, long lastActive) {
        this.setStoredPotionEffects();
        ExtendedPotionEffect potionEffect = this.potionEffects.get(type);

        if (potionEffect != null
                && potionEffect.timePassed() <= lastActive
                && potionEffect.bukkitEffect.getType().equals(type)) {
            return potionEffect;
        } else {
            return null;
        }
    }

    public boolean hasPotionEffect(PotionEffectType type, long lastActive) {
        this.setStoredPotionEffects();
        ExtendedPotionEffect potionEffect = this.potionEffects.get(type);
        return potionEffect != null
                && potionEffect.timePassed() <= lastActive
                && potionEffect.bukkitEffect.getType().equals(type);
    }

    public GameMode getGameMode() {
        return ProtocolLib.isTemporary(this.bukkit())
                ? GameMode.SURVIVAL
                : this.bukkit.getGameMode();
    }

    public List<Entity> getNearbyEntities(double radius) {
        this.maxNearbyEntitiesCoordinate[0] = Math.max(radius, this.maxNearbyEntitiesCoordinate[0]);
        this.maxNearbyEntitiesCoordinate[1] = Math.max(radius, this.maxNearbyEntitiesCoordinate[1]);
        this.maxNearbyEntitiesCoordinate[2] = Math.max(radius, this.maxNearbyEntitiesCoordinate[2]);

        if (PluginBase.isSynchronised()) {
            List<Entity> list = this.bukkit().getNearbyEntities(radius, radius, radius);
            this.nearbyEntities = list.toArray(new Entity[0]);
            return list;
        } else if (this.nearbyEntities.length == 0) {
            return new ArrayList<>(0);
        } else {
            List<Entity> entities = new ArrayList<>();
            Location location = this.getLocation();
            int smallX = MathHelper.floor_double((location.getX() - radius) / PlayerUtils.chunk);
            int bigX = MathHelper.floor_double((location.getX() + radius) / PlayerUtils.chunk);
            int smallZ = MathHelper.floor_double((location.getZ() - radius) / PlayerUtils.chunk);
            int bigZ = MathHelper.floor_double((location.getZ() + radius) / PlayerUtils.chunk);
            double radiusSquared = radius * radius;

            for (int xx = smallX; xx <= bigX; xx++) {
                for (int zz = smallZ; zz <= bigZ; zz++) {
                    for (Entity entity : nearbyEntities) {
                        Location eLoc = ProtocolLib.getLocationOrNull(entity);

                        if (eLoc != null) {
                            if (ServerLocation.distanceSquared(
                                    eLoc,
                                    location
                            ) <= radiusSquared) {
                                entities.add(entity);
                            }
                        }
                    }
                }
            }
            entities.remove(this.bukkit());
            return entities;
        }
    }

    public List<Entity> getNearbyEntities(double x, double y, double z) {
        this.maxNearbyEntitiesCoordinate[0] = Math.max(x, this.maxNearbyEntitiesCoordinate[0]);
        this.maxNearbyEntitiesCoordinate[1] = Math.max(y, this.maxNearbyEntitiesCoordinate[1]);
        this.maxNearbyEntitiesCoordinate[2] = Math.max(z, this.maxNearbyEntitiesCoordinate[2]);

        if (PluginBase.isSynchronised()) {
            List<Entity> list = this.bukkit().getNearbyEntities(x, y, z);
            this.nearbyEntities = list.toArray(new Entity[0]);
            return list;
        } else if (this.nearbyEntities.length == 0) {
            return new ArrayList<>(0);
        } else {
            List<Entity> entities = new ArrayList<>();
            Location location = this.getLocation();
            int smallX = MathHelper.floor_double((location.getX() - x) / PlayerUtils.chunk);
            int bigX = MathHelper.floor_double((location.getX() + x) / PlayerUtils.chunk);
            int smallZ = MathHelper.floor_double((location.getZ() - z) / PlayerUtils.chunk);
            int bigZ = MathHelper.floor_double((location.getZ() + z) / PlayerUtils.chunk);

            for (int xx = smallX; xx <= bigX; xx++) {
                for (int zz = smallZ; zz <= bigZ; zz++) {
                    for (Entity entity : nearbyEntities) {
                        Location eLoc = ProtocolLib.getLocationOrNull(entity);

                        if (eLoc != null) {
                            if (Math.abs(eLoc.getX() - location.getX()) <= x
                                    && Math.abs(eLoc.getY() - location.getY()) <= y
                                    && Math.abs(eLoc.getZ() - location.getZ()) <= z) {
                                entities.add(entity);
                            }
                        }
                    }
                }
            }
            entities.remove(this.bukkit());
            return entities;
        }
    }

    // Teleport

    public void groundTeleport() {
        Location location = this.getLocation();
        ServerLocation locationP1 = new ServerLocation(location.clone().add(0, 1, 0));

        if (BlockUtils.isSolid(locationP1.getBlock().getType())
                && !(BlockUtils.areWalls(locationP1.getBlock().getType())
                || BlockUtils.canClimb(locationP1.getBlock().getType(), false))) {
            return;
        }
        World world = this.getWorld();
        double startY = Math.min(BlockUtils.getMaxHeight(world), location.getY());
        int iterations = 0;

        for (double progressiveY = startY; startY > BlockUtils.getMinHeight(world); progressiveY--) {
            ServerLocation loopLocation = new ServerLocation(
                    location.clone().add(0.0, -(startY - progressiveY), 0.0)
            );
            Material material = loopLocation.getBlock().getType();

            if (iterations != PlayerUtils.chunk
                    && (BlockUtils.canClimb(material, false)
                    || BlockUtils.areWalls(material)
                    || !BlockUtils.isSolid(material))) {
                iterations++;
            } else {
                loopLocation.setY(Math.floor(progressiveY) + 1.0);

                if (this.packetsEnabled()) {
                    this.teleport(loopLocation.bukkit());
                }
                break;
            }
        }

        if (iterations > 0) {
            this.bukkit().setFallDistance(0.0f);

            if (Config.settings.getBoolean("Detections.fall_damage_on_teleport")
                    && iterations > PlayerUtils.fallDamageAboveBlocks) { // Damage
                this.bukkit().damage(Math.max(this.bukkit().getFallDistance(), iterations));
            }
        }
    }

    public boolean isOutsideOfTheBorder(double deviation) {
        Location loc = this.getLocation();
        WorldBorder border = this.getWorld().getWorldBorder();
        double size = (border.getSize() / 2.0) + deviation;
        Location center = border.getCenter();
        return Math.abs(loc.getX() - center.getX()) > size
                || Math.abs(loc.getZ() - center.getZ()) > size;
    }

    public double getEyeHeight() {
        return ProtocolLib.getEyeHeight(this.bukkit());
    }

    public boolean isGliding() {
        if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_9)) {
            if (this.bukkit().isGliding()) {
                this.lastGlide = System.currentTimeMillis();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isSwimming() {
        if (MultiVersion.isOrGreater(MultiVersion.MCVersion.V1_13)) {
            return this.bukkit().isSwimming();
        } else {
            return false;
        }
    }

    public boolean wasGliding() {
        return this.isGliding()
                || System.currentTimeMillis() - this.lastGlide <= TPS.maximum * TPS.tickTime;
    }

    public boolean isFlying() {
        Entity vehicle = this.getVehicle();
        boolean flying;

        if (vehicle != null) {
            flying = vehicle instanceof Player && ((Player) vehicle).isFlying();
        } else {
            flying = this.bukkit().isFlying();
        }
        if (flying) {
            this.lastFlight = System.currentTimeMillis();
        }
        return flying;
    }

    public boolean wasFlying() {
        return this.isFlying()
                || System.currentTimeMillis() - this.lastFlight <= TPS.maximum * TPS.tickTime;
    }

}