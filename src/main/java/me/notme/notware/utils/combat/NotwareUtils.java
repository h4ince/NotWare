package me.notme.notware.utils.combat;

import me.notme.notware.utils.misc.Members;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotwareUtils {
    public static int CPS = 0;
    public static MinecraftClient mc;

    public static void init() {
        mc = MinecraftClient.getInstance();
        new Timer().scheduleAtFixedRate(newTimerTaskFromLambda(() -> CPS = 0), 0, 1000);
    }

    public static TimerTask newTimerTaskFromLambda(Runnable runnable) {
        return new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }

    public static double squaredDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;
        return dX * dX + dY * dY + dZ * dZ;
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distance(double y1, double y2) {
        double dY = y2 - y1;
        return Math.sqrt(dY * dY);
    }

    public static double distance(Vec3d vec1, Vec3d vec2) {
        double dX = vec2.x - vec1.x;
        double dY = vec2.y - vec1.y;
        double dZ = vec2.z - vec1.z;
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distance(BlockPos block1, BlockPos block2) {
        double dX = block2.getX() - block1.getX();
        double dY = block2.getY() - block1.getY();
        double dZ = block2.getZ() - block1.getZ();
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distanceBetweenXZ(BlockPos pos1, BlockPos pos2) {
        double d = pos1.getX() - pos2.getX();
        double f = pos1.getZ() - pos2.getZ();
        return MathHelper.sqrt((float) (d * d + f * f));
    }

    // Blocks

    public static Block getBlock(BlockPos pos) {
        if (pos == null) return null;
        return mc.world.getBlockState(pos).getBlock();
    }

    public static BlockState getBlockState(BlockPos pos) {
        if (pos == null) return null;
        return mc.world.getBlockState(pos);
    }

    public static VoxelShape getCollision(BlockPos pos) {
        return getBlockState(pos).getCollisionShape(mc.world, pos);
    }

    public static boolean isSolid(BlockPos pos) {
        return getBlockState(pos).isSolidBlock(mc.world, pos);
    }

    public static boolean isSurroundBlock(BlockPos blockPos) {
        Block block = mc.world.getBlockState(blockPos).getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.RESPAWN_ANCHOR || block == Blocks.ANVIL;
    }

    public static boolean isBlastResistant(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().getBlastResistance() >= 600;
    }

    public static boolean isSurrounded(LivingEntity targetEntity) {
        assert mc.world != null;
        return isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1));
    }

    public static boolean isGreenHole(LivingEntity target) {
        assert mc.world != null;
        return mc.world.getBlockState(target.getBlockPos().add(1, 0, 0)).isOf(Blocks.BEDROCK) && mc.world.getBlockState(target.getBlockPos().add(-1, 0, 0)).isOf(Blocks.BEDROCK) && mc.world.getBlockState(target.getBlockPos().add(0, 0, 1)).isOf(Blocks.BEDROCK) && mc.world.getBlockState(target.getBlockPos().add(0, 0, -1)).isOf(Blocks.BEDROCK);
    }

    public static float getTotalHealthLiving(LivingEntity target) {
        return target.getHealth() + target.getAbsorptionAmount();
    }

    public static boolean isBedrock(BlockPos pos) {
        return mc.world.getBlockState(pos).isOf(Blocks.BEDROCK);
    }

    public static boolean isSurroundBroken(LivingEntity targetEntity) {
        assert mc.world != null;
        return (!isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1))) || (isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0)) && !isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1))) || (isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0)) && !isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1))) || (isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0)) && isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1)) && !isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1)));
    }

    public static boolean inFace(LivingEntity target) {
        assert mc.world != null;
        return isBlastResistant(target.getBlockPos().add(1, 1, 0)) && isBlastResistant(target.getBlockPos().add(-1, 1, 0)) && isBlastResistant(target.getBlockPos().add(0, 1, 1)) && isBlastResistant(target.getBlockPos().add(0, 1, -1));
    }

    public static boolean isBurrowedLiving(LivingEntity p) {
        if(p == null) return false;
        if(mc.world == null) return false;
        return mc.world.getBlockState(p.getBlockPos()).getBlock() == Blocks.ENDER_CHEST || mc.world.getBlockState(p.getBlockPos()).getBlock() == Blocks.OBSIDIAN;
    }

    public static boolean isSurroundBlock(Block block) {
        return block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.RESPAWN_ANCHOR || block == Blocks.ANVIL;
    }

    private static final Vec3d hitPos = new Vec3d(0.0D, 0.0D, 0.0D);

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, int rotationPriority) {
        return place(blockPos, findItemResult, rotationPriority, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority) {
        return place(blockPos, findItemResult, rotate, rotationPriority, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean checkEntities) {
        return place(blockPos, findItemResult, rotate, rotationPriority, true, checkEntities);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, int rotationPriority, boolean checkEntities) {
        return place(blockPos, findItemResult, true, rotationPriority, true, checkEntities);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities) {
        return place(blockPos, findItemResult, rotate, rotationPriority, swingHand, checkEntities, true);
    }

    public static boolean place(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities, boolean swapBack) {
        if (findItemResult.isOffhand()) {
            return place(blockPos, Hand.OFF_HAND, mc.player.getInventory().selectedSlot, rotate, rotationPriority, swingHand, checkEntities, swapBack);
        } else {
            return findItemResult.isHotbar() ? place(blockPos, Hand.MAIN_HAND, findItemResult.getSlot(), rotate, rotationPriority, swingHand, checkEntities, swapBack) : false;
        }
    }

    public static boolean place(BlockPos blockPos, Hand hand, int slot, boolean rotate, int rotationPriority, boolean swingHand, boolean checkEntities, boolean swapBack) {
        if (slot >= 0 && slot <= 8) {
            if (!canPlace(blockPos, checkEntities)) {
                return false;
            } else {
                ((IVec3d)hitPos).set((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D);
                Direction side = getPlaceSide(blockPos);
                BlockPos neighbour;
                if (side == null) {
                    side = Direction.UP;
                    neighbour = blockPos;
                } else {
                    neighbour = blockPos.offset(side.getOpposite());
                    hitPos.add((double) side.getOffsetX() * 0.5D, (double) side.getOffsetY() * 0.5D, (double) side.getOffsetZ() * 0.5D);
                }

                if (rotate) {
                    Direction finalSide = side;
                    Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos), rotationPriority, () -> {
                        InvUtils.swap(slot, swapBack);

                        place(new BlockHitResult(hitPos, finalSide, neighbour, false), hand, swingHand);

                        if (swapBack) InvUtils.swapBack();
                    });
                } else {
                    InvUtils.swap(slot, swapBack);

                    place(new BlockHitResult(hitPos, side, neighbour, false), hand, swingHand);

                    if (swapBack) InvUtils.swapBack();
                }

                return true;
            }
        } else {
            return false;
        }
    }

    private static void place(BlockHitResult blockHitResult, Hand hand, boolean swing) {
        boolean wasSneaking = mc.player.input.sneaking;
        mc.player.input.sneaking = false;
        ActionResult result = mc.interactionManager.interactBlock(mc.player, mc.world, hand, blockHitResult);
        if (result.shouldSwingHand()) {
            if (swing) {
                mc.player.swingHand(hand);
            } else {
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
            }
        }

        mc.player.input.sneaking = wasSneaking;
    }

    public static boolean canPlace(BlockPos pos, boolean checkEntities) {
        if (pos == null || mc.world == null) return false;

        // Check y level
        if (!World.isValid(pos)) return false;

        // Check if current block is replaceable
        if (!getBlockState(pos).getMaterial().isReplaceable()) return false;

        // Check for entities or if the block is air
        return checkEntities ? mc.world.canPlace(Blocks.OBSIDIAN.getDefaultState(), pos, ShapeContext.absent()) : getBlockState(pos).getMaterial().isReplaceable();
    }

    public static Direction getPlaceSide(BlockPos blockPos) {
        Direction[] var1 = Direction.values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Direction side = var1[var3];
            BlockPos neighbor = blockPos.offset(side);
            Direction side2 = side.getOpposite();
            BlockState state = MeteorClient.mc.world.getBlockState(neighbor);
            if (!state.isAir() && !isClickable(state.getBlock()) && state.getFluidState().isEmpty()) {
                return side2;
            }
        }

        return null;
    }

    public static boolean isClickable(Block block) {
        return block instanceof CraftingTableBlock || block instanceof AnvilBlock || block instanceof AbstractButtonBlock || block instanceof AbstractPressurePlateBlock || block instanceof BlockWithEntity || block instanceof BedBlock || block instanceof FenceGateBlock || block instanceof DoorBlock || block instanceof NoteBlock || block instanceof TrapdoorBlock;
    }

    public static boolean canPlace(BlockPos pos) {
        return canPlace(pos, true);
    }

    public static ArrayList<BlockPos> getPositionsAroundPlayer(double range) {
        double pX = mc.player.getX() - 0.5;
        double pY = mc.player.getY();
        double pZ = mc.player.getZ() - 0.5;

        int minX = (int) Math.floor(pX - range);
        int minY = (int) Math.floor(pY - range);
        int minZ = (int) Math.floor(pZ - range);

        int maxX = (int) Math.floor(pX + range);
        int maxY = (int) Math.floor(pY + range);
        int maxZ = (int) Math.floor(pZ + range);

        double rangeSq = Math.pow(range, 2);

        ArrayList<BlockPos> positions = new ArrayList<>();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (NotwareUtils.distance(pX, pY, pZ, x, y, z) <= rangeSq) {
                        BlockPos pos = new BlockPos(x, y, z);

                        if (World.isValid(pos)) positions.add(pos);
                    }
                }
            }
        }

        return positions;
    }

    public static double[] directionSpeed(float speed) {
        float forward = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw);

        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }

        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;

        return new double[] {posX, posZ};
    }

    public static List<String> members = new ArrayList<>();

    public static void members() {
        Thread thread = new Thread(() -> {
            try {
                members = Members.getUserNames();
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });

        thread.start();
    }
}
