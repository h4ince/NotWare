package me.notme.notware.modules.elytra.bot;

import me.notme.notware.utils.elytra.PlayerMoveUpdateUtils;
import me.notme.notware.utils.elytra.TimerUtils;
import me.notme.notware.utils.elytra.PlayerPacketUtils;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PacketFlyUtils {
    private static TimerUtils antiKickTimer = new TimerUtils();
    private static double startY;
    public static boolean toggled;
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @EventHandler
    public void playerMotionUpdateEvent(PlayerMoveUpdateUtils event) {
        mc.player.setVelocity(0.0D, 0.0D, 0.0D);
        float speedY = 0.0F;
        if (mc.player.getY() < startY) {
            if (!antiKickTimer.hasPassed(3000)) {
                speedY = mc.player.age % 20 == 0 ? -0.1F : 0.031F;
            } else {
                antiKickTimer.reset();
                speedY = -0.1F;
            }
        } else if (mc.player.age % 4 == 0) {
            speedY = -0.1F;
        }

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX() + Math.abs(mc.player.getX() - mc.player.prevX), mc.player.getY() + Math.abs(mc.player.getY() - mc.player.prevY), mc.player.getZ() + Math.abs(mc.player.getZ() - mc.player.prevZ), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
        double y = mc.player.getY() + Math.abs(mc.player.getY() - mc.player.prevY);
        y += 1337.0D;
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX() + Math.abs(mc.player.getX() - mc.player.prevX), y, mc.player.getZ() + Math.abs(mc.player.getZ() - mc.player.prevZ), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
    }

    @EventHandler
    public void onPacket(PlayerPacketUtils event) {
        mc.player.setVelocity(0.0D, 0.0D, 0.0D);
        float speedY = 0.0F;
        if (mc.player.getY() < startY) {
            if (!antiKickTimer.hasPassed(3000)) {
                speedY = mc.player.age % 20 == 0 ? -0.1F : 0.031F;
            } else {
                antiKickTimer.reset();
                speedY = -0.1F;
            }
        } else if (mc.player.age % 4 == 0) {
            speedY = -0.1F;
        }

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX() + Math.abs(mc.player.getX() - mc.player.prevX), mc.player.getY() + Math.abs(mc.player.getY() - mc.player.prevY), mc.player.getZ() + Math.abs(mc.player.getZ() - mc.player.prevZ), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
        double y = mc.player.getY() + Math.abs(mc.player.getY() - mc.player.prevY);
        y += 1337.0D;
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX() + Math.abs(mc.player.getX() - mc.player.prevX), y, mc.player.getZ() + Math.abs(mc.player.getZ() - mc.player.prevZ), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
    }

    public static void toggle(boolean on) {
        if (on) {
            startY = mc.player.getY();
            toggled = true;
        } else {
            toggled = false;
        }
    }
}
