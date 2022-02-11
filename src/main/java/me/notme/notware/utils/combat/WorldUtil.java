package me.notme.notware.utils.combat;

import me.notme.notware.modules.main.SurroundPlus;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.Arrays;
import java.util.List;

public class WorldUtil {
    public static MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean placeBlockMainHand(BlockPos pos) {
        return placeBlockMainHand(pos, true);
    }

    public static boolean placeBlockMainHand(BlockPos pos, Boolean rotate) {
        return placeBlockMainHand(pos, rotate, true);
    }

    public static boolean placeBlockMainHand(BlockPos pos, Boolean rotate, Boolean airPlace) {
        return placeBlockMainHand(pos, rotate, airPlace, false);
    }

    public static boolean placeBlockMainHand(BlockPos pos, Boolean rotate, Boolean airPlace, Boolean ignoreEntity) {
        return placeBlockMainHand(pos, rotate, airPlace, ignoreEntity, null);
    }

    public static boolean placeBlockMainHand(BlockPos pos, Boolean rotate, Boolean airPlace, Boolean ignoreEntity, Direction overrideSide) {
        return placeBlock(Hand.MAIN_HAND, pos, rotate, airPlace, ignoreEntity, overrideSide);
    }

    public static boolean placeBlockNoRotate(Hand hand, BlockPos pos) {
        return placeBlock(hand, pos, false, true, false);
    }

    public static boolean placeBlock(Hand hand, BlockPos pos) {
        placeBlock(hand, pos, true, false);
        return true;
    }

    public static boolean placeBlock(Hand hand, BlockPos pos, Boolean rotate) {
        placeBlock(hand, pos, rotate, false);
        return true;
    }

    public static boolean placeBlock(Hand hand, BlockPos pos, Boolean rotate, Boolean airPlace) {
        placeBlock(hand, pos, rotate, airPlace, false);
        return true;
    }

    public static boolean placeBlock(Hand hand, BlockPos pos, Boolean rotate, Boolean airPlace, Boolean ignoreEntity) {
        placeBlock(hand, pos, rotate, airPlace, ignoreEntity, null);
        return true;
    }

    public static boolean placeBlock(Hand hand, BlockPos pos, Boolean rotate, Boolean airPlace, Boolean ignoreEntity, Direction overrideSide) {
        if (ignoreEntity) {
            if (!mc.world.getBlockState(pos).getMaterial().isReplaceable())
                return false;
        } else if (!mc.world.getBlockState(pos).getMaterial().isReplaceable() || !mc.world.canPlace(Blocks.OBSIDIAN.getDefaultState(), pos, ShapeContext.absent()))
            return false;

        Vec3d eyesPos = new Vec3d(mc.player.getX(),
            mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
            mc.player.getZ());

        Vec3d hitVec = null;
        BlockPos neighbor = null;
        Direction side2 = null;

        if (overrideSide != null) {
            neighbor = pos.offset(overrideSide.getOpposite());
            side2 = overrideSide;
        }

        for (Direction side : Direction.values()) {
            if (overrideSide == null) {
                neighbor = pos.offset(side);
                side2 = side.getOpposite();

                if (mc.world.getBlockState(neighbor).isAir() || mc.world.getBlockState(neighbor).getBlock() instanceof FluidBlock || (Modules.get().get(SurroundPlus.class).ignoreOpenable.get() &&
                    mc.world.getBlockState(neighbor).getBlock() instanceof AnvilBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof CraftingTableBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof ChestBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof TrappedChestBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof BarrelBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof EnderChestBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof ShulkerBoxBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof FurnaceBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof LoomBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof CartographyTableBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof FletchingTableBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof GrindstoneBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof SmithingTableBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof StonecutterBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof BlastFurnaceBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof SmokerBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof HopperBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof DispenserBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof DropperBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof LecternBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof BeaconBlock
                    || mc.world.getBlockState(neighbor).getBlock() instanceof EnchantingTableBlock)) {
                    neighbor = null;
                    side2 = null;
                    continue;
                }
            }

            hitVec = new Vec3d(neighbor.getX(), neighbor.getY(), neighbor.getZ()).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getUnitVector()).multiply(0.5));
            break;
        }

        if (airPlace) {
            if (hitVec == null) hitVec = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
            if (neighbor == null) neighbor = pos;
            if (side2 == null) side2 = Direction.UP;
        } else if (hitVec == null || neighbor == null || side2 == null) {
            return false;
        }

        double diffX = hitVec.x - eyesPos.x;
        double diffY = hitVec.y - eyesPos.y;
        double diffZ = hitVec.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        float[] rotations = {
            mc.player.getYaw()
                + MathHelper.wrapDegrees(yaw - mc.player.getYaw()),
            mc.player.getPitch() + MathHelper
                .wrapDegrees(pitch - mc.player.getPitch())};

        if (rotate)
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rotations[0], rotations[1], mc.player.isOnGround()));

        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        mc.interactionManager.interactBlock(mc.player, mc.world, hand, new BlockHitResult(hitVec, side2, neighbor, false));
        mc.player.swingHand(hand);
        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

        return true;
    }

    public static final List<Block> NONSOLID_BLOCKS = Arrays.asList(
        Blocks.AIR, Blocks.LAVA, Blocks.WATER, Blocks.GRASS,
        Blocks.VINE, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS,
        Blocks.SNOW, Blocks.TALL_GRASS, Blocks.FIRE, Blocks.VOID_AIR);

    public static boolean canReplace(BlockPos pos) {
        return NONSOLID_BLOCKS.contains(mc.world.getBlockState(pos).getBlock()) && mc.world.getOtherEntities(null, new Box(pos)).stream().noneMatch(Entity::collides);
    }

    public static void moveEntityWithSpeed(Entity entity, double speed, boolean shouldMoveY) {
        float yaw = (float) Math.toRadians(mc.player.getYaw());

        double motionX = 0;
        double motionY = 0;
        double motionZ = 0;

        if (mc.player.input.pressingForward && mc.player.input.pressingLeft) {
            motionX = (MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
            motionZ = (MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
        } else if (mc.player.input.pressingLeft && mc.player.input.pressingBack) {
            motionX = (MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
            motionZ = -(MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
        } else if (mc.player.input.pressingBack && mc.player.input.pressingRight) {
            motionX = -(MathHelper.cos(yaw) * speed) + (MathHelper.sin(yaw) * speed);
            motionZ = -(MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
        } else if (mc.player.input.pressingRight && mc.player.input.pressingForward) {
            motionX = -(MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
            motionZ = (MathHelper.cos(yaw) * speed) - (MathHelper.sin(yaw) * speed);
        }

        entity.setVelocity(motionX, motionY, motionZ);
    }

    public static void rotate(float yaw, float pitch) {
        mc.player.getYaw(yaw);
        mc.player.getPitch(pitch);
    }

    public static void rotate(double[] rotations) {
        mc.player.getYaw((float) rotations[0]);
        mc.player.getPitch((float) rotations[1]);
    }

    public static BlockPos roundBlockPos(Vec3d vec) {
        return new BlockPos(vec.x, (int) Math.round(vec.y), vec.z);
    }

    public static void snapPlayer() {
        BlockPos lastPos = mc.player.isOnGround() ? WorldUtil.roundBlockPos(mc.player.getPos()) : mc.player.getBlockPos();
        snapPlayer(lastPos);
    }

    public static void snapPlayer(BlockPos lastPos) {
        double xPos = mc.player.getPos().x;
        double zPos = mc.player.getPos().z;

        if (Math.abs((lastPos.getX() + 0.5) - mc.player.getPos().x) >= 0.2) {
            int xDir = (lastPos.getX() + 0.5) - mc.player.getPos().x > 0 ? 1 : -1;
            xPos += 0.3 * xDir;
        }

        if (Math.abs((lastPos.getZ() + 0.5) - mc.player.getPos().z) >= 0.2) {
            int zDir = (lastPos.getZ() + 0.5) - mc.player.getPos().z > 0 ? 1 : -1;
            zPos += 0.3 * zDir;
        }

        mc.player.setVelocity(0, 0, 0);
        mc.player.updatePosition(xPos, mc.player.getY(), zPos);
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
    }
}
