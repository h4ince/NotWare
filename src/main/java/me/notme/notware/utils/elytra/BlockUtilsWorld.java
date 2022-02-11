package me.notme.notware.utils.elytra;

import me.notme.notware.modules.main.BedAura;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockUtilsWorld {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static ArrayList<Vec3d> surround = new ArrayList<Vec3d>() {
        {
            add(new Vec3d(1.0D, 0.0D, 0.0D));
            add(new Vec3d(-1.0D, 0.0D, 0.0D));
            add(new Vec3d(0.0D, 0.0D, 1.0D));
            add(new Vec3d(0.0D, 0.0D, -1.0D));
        }
    };
    public static ArrayList<Vec3d> selfTrap = new ArrayList<Vec3d>() {
        {
            add(new Vec3d(1.0D, 1.0D, 0.0D));
            add(new Vec3d(-1.0D, 1.0D, 0.0D));
            add(new Vec3d(0.0D, 1.0D, 1.0D));
            add(new Vec3d(0.0D, 1.0D, -1.0D));
        }
    };
    public static ArrayList<Vec3d> head = new ArrayList<Vec3d>() {
        {
            add(new Vec3d(0.0D, 2.0D, 0.0D));
        }
    };
    private static final SetBlockResult RESULT = new SetBlockResult();

    public static boolean isTrapBlock(BlockPos pos) {
        return getBlock(pos) == Blocks.OBSIDIAN || getBlock(pos) == Blocks.ENDER_CHEST || getBlock(pos) == Blocks.BEDROCK;
    }

    public static Block getBlock(BlockPos p) {
        return p == null ? null : mc.world.getBlockState(p).getBlock();
    }

    public static boolean isBurrowed(PlayerEntity p) {
        if (p == null) {
            return false;
        } else if (mc.world == null) {
            return false;
        } else {
            return mc.world.getBlockState(p.getBlockPos()).getBlock() == Blocks.ENDER_CHEST || mc.world.getBlockState(p.getBlockPos()).getBlock() == Blocks.OBSIDIAN;
        }
    }
    public static boolean isTrapped(PlayerEntity p) {
        BlockPos south = p.getBlockPos().up().south();
        BlockPos north = p.getBlockPos().up().north();
        BlockPos east = p.getBlockPos().up().east();
        BlockPos west = p.getBlockPos().up().west();
        BlockPos head = p.getBlockPos().up(2);
        return isTrapBlock(south) && isTrapBlock(north) && isTrapBlock(east) && isTrapBlock(west) && isTrapBlock(head);
    }

    public static boolean isSurrounded(PlayerEntity p) {
        BlockPos south = p.getBlockPos().south();
        BlockPos north = p.getBlockPos().north();
        BlockPos east = p.getBlockPos().east();
        BlockPos west = p.getBlockPos().west();
        return isTrapBlock(south) && isTrapBlock(north) && isTrapBlock(east) && isTrapBlock(west);
    }

    public static BlockPos LowestDist(PlayerEntity a) {
        BlockPos south = a.getBlockPos().up(2).south();
        BlockPos north = a.getBlockPos().up(2).north();
        BlockPos east = a.getBlockPos().up(2).east();
        BlockPos west = a.getBlockPos().up(2).west();
        double south1 = distanceBetween(mc.player.getBlockPos().up(), south);
        double north1 = distanceBetween(mc.player.getBlockPos().up(), north);
        double east1 = distanceBetween(mc.player.getBlockPos().up(), east);
        double west1 = distanceBetween(mc.player.getBlockPos().up(), west);
        if (south1 < north1 && south1 < east1 && south1 < west1 && south1 <= 6.0D && mc.world.getBlockState(south).getMaterial().isReplaceable()) {
            return south;
        } else if (east1 < north1 && east1 < south1 && east1 < west1 && south1 <= 6.0D && mc.world.getBlockState(east).getMaterial().isReplaceable()) {
            return east;
        } else if (north1 < south1 && north1 < east1 && north1 < west1 && north1 <= 6.0D && mc.world.getBlockState(north).getMaterial().isReplaceable()) {
            return north;
        } else {
            return west1 < north1 && west1 < south1 && west1 < east1 && west1 <= 6.0D && mc.world.getBlockState(west).getMaterial().isReplaceable() ? west : null;
        }
    }

    public static double distanceBetween(BlockPos pos1, BlockPos pos2) {
        double d = (pos1.getX() - pos2.getX());
        double e = (pos1.getY() - pos2.getY());
        double f = (pos1.getZ() - pos2.getZ());
        return MathHelper.sqrt((float) (d * d + e * e + f * f));
    }

    public static int distance(BlockPos first, BlockPos second) {
        return Math.abs(first.getX() - second.getX()) + Math.abs(first.getY() - second.getY()) + Math.abs(first.getZ() - second.getZ());
    }

    public static void Mine(BlockPos targetPos, int slot) {
        InvUtils.swap(slot, false);
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, targetPos, Direction.UP));
        InvUtils.swap(slot, false);
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, targetPos, Direction.UP));
    }

    public static BlockPos getSelf(PlayerEntity p, Boolean escapePrevention) {
        BlockPos tpos = p.getBlockPos();
        List<BlockPos> selfTrapBlocks = new ArrayList();
        if (!escapePrevention && isTrapBlock(tpos.up(2))) {
            return tpos.up(2);
        } else {

            for (Vec3d stp : selfTrap) {
                BlockPos stb = tpos.add(stp.x, stp.y, stp.z);
                if (isTrapBlock(stb)) {
                    selfTrapBlocks.add(stb);
                }
            }

            if (selfTrapBlocks.isEmpty()) {
                return null;
            } else {
                return selfTrapBlocks.get((new Random()).nextInt(selfTrapBlocks.size()));
            }
        }
    }

    public static BlockPos getHead(PlayerEntity p, Boolean escapePrevention) {
        BlockPos tpos = p.getBlockPos();
        List<BlockPos> selfTrapBlocks = new ArrayList();
        if (!escapePrevention && isTrapBlock(tpos.up(2))) {
            return tpos.up(2);
        } else {

            for (Vec3d stp : head) {
                BlockPos stb = tpos.add(stp.x, stp.y, stp.z);
                if (isTrapBlock(stb)) {
                    selfTrapBlocks.add(stb);
                }
            }

            if (selfTrapBlocks.isEmpty()) {
                return null;
            } else {
                return selfTrapBlocks.get((new Random()).nextInt(selfTrapBlocks.size()));
            }
        }
    }

    public static BlockPos getSurr(PlayerEntity p, Boolean escapePrevention) {
        BlockPos tpos = p.getBlockPos();
        List<BlockPos> selfTrapBlocks = new ArrayList();
        if (!escapePrevention && isTrapBlock(tpos.up(2))) {
            return tpos.up(2);
        } else {

            for (Vec3d stp : surround) {
                BlockPos stb = tpos.add(stp.x, stp.y, stp.z);
                if (isTrapBlock(stb)) {
                    selfTrapBlocks.add(stb);
                }
            }

            if (selfTrapBlocks.isEmpty()) {
                return null;
            } else {
                return selfTrapBlocks.get((new Random()).nextInt(selfTrapBlocks.size()));
            }
        }
    }

    public static double distanceTo(BlockPos pos) {
        return distanceTo(pos.getX(), pos.getY(), pos.getZ());
    }

    public static double distanceTo(double x, double y, double z) {
        if (x >= 0.0D) {
            x += 0.5D;
        } else {
            x -= 0.5D;
        }

        if (y >= 0.0D) {
            y += 0.5D;
        } else {
            y -= 0.5D;
        }

        if (z >= 0.0D) {
            z += 0.5D;
        } else {
            z -= 0.5D;
        }

        double px = mc.player.getX();
        double py = mc.player.getY() + 1.25D;
        double pz = mc.player.getZ();
        if (px < 0.0D) {
            --px;
        }

        if (pz < 0.0D) {
            --pz;
        }

        double f = px - x;
        double g = py - y;
        double h = pz - z;
        return Math.sqrt(f * f + g * g + h * h);
    }

    public static SetBlockResult setBlock() {
        return RESULT;
    }

    public static int invIndexToSlotId(int invIndex) {
        return invIndex < 9 && invIndex != -1 ? 44 - (8 - invIndex) : invIndex;
    }

    public static void swap(int s) {
        if (s != mc.player.getInventory().selectedSlot && s >= 0 && s < 9) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(s));
            mc.player.getInventory().selectedSlot = s;
        }
    }

    public static void clickSlot(int slot, int button, SlotActionType action) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, button, action, mc.player);
    }

    public static class SetBlockResult {
        private static int slot = -1;
        private static BlockPos pos = null;
        private static Direction direct;
        private static boolean rotate;
        private static boolean noback;
        private static boolean packet;
        private static Hand hand;

        public SetBlockResult POS(BlockPos s) {
            pos = s;
            return this;
        }

        public SetBlockResult DIRECTION(Direction s) {
            direct = s;
            return this;
        }

        public SetBlockResult ROTATE(boolean s) {
            rotate = s;
            return this;
        }

        public SetBlockResult XYZ(int x, int y, int z) {
            pos = new BlockPos(x, y, z);
            return this;
        }

        public SetBlockResult RELATIVE_XYZ(int x, int y, int z) {
            pos = new BlockPos(BlockUtilsWorld.mc.player.getBlockPos().getX() + x, BlockUtilsWorld.mc.player.getBlockPos().getY() + y, BlockUtilsWorld.mc.player.getBlockPos().getZ() + z);
            return this;
        }

        public SetBlockResult NOBACK() {
            noback = true;
            return this;
        }

        public SetBlockResult PACKET(boolean s) {
            packet = s;
            return this;
        }

        public SetBlockResult SLOT(int slot) {
            SetBlockResult.slot = slot;
            return this;
        }

        public SetBlockResult INDEX_SLOT(int s) {
            slot = BlockUtilsWorld.invIndexToSlotId(s);
            return this;
        }

        public SetBlockResult HAND(Hand hand) {
            SetBlockResult.hand = hand;
            return this;
        }

        private void reset() {
            slot = -1;
            pos = null;
            direct = Direction.DOWN;
            rotate = false;
            noback = false;
            packet = false;
        }

        public boolean S() {
            if (pos != null && slot != -1 && !BlockUtilsWorld.mc.player.getInventory().getStack(slot).isEmpty() && BlockUtilsWorld.mc.player.getInventory().getStack(slot).getItem() instanceof BlockItem) {
                if (!BlockUtils.canPlace(pos, true)) {
                    reset();
                    return false;
                } else {
                    Block block = ((BlockItem) BlockUtilsWorld.mc.player.getInventory().getStack(slot).getItem()).getBlock();
                    if (!block.canPlaceAt(block.getDefaultState(), BlockUtilsWorld.mc.world, pos)) {
                        reset();
                        return false;
                    } else {
                        int PreSlot = BlockUtilsWorld.mc.player.getInventory().selectedSlot;
                        BlockUtilsWorld.swap(slot);
                        if (rotate) {
                            Vec3d hitPos = new Vec3d(0.0D, 0.0D, 0.0D);
                            ((IVec3d) hitPos).set(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                            Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos));
                        }

                        BlockHitResult hitresult = new BlockHitResult(BlockUtilsWorld.mc.player.getPos(), direct, pos, true);
                        if (packet) {
                            BlockUtilsWorld.mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(hand, hitresult));
                        } else {
                            BlockUtilsWorld.mc.interactionManager.interactBlock(BlockUtilsWorld.mc.player, BlockUtilsWorld.mc.world, hand, hitresult);
                        }

                        if (!noback) {
                            BlockUtilsWorld.swap(PreSlot);
                        }

                        reset();
                        return true;
                    }
                }
            } else {
                reset();
                return false;
            }
        }

        static {
            direct = Direction.DOWN;
            rotate = false;
            noback = false;
            packet = false;
            hand = Hand.MAIN_HAND;
        }
    }
}
