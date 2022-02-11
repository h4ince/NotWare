package me.notme.notware.modules.main;

import me.notme.notware.NotWare;
import meteordevelopment.meteorclient.events.entity.player.FinishUsingItemEvent;
import meteordevelopment.meteorclient.events.entity.player.StoppedUsingItemEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class PacketUse extends Module {

    public PacketUse() {
        super(NotWare.nwcombat, "one-click-use", "allows you to eat a consumable with one click");
    }

    private boolean isUsing = false;
    boolean presseD = false;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!mc.options.keyUse.isPressed()) {
            presseD = false;
        }

        if (mc.options.keyUse.isPressed() && !isUsing && !presseD && (mc.player.getMainHandStack().getItem().isFood() && mc.player.getHungerManager().isNotFull() || mc.player.getOffHandStack().getItem().isFood() && mc.player.getHungerManager().isNotFull() || mc.player.getMainHandStack().getItem() instanceof PotionItem || mc.player.getOffHandStack().getItem() instanceof PotionItem || mc.player.getMainHandStack().getItem() instanceof EnchantedGoldenAppleItem || mc.player.getOffHandStack().getItem() instanceof EnchantedGoldenAppleItem || mc.player.getMainHandStack().getItem() instanceof ChorusFruitItem || mc.player.getOffHandStack().getItem() instanceof ChorusFruitItem || mc.player.getMainHandStack().getItem() == Items.GOLDEN_APPLE || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE)) {
            if (mc.targetedEntity != null) {
                if (!(mc.targetedEntity instanceof PlayerEntity)) return;
            } else if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK && BlockUtils.isClickable(mc.world.getBlockState(((BlockHitResult) mc.crosshairTarget).getBlockPos()).getBlock())) {
                if (BlockUtils.isClickable(mc.world.getBlockState(((BlockHitResult) mc.crosshairTarget).getBlockPos()).getBlock()))
                    return;
            }

            mc.options.keyUse.setPressed(true);
            isUsing = true;
            presseD = true;
        }

        if (mc.options.keyUse.isPressed()) {
            presseD = true;
        }

        if (isUsing) {
            mc.options.keyUse.setPressed(true);
        }
    }

    @Override
    public void onDeactivate() {
        stopIfUsing();
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            stopIfUsing();
        }
    }

    @EventHandler
    private void onFinishUsingItem(FinishUsingItemEvent event) {
        stopIfUsing();
    }

    @EventHandler
    private void onStoppedUsingItem(StoppedUsingItemEvent event) {
        stopIfUsing();
    }

    private void stopIfUsing() {
        if (isUsing) {
            mc.options.keyUse.setPressed(false);
            isUsing = false;
        }
    }
}
