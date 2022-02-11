package me.notme.notware.utils.elytra;

import me.notme.notware.modules.elytra.bot.ElytraBaritone;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class MiningUtil1 {
    public static MiningUtil1 miningUtil = new MiningUtil1();
    public static Direction facing;
    public static BlockPos pos;
    public static boolean isRotateSpoofing;
    public static boolean start;
    public static boolean spoofRotation;
    public static boolean isMining;
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean mine(BlockPos pos, boolean spoofRotation) {
        if (!hasPickaxe()) {
            return false;
        } else {
            InvUtils.swap(BotUtils.getSlot(Items.DIAMOND_PICKAXE), false);
            if (InvUtils.findInHotbar(Items.DIAMOND_PICKAXE).getSlot() != -1) {
                MiningUtil1.pos = pos;
                facing = getFacing(pos);
                start = true;
                MiningUtil1.spoofRotation = spoofRotation;
                isMining = true;
                BotUtils.sleepUntil(() -> {
                    return !BotUtils.isSolid(pos);
                }, 15000);
                isMining = false;
                stopRotating();
                return !BotUtils.isSolid(pos);
            } else {
                return false;
            }
        }
    }

    public static void mineAnyway(BlockPos pos, boolean spoofRotation) {
        if (hasPickaxe()) {
            BotUtils.switchItem(BotUtils.getSlot(Items.DIAMOND_PICKAXE), false);
        }

        MiningUtil1.pos = pos;
        facing = getFacing(pos);
        start = true;
        MiningUtil1.spoofRotation = spoofRotation;
        isMining = true;
        BotUtils.sleepUntil(() -> {
            return !BotUtils.isSolid(pos);
        }, 15000);
        isMining = false;
        stopRotating();
    }

    public static boolean mineWithoutSwitch(BlockPos pos) {
        MiningUtil1.pos = pos;
        facing = getFacing(pos);
        start = true;
        BotUtils.sleepUntil(() -> {
            return !BotUtils.isSolid(pos) && BlockUtilsWorld.getBlock(pos) != Blocks.COBWEB;
        }, 6000);
        return !BotUtils.isSolid(pos);
    }

    public static boolean hasPickaxe() {
        return BotUtils.hasItem(Items.DIAMOND_PICKAXE);
    }

    public static boolean canMine(BlockPos pos, VoxelShape shape, BlockState state) {
        if (BotUtils.isSolid(pos) && BlockUtilsWorld.getBlock(pos) != Blocks.BEDROCK && !(BlockUtilsWorld.distanceBetween(pos, mc.player.getBlockPos()) > 7.0D)) {
            Vec3d start = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            Vec3d end = new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            BlockHitResult result = mc.world.raycastBlock(start, end, mc.player.getBlockPos(), shape, state);
            return result != null && result.getBlockPos().equals(pos);
        } else {
            return false;
        }
    }

    public static Direction getFacing(BlockPos pos) {
        Direction closest = null;
        double lowestDistance = 2.147483647E9D;
        Direction[] var4 = Direction.values();
        int var5 = var4.length;

        for (Direction facing : var4) {
            BlockPos neighbor = pos.offset(facing);
            if (!BotUtils.isSolid(neighbor)) {
                double distance = BlockUtilsWorld.distanceBetween(neighbor, mc.player.getBlockPos());
                if (distance < lowestDistance) {
                    closest = facing;
                    lowestDistance = distance;
                }
            }
        }

        return closest;
    }

    public static void rotate(Vec3d vec, boolean sendPacket) {
        float[] rotations = getRotations(vec);
        if (sendPacket) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rotations[0], rotations[1], mc.player.isOnGround()));
        }

        rotations[0] = mc.player.getYaw();
        rotations[1] = mc.player.getPitch();
    }

    public static void stopRotating() {
        isRotateSpoofing = false;
    }

    public static float[] getRotations(Vec3d vec) {
        Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{mc.player.getYaw() + MathHelper.wrapDegrees(yaw - mc.player.getYaw()), mc.player.getPitch() + MathHelper.wrapDegrees(pitch - mc.player.getPitch())};
    }

    public static void BlockRotate(BlockPos blockPos) {
        Vec3d hitPos = new Vec3d(0.0D, 0.0D, 0.0D);
        ((IVec3d) hitPos).set(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
        Rotations.getYaw(hitPos);
        Rotations.getPitch(hitPos);
    }
}
