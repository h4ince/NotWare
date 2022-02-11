package me.notme.notware.modules.elytra.bot;

import me.notme.notware.utils.elytra.PlayerTravelUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class ElytraFlyBot {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static boolean toggled;

    @EventHandler
    public void onTravel(PlayerTravelUtils event) {
        event.cancel();
    }

    public static void toggle(boolean on) {
        toggled = on;
    }

    public static void setMotion(BlockPos pos, BlockPos next, BlockPos previous) {
        double x = 0.0D;
        double y = 0.0D;
        double z = 0.0D;
        double xDiff = pos.getX() + 0.5D - mc.player.getX();
        double yDiff = pos.getY() + 0.4D - mc.player.getY();
        double zDiff = pos.getZ() + 0.5D - mc.player.getZ();
        double speed = ElytraBaritone.EFspeed;
        int amount = 0;

        try {
            if (Math.abs(next.getX() - previous.getX()) > 0) {
                amount++;
            }

            if (Math.abs(next.getY() - previous.getY()) > 0) {
                amount++;
            }

            if (Math.abs(next.getZ() - previous.getZ()) > 0) {
                amount++;
            }

            if (amount > 1) {
                speed = ElytraBaritone.EFMspeed;
                if (next.getX() - previous.getX() == next.getZ() - previous.getZ() && next.getY() - previous.getY() == 0 && (xDiff >= 1.0D && zDiff >= 1.0D || xDiff <= -1.0D && zDiff <= -1.0D)) {
                    speed = ElytraBaritone.EFspeed;
                }
            }
        } catch (Exception var22) {
            speed = ElytraBaritone.EFMspeed;
        }

        if ((int) xDiff <= 0) {
            if ((int) xDiff < 0) {
                x = -speed;
            }
        }

        if ((int) yDiff > 0) {
            y = ElytraBaritone.EFMspeed;
        } else if ((int) yDiff < 0) {
            y = -ElytraBaritone.EFMspeed;
        }

        if ((int) zDiff <= 0) {
            if ((int) zDiff < 0) {
                z = -speed;
            }
        }

        x = Math.abs(mc.player.getX() - mc.player.prevX);
        y = Math.abs(mc.player.getY() - mc.player.prevY);
        z = Math.abs(mc.player.getZ() - mc.player.prevZ);
        double centerSpeed = 0.2D;
        double centerCheck = 0.1D;
        if (x == 0.0D) {
            if (!(xDiff > centerCheck)) {
                if (xDiff < -centerCheck) {
                    x = -centerSpeed;
                } else {
                    x = 0.0D;
                }
            }
        }

        if (y == 0.0D) {
            if (!(yDiff > centerCheck)) {
                if (yDiff < -centerCheck) {
                    y = -centerSpeed;
                } else {
                    y = 0.0D;
                }
            }
        }

        if (z == 0.0D) {
            if (!(zDiff > centerCheck)) {
                if (zDiff < -centerCheck) {
                    z = -centerSpeed;
                } else {
                    z = 0.0D;
                }
            }
        }
    }
}
