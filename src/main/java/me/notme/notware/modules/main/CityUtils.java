package me.notme.notware.modules.main;

import java.util.ArrayList;
import java.util.Iterator;
import meteordevelopment.meteorclient.mixin.AbstractBlockAccessor;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;

public class CityUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final BlockPos[] surround = new BlockPos[]{new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0)};

    public static PlayerEntity getPlayerTarget(double range) {
        if (mc.player.isDead()) {
            return null;
        } else {
            PlayerEntity closestTarget = null;

            for (AbstractClientPlayerEntity abstractClientPlayerEntity : mc.world.getPlayers()) {
                PlayerEntity target = abstractClientPlayerEntity;
                if (target != mc.player && !target.isDead() && Friends.get().shouldAttack(target) && !(mc.player.distanceTo(target) > range)) {
                    if (closestTarget == null) {
                        closestTarget = target;
                    } else if (mc.player.distanceTo(target) < mc.player.distanceTo((Entity) closestTarget)) {
                        closestTarget = target;
                    }
                }
            }

            if (closestTarget == null) {

                for (FakePlayerEntity target : FakePlayerManager.getPlayers()) {
                    if (!target.isDead() && Friends.get().shouldAttack(target) && !(mc.player.distanceTo(target) > range)) {
                        if (closestTarget == null) {
                            closestTarget = target;
                        } else if (mc.player.distanceTo(target) < mc.player.distanceTo((Entity) closestTarget)) {
                            closestTarget = target;
                        }
                    }
                }
            }

            return closestTarget;
        }
    }

    public static BlockPos getTargetBlock(PlayerEntity target) {
        BlockPos finalPos = null;
        ArrayList<BlockPos> positions = getTargetSurround(target);
        ArrayList<BlockPos> myPositions = getTargetSurround(mc.player);
        if (positions == null) {
            return null;
        } else {
            Iterator var4 = positions.iterator();

            while(true) {
                BlockPos pos;
                do {
                    if (!var4.hasNext()) {
                        return finalPos;
                    }

                    pos = (BlockPos)var4.next();
                } while(myPositions != null && !myPositions.isEmpty() && myPositions.contains(pos));

                if (finalPos == null) {
                    finalPos = pos;
                } else if (mc.player.squaredDistanceTo(Utils.vec3d(pos)) < mc.player.squaredDistanceTo(Utils.vec3d(finalPos))) {
                    finalPos = pos;
                }
            }
        }
    }

    private static ArrayList<BlockPos> getTargetSurround(PlayerEntity player) {
        ArrayList<BlockPos> positions = new ArrayList();
        boolean isAir = false;

        for(int i = 0; i < 4; i++) {
            if (player != null) {
                BlockPos obbySurround = getSurround(player, surround[i]);
                if (obbySurround != null) {
                    assert mc.world != null;

                    if (mc.world.getBlockState(obbySurround) != null) {
                        if (!((AbstractBlockAccessor)mc.world.getBlockState(obbySurround).getBlock()).isCollidable()) {
                            isAir = true;
                        }

                        if (mc.world.getBlockState(obbySurround).getBlock() == Blocks.OBSIDIAN) {
                            positions.add(obbySurround);
                        }
                    }
                }
            }
        }

        if (isAir) {
            return null;
        } else {
            return positions;
        }
    }

    public static BlockPos getSurround(Entity entity, BlockPos toAdd) {
        Vec3d v = entity.getPos();
        return toAdd == null ? new BlockPos(v.x, v.y, v.z) : (new BlockPos(v.x, v.y, v.z)).add(toAdd);
    }
}
