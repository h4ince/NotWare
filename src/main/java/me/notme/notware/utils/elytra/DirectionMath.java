package me.notme.notware.utils.elytra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Direction;

public enum DirectionMath {
    XP("X-Plus"),
    XM("X-Minus"),
    ZP("Z-Plus"),
    ZM("Z-Minus"),
    XP_ZP("X-Plus, Z-Plus"),
    XM_ZP("X-Minus, Z-Plus"),
    XM_ZM("X-Minus, Z-Minus"),
    XP_ZM("X-Plus, Z-Minus");

    public String name;
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private DirectionMath(String name) {
        this.name = name;
    }

    public static DirectionMath getDirection() {
        Direction facing = mc.player.getHorizontalFacing();
        return facing == Direction.NORTH ? ZM : (facing == Direction.WEST ? XM : (facing == Direction.SOUTH ? ZP : XP));
    }

    public static DirectionMath getDiagonalDirection() {
        Direction facing = mc.player.getHorizontalFacing();
        double closest;
        if (facing.equals(Direction.NORTH)) {
            closest = getClosest(135.0D, -135.0D);
            return closest == -135.0D ? XP_ZM : XM_ZM;
        } else if (facing.equals(Direction.WEST)) {
            closest = getClosest(135.0D, 45.0D);
            return closest == 135.0D ? XM_ZM : XM_ZP;
        } else if (facing.equals(Direction.EAST)) {
            closest = getClosest(-45.0D, -135.0D);
            return closest == -135.0D ? XP_ZM : XP_ZP;
        } else {
            closest = getClosest(45.0D, -45.0D);
            return closest == 45.0D ? XM_ZP : XP_ZP;
        }
    }

    private static double getClosest(double a, double b) {
        double yaw = mc.player.getYaw();
        yaw = yaw < -180.0D ? (yaw += 360.0D) : (yaw > 180.0D ? (yaw -= 360.0D) : yaw);
        return Math.abs(yaw - a) < Math.abs(yaw - b) ? a : b;
    }


    private static DirectionMath[] $values() {
        return new DirectionMath[]{XP, XM, ZP, ZM, XP_ZP, XM_ZP, XM_ZM, XP_ZM};
    }
}
