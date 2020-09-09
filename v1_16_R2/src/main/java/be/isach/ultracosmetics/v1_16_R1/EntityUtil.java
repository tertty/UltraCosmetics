package be.isach.ultracosmetics.v1_16_R2;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.treasurechests.ChestType;
import be.isach.ultracosmetics.treasurechests.TreasureChestDesign;
import be.isach.ultracosmetics.util.MathUtils;
import be.isach.ultracosmetics.util.PacketSender;
import be.isach.ultracosmetics.util.Particles;
import be.isach.ultracosmetics.util.UtilParticles;
import be.isach.ultracosmetics.v1_16_R2.pathfinders.CustomPathFinderGoalPanic;
import be.isach.ultracosmetics.version.IEntityUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.entity.*;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.*;

import static java.lang.Math.*;

/**
 * @authors RadBuilder, iSach
 */
public class EntityUtil implements IEntityUtil {

    private final Random r = new Random();
    private Map<Player, List<EntityArmorStand>> fakeArmorStandsMap = new HashMap<>();
    private Map<Player, List<org.bukkit.entity.Entity>> cooldownJumpMap = new HashMap<>();

    @Override
    public void setPassenger(org.bukkit.entity.Entity vehicle, org.bukkit.entity.Entity passenger) {
        vehicle.setPassenger(passenger);
    }

    @Override
    public void resetWitherSize(Wither wither) {
        ((CraftWither) wither).getHandle().setInvul(600);
    }


    @Override
    public void setHorseSpeed(org.bukkit.entity.Entity horse, double speed) {
        ((CraftAbstractHorse) horse).getHandle().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
    }

    @Override
    public void sendBlizzard(final Player player, Location loc, boolean affectPlayers, Vector v) {
        try {
            if (!fakeArmorStandsMap.containsKey(player))
                fakeArmorStandsMap.put(player, new ArrayList<>());
            if (!cooldownJumpMap.containsKey(player))
                cooldownJumpMap.put(player, new ArrayList<>());

            final List<EntityArmorStand> fakeArmorStands = fakeArmorStandsMap.get(player);
            final List<org.bukkit.entity.Entity> cooldownJump = cooldownJumpMap.get(player);

            final EntityArmorStand as = new EntityArmorStand(EntityTypes.ARMOR_STAND, ((CraftWorld) player.getWorld()).getHandle());
            as.setInvisible(true);
            as.setFlag(5, true);
            as.setSmall(true);
            as.setNoGravity(true);
            as.setArms(true);
            as.setHeadPose(new Vector3f((float) (r.nextInt(360)),
                    (float) (r.nextInt(360)),
                    (float) (r.nextInt(360))));
            as.setLocation(loc.getX() + MathUtils.randomDouble(-1.5, 1.5), loc.getY() + MathUtils.randomDouble(0, .5) - 0.75, loc.getZ() + MathUtils.randomDouble(-1.5, 1.5), 0, 0);
            fakeArmorStands.add(as);
            for (Player players : player.getWorld().getPlayers()) {
                PacketSender.send(players, new PacketPlayOutSpawnEntityLiving(as));
                PacketSender.send(players, new PacketPlayOutEntityMetadata(as.getId(), as.getDataWatcher(), false));
                List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R2.ItemStack>> list = new ArrayList<>();
                list.add(new Pair(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(org.bukkit.Material.PACKED_ICE))));
                PacketSender.send(players, new PacketPlayOutEntityEquipment(as.getId(), list));
            }
            UtilParticles.display(Particles.CLOUD, loc.clone().add(MathUtils.randomDouble(-1.5, 1.5), MathUtils.randomDouble(0, .5) - 0.75, MathUtils.randomDouble(-1.5, 1.5)), 2, 0.4f);
            Bukkit.getScheduler().runTaskLater(UltraCosmeticsData.get().getPlugin(), () -> {
                for (Player pl : player.getWorld().getPlayers())
                    PacketSender.send(pl, new PacketPlayOutEntityDestroy(as.getId()));
                fakeArmorStands.remove(as);
            }, 20);
            if (affectPlayers)
                as.getBukkitEntity().getNearbyEntities(0.5, 0.5, 0.5).stream().filter(ent -> !cooldownJump.contains(ent) && ent != player).forEachOrdered(ent -> {
                    MathUtils.applyVelocity(ent, new Vector(0, 1, 0).add(v));
                    cooldownJump.add(ent);
                    Bukkit.getScheduler().runTaskLater(UltraCosmeticsData.get().getPlugin(), () -> cooldownJump.remove(ent), 20);
                });
        } catch (Exception exc) {

        }
    }

    @Override
    public void clearBlizzard(Player player) {
        if (!fakeArmorStandsMap.containsKey(player)) return;

        for (EntityArmorStand as : fakeArmorStandsMap.get(player)) {
            if (as == null) {
                continue;
            }
            for (Player pl : player.getWorld().getPlayers()) {
                PacketSender.send(pl, new PacketPlayOutEntityDestroy(as.getId()));
            }
        }

        fakeArmorStandsMap.remove(player);
        cooldownJumpMap.remove(player);
    }

    @Override
    public void clearPathfinders(org.bukkit.entity.Entity entity) {
        EntityInsentient nmsEntity = (EntityInsentient) ((CraftEntity) entity).getHandle();
        PathfinderGoalSelector goalSelector = nmsEntity.goalSelector;
        PathfinderGoalSelector targetSelector = nmsEntity.targetSelector;

        try {
            Field brField = EntityLiving.class.getDeclaredField("bn");
            brField.setAccessible(true);
            BehaviorController<?> controller = (BehaviorController<?>) brField.get(nmsEntity);

            Field memoriesField = BehaviorController.class.getDeclaredField("memories");
            memoriesField.setAccessible(true);
            memoriesField.set(controller, new HashMap<>());

            Field sensorsField = BehaviorController.class.getDeclaredField("sensors");
            sensorsField.setAccessible(true);
            sensorsField.set(controller, new LinkedHashMap<>());

            Field cField = BehaviorController.class.getDeclaredField("e");
            cField.setAccessible(true);
            cField.set(controller, new TreeMap<>());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            Field dField;
            dField = PathfinderGoalSelector.class.getDeclaredField("d");
            dField.setAccessible(true);
            dField.set(goalSelector, new LinkedHashSet<>());
            dField.set(targetSelector, new LinkedHashSet<>());

            Field cField;
            cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            dField.set(goalSelector, new LinkedHashSet<>());
            cField.set(targetSelector, new EnumMap<>(PathfinderGoal.Type.class));

            Field fField;
            fField = PathfinderGoalSelector.class.getDeclaredField("f");
            fField.setAccessible(true);
            dField.set(goalSelector, new LinkedHashSet<>());
            fField.set(targetSelector, EnumSet.noneOf(PathfinderGoal.Type.class));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void makePanic(org.bukkit.entity.Entity entity) {
        EntityInsentient insentient = (EntityInsentient) ((CraftEntity) entity).getHandle();
        insentient.goalSelector.a(3, new CustomPathFinderGoalPanic((EntityCreature) insentient, 0.4d));
    }

    @Override
    public void sendDestroyPacket(Player player, org.bukkit.entity.Entity entity) {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(((CraftEntity) entity).getHandle().getId());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void move(Creature creature, Location loc) {
        EntityCreature ec = ((CraftCreature) creature).getHandle();
        ec.G = 1;

        if (loc == null) return;

        ec.aK = loc.getYaw();
        PathEntity path = ec.getNavigation().a(loc.getX(), loc.getY(), loc.getZ(), 1);
        ec.getNavigation().a(path, 2);
    }

    @Override
    public void moveDragon(Player player, Vector vector, org.bukkit.entity.Entity entity) {
        EntityEnderDragon ec = ((CraftEnderDragon) entity).getHandle();

        ec.hurtTicks = -1;
        ec.pitch = player.getLocation().getPitch();
        ec.yaw = player.getLocation().getYaw() - 180;

        float yaw = player.getPlayer().getLocation().getYaw();

        double angleInRadians = toRadians(-yaw);

        double x = sin(angleInRadians);
        double z = cos(angleInRadians);

        Vector v = ec.getBukkitEntity().getLocation().getDirection();

        ec.move(EnumMoveType.SELF, new Vec3D(x, v.getY(), z));
    }

    @Override
    public void setClimb(org.bukkit.entity.Entity entity) {
        ((CraftEntity) entity).getHandle().I = 1;
    }

    @Override
    public void moveShip(Player player, org.bukkit.entity.Entity entity, Vector vector) {
        EntityBoat ec = ((CraftBoat) entity).getHandle();

        ec.getBukkitEntity().setVelocity(vector);

        ec.pitch = player.getLocation().getPitch();
        ec.yaw = player.getLocation().getYaw() - 180;

        ec.move(EnumMoveType.SELF, new Vec3D(1, 0, 0));
    }

    @Override
    public void playChestAnimation(Block b, boolean open, TreasureChestDesign design) {
        Location location = b.getLocation();
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        if (design.getChestType() == ChestType.ENDER) {
            TileEntityEnderChest tileChest = (TileEntityEnderChest) world.getTileEntity(position);
            world.playBlockAction(position, tileChest.getBlock().getBlock(), 1, open ? 1 : 0);
        } else {
            TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
            world.playBlockAction(position, tileChest.getBlock().getBlock(), 1, open ? 1 : 0);
        }
    }

    @Override
    public org.bukkit.entity.Entity spawnItem(org.bukkit.inventory.ItemStack itemStack, Location blockLocation) {
        EntityItem ei = new EntityItem(
                ((CraftWorld) blockLocation.clone().add(0.5D, 1.2D, 0.5D).getWorld()).getHandle(),
                blockLocation.clone().add(0.5D, 1.2D, 0.5D).getX(),
                blockLocation.clone().add(0.5D, 1.2D, 0.5D).getY(),
                blockLocation.clone().add(0.5D, 1.2D, 0.5D).getZ(),
                CraftItemStack.asNMSCopy(itemStack)) {
        };
        ei.getBukkitEntity().setVelocity(new Vector(0.0D, 0.25D, 0.0D));
        ei.pickupDelay = 2147483647;
        ei.getBukkitEntity().setCustomName(UltraCosmeticsData.get().getItemNoPickupString());
        ei.pickupDelay = 20;

        ((CraftWorld) blockLocation.clone().add(0.5D, 1.2D, 0.5D).getWorld()).getHandle().addEntity(ei);

        return ei.getBukkitEntity();
    }

    @Override
    public boolean isSameInventory(Inventory first, Inventory second) {
        return ((CraftInventory) first).getInventory().equals(((CraftInventory) second).getInventory());
    }

    @Override
    public void follow(org.bukkit.entity.Entity toFollow, org.bukkit.entity.Entity follower) {
        Entity pett = ((CraftEntity) follower).getHandle();
        ((EntityInsentient) pett).getNavigation().a(2);
        Object petf = ((CraftEntity) follower).getHandle();
        Location targetLocation = toFollow.getLocation();
        PathEntity path;
        path = ((EntityInsentient) petf).getNavigation().a(targetLocation.getX() + 1, targetLocation.getY(), targetLocation.getZ() + 1, 1);
        if (path != null) {
            ((EntityInsentient) petf).getNavigation().a(path, 1.05D);
            ((EntityInsentient) petf).getNavigation().a(1.05D);
        }
    }

    @Override
    public void chickenFall(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        if (!entityPlayer.isOnGround() && entityPlayer.getMot().getY() < 0.0D) {
            Vector v = player.getVelocity();
            player.setVelocity(v);
            entityPlayer.setMot(entityPlayer.getMot().a(0.85));
        }
    }

    @Override
    public void sendTeleportPacket(Player player, org.bukkit.entity.Entity entity) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(((CraftEntity) entity).getHandle()));
    }

    @Override
    public boolean isMoving(Player entity) {
        return false;
    }

    @Override
    public byte[] getEncodedData(String url) {
        return Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
    }
}
