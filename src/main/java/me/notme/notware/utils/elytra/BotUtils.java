package me.notme.notware.utils.elytra;

import me.notme.notware.modules.elytra.bot.ElytraBaritone;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.BooleanSupplier;

public class BotUtils {
    private static boolean check;
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static boolean eating;
    private static long eatingMs;

    public static ArrayList<BlockPos> generatePath(BlockPos start, BlockPos goal, BlockPos[] positions, ArrayList<BlockPos> checkPositions, int loopAmount) {
        AStarNode.nodes.clear();
        BlockPos current = start;
        BlockPos closest = start;
        ArrayList<BlockPos> open = new ArrayList();
        ArrayList<BlockPos> closed = new ArrayList();
        int noClosest = 0;

        for (int i = 0; i < loopAmount; i++) {
            if (current.equals(goal)) {
                check = false;
                return getPath(current);
            }

            double lowestFCost = 2.147483647E9D;

            for (BlockPos pos : open) {
                double fCost = fCost(pos, goal, start);
                if (fCost < lowestFCost) {
                    lowestFCost = fCost;
                    current = pos;
                }
            }

            closed.add(current);
            open.remove(current);
            ArrayList<BlockPos> addToOpen = addToOpen(positions, checkPositions, current, goal, start, open, closed);
            if (addToOpen == null) {
                break;
            }

            open.addAll(addToOpen);
            if (lowestFCost < fCost(closest, goal, start)) {
                closest = current;
                noClosest = 0;
            } else {
                noClosest++;
                if (noClosest > 200) {
                    break;
                }
            }
        }

        if (!check) {
            check = true;
            return generatePath(start, closest, positions, checkPositions, loopAmount);
        } else {
            check = false;
            return new ArrayList();
        }
    }

    public static ArrayList<BlockPos> addToOpen(BlockPos[] positions, ArrayList<BlockPos> checkPositions, BlockPos current, BlockPos goal, BlockPos start, ArrayList<BlockPos> open, ArrayList<BlockPos> closed) {
        ArrayList<BlockPos> list = new ArrayList();
        ArrayList<BlockPos> positions2 = new ArrayList();
        BlockPos[] var9 = positions;
        int var10 = positions.length;

        for (int var11 = 0; var11 < var10; var11++) {
            BlockPos pos = var9[var11];
            positions2.add(current.add(pos.getX(), pos.getY(), pos.getZ()));
        }

        Iterator var14 = positions2.iterator();

        while (true) {
            AStarNode n;
            do {
                label72:
                while (true) {
                    BlockPos pos;
                    do {
                        do {
                            if (!var14.hasNext()) {
                                return list;
                            }

                            pos = (BlockPos) var14.next();
                        } while (isSolid(pos));
                    } while (closed.contains(pos));

                    ArrayList<BlockPos> checkPositions2 = new ArrayList();
                    Iterator var17 = checkPositions.iterator();

                    BlockPos check;
                    while (var17.hasNext()) {
                        check = (BlockPos) var17.next();
                        checkPositions2.add(pos.add(check.getX(), check.getY(), check.getZ()));
                    }

                    var17 = checkPositions2.iterator();

                    while (var17.hasNext()) {
                        check = (BlockPos) var17.next();
                        if (ElytraBaritone.CountMode == 1 && !isInRenderDistance(check)) {
                            return null;
                        }

                        if (isSolid(check) || !isInRenderDistance(check) || BlockUtilsWorld.getBlock(check) == Blocks.LAVA && ElytraBaritone.aLava || ElytraBaritone.YCor != -1.0D && check.getY() > ElytraBaritone.YCor) {
                            continue label72;
                        }
                    }

                    n = AStarNode.getNodeFromBlockpos(pos);
                    if (n == null) {
                        n = new AStarNode(pos);
                    }

                    if (!open.contains(pos)) {
                        list.add(pos);
                    }
                    break;
                }
            } while (n.parent != null && !(gCost(current, start) < gCost(n.parent, start)));

            n.parent = current;
        }
    }

    public static double fCost(BlockPos pos, BlockPos goal, BlockPos start) {
        double dx = (goal.getX() - pos.getX());
        double dz = (goal.getZ() - pos.getZ());
        double h = Math.sqrt(dx * dx + dz * dz);
        double fCost = gCost(pos, start) + h;
        return fCost;
    }

    public static double gCost(BlockPos pos, BlockPos start) {
        double dx = (start.getX() - pos.getX());
        double dy = (start.getY() - pos.getY());
        double dz = (start.getZ() - pos.getZ());
        return Math.sqrt(Math.abs(dx) + Math.abs(dy) + Math.abs(dz));
    }

    private static ArrayList<BlockPos> getPath(BlockPos current) {
        ArrayList path = new ArrayList();

        try {
            AStarNode n = AStarNode.getNodeFromBlockpos(current);
            if (n == null) {
                n = (AStarNode) AStarNode.nodes.get(AStarNode.nodes.size() - 1);
            }

            path.add(n.pos);

            while (n != null && n.parent != null) {
                path.add(n.parent);
                n = AStarNode.getNodeFromBlockpos(n.parent);
            }
        } catch (IndexOutOfBoundsException var3) {
        }

        return path;
    }

    public static boolean isInRenderDistance(BlockPos pos) {
        return mc.world.getChunk(pos) instanceof WorldChunk;
    }

    public static boolean isSolid(BlockPos pos) {
        try {
            return mc.world.getBlockState(pos).getMaterial().isSolid();
        } catch (NullPointerException var2) {
            return false;
        }
    }

    public static ItemStack getItemStack(int id) {
        try {
            return mc.player.getInventory().getStack(id);
        } catch (NullPointerException var2) {
            return null;
        }
    }

    public static boolean hasItem(Item item) {
        return getAmountOfItem(item) != 0;
    }

    public static int getAmountOfItem(Item item) {
        int count = 0;
        Iterator var2 = getAllItems().iterator();

        while (var2.hasNext()) {
            ItemStackUtil itemStack = (ItemStackUtil) var2.next();
            if (itemStack.itemStack != null && itemStack.itemStack.getItem().equals(item)) {
                count += itemStack.itemStack.getCount();
            }
        }

        return count;
    }

    public static ArrayList<ItemStackUtil> getAllItems() {
        ArrayList<ItemStackUtil> items = new ArrayList();

        for (int i = 0; i < 36; i++) {
            items.add(new ItemStackUtil(getItemStack(i), i));
        }

        return items;
    }

    public static int getSlot(Block block) {
        try {
            Iterator var1 = getAllItems().iterator();

            while (var1.hasNext()) {
                ItemStackUtil itemStack = (ItemStackUtil) var1.next();
                if (Block.getBlockFromItem(itemStack.itemStack.getItem()).equals(block)) {
                    return itemStack.slotId;
                }
            }
        } catch (Exception var3) {
        }

        return -1;
    }

    public static int getSlot(Item item) {
        try {
            Iterator var1 = getAllItems().iterator();

            while (var1.hasNext()) {
                ItemStackUtil itemStack = (ItemStackUtil) var1.next();
                if (itemStack.itemStack.getItem().equals(item)) {
                    return itemStack.slotId;
                }
            }
        } catch (Exception var3) {
        }

        return -1;
    }

    public static int getClickSlot(int id) {
        if (id == -1) {
            return id;
        } else if (id < 9) {
            id += 36;
            return id;
        } else {
            if (id == 39) {
                id = 5;
            } else if (id == 38) {
                id = 6;
            } else if (id == 37) {
                id = 7;
            } else if (id == 36) {
                id = 8;
            } else if (id == 40) {
                id = 45;
            }

            return id;
        }
    }

    public static void clickSlot(int id, int otherRows) {
        if (id != -1) {
            try {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, getClickSlot(id) + otherRows, 0, SlotActionType.SWAP, mc.player);
            } catch (Exception var3) {
            }
        }

    }

    public static void clickSlot(int id) {
        if (id != -1) {
            try {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, getClickSlot(id), 0, SlotActionType.SWAP, mc.player);
            } catch (Exception var2) {
            }
        }

    }

    public static void centerMotionFull() {
        if (!isCentered()) {
            double[] centerPos = new double[]{Math.floor(mc.player.getX()) + 0.5D, Math.floor(mc.player.getY()), Math.floor(mc.player.getZ()) + 0.5D};
            double xSpeed = Math.abs(mc.player.getX() - mc.player.prevX);
            double zSpeed = Math.abs(mc.player.getZ() - mc.player.prevZ);
            xSpeed = (centerPos[0] - mc.player.getX()) / 2.0D;
            zSpeed = (centerPos[2] - mc.player.getZ()) / 2.0D;
            sleepUntil(() -> {
                return Math.abs(centerPos[0] - mc.player.getX()) <= 0.1D && Math.abs(centerPos[2] - mc.player.getZ()) <= 0.1D;
            }, 1000);
            xSpeed = 0.0D;
            zSpeed = 0.0D;
        }
    }

    public static void sleepUntil(BooleanSupplier condition, int timeout) {
        long startTime = System.currentTimeMillis();

        while (!condition.getAsBoolean() && (timeout == -1 || System.currentTimeMillis() - startTime < timeout)) {
            sleep(10);
        }

    }

    public static void sleepUntil(BooleanSupplier condition, int timeout, int amountToSleep) {
        long startTime = System.currentTimeMillis();

        while (!condition.getAsBoolean() && (timeout == -1 || System.currentTimeMillis() - startTime < timeout)) {
            sleep(amountToSleep);
        }

    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception var2) {
        }

    }

    public static boolean isCentered() {
        double[] centerPos = new double[]{Math.floor(mc.player.getX()) + 0.5D, Math.floor(mc.player.getY()), Math.floor(mc.player.getZ()) + 0.5D};
        return Math.abs(centerPos[0] - mc.player.getX()) <= 0.1D && Math.abs(centerPos[2] - mc.player.getZ()) <= 0.1D;
    }

    public static int getDurability(ItemStack itemStack) {
        return itemStack.getMaxDamage() - itemStack.getDamage();
    }

    public static void switchItem(int slot, boolean sleep) {
        if (slot < 9) {
            mc.player.getInventory().selectedSlot = slot;
        } else {
            int freeSlot = 8;

            for (int i = 0; i < 9; i++) {
                if (getItemStack(i).getItem() == Items.AIR) {
                    freeSlot = i;
                    break;
                }
            }

            clickSlot(slot);
            if (sleep) {
                sleep(200);
            }

            clickSlot(freeSlot);
            if (sleep) {
                sleep(200);
            }

            clickSlot(slot);
            if (sleep) {
                sleep(200);
            }

            mc.player.getInventory().selectedSlot = freeSlot;
            if (sleep) {
                sleep(100);
            }
        }

    }

    public static void eatItem(Item item) {
        eating = true;
        int PreSlot = mc.player.getInventory().selectedSlot;
        int slot = InvUtils.findInHotbar(new Item[]{item}).getSlot();
        InvUtils.swap(slot, false);
        if (!mc.player.isUsingItem()) {
            Utils.rightClick();
        }

    }

    public static boolean isEating() {
        return eating;
    }

    public static class AStarNode {
        public static ArrayList<AStarNode> nodes = new ArrayList();
        public BlockPos pos;
        public BlockPos parent;

        public AStarNode(BlockPos pos) {
            pos = pos;
            nodes.add(this);
        }

        public static AStarNode getNodeFromBlockpos(BlockPos pos) {
            Iterator var1 = nodes.iterator();

            AStarNode node;
            do {
                if (!var1.hasNext()) {
                    return null;
                }

                node = (AStarNode) var1.next();
            } while (!node.pos.equals(pos));

            return node;
        }
    }

    public static class ItemStackUtil {
        public ItemStack itemStack;
        public int slotId;

        public ItemStackUtil(ItemStack itemStack, int slotId) {
            itemStack = itemStack;
            slotId = slotId;
        }
    }
}
