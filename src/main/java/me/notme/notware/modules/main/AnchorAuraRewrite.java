package me.notme.notware.modules.main;

import me.notme.notware.NotWare;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AnchorAuraRewrite extends Module {
    public AnchorAuraRewrite(){
        super(NotWare.nwcombat,"anchor-aura-rewrite", "for retards that cant configing");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("The radius in which players get targeted.")
        .defaultValue(4)
        .min(0)
        .sliderMax(5)
        .build()
    );
    private final Setting<SortPriority> targetPriority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How to select the player to target.")
        .defaultValue(SortPriority.LowestHealth)
        .build()
    );
    private final Setting<Integer> placeDelay = sgGeneral.add(new IntSetting.Builder()
        .name("place-delay")
        .description("The tick delay between breaking anchors.")
        .defaultValue(10)
        .min(0)
        .max(10)
        .build()
    );
    private final Setting<Integer> breakDelay = sgGeneral.add(new IntSetting.Builder()
        .name("break-delay")
        .description("The tick delay between breaking anchors.")
        .defaultValue(10)
        .min(0)
        .max(10)
        .build()
    );
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates towards blocks when placing.")
        .defaultValue(true)
        .build()
    );
    private PlayerEntity target;
    private int timer;
    BlockPos targetblock;
    private boolean return_;
    @Override
    public void onActivate() {timer = 0;}
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (TargetUtils.isBadTarget(target, targetRange.get())) target = TargetUtils.getPlayerTarget(targetRange.get(), targetPriority.get());
        if (TargetUtils.isBadTarget(target, targetRange.get())) return;

        targetblock = target.getBlockPos();

        if (mc.world.getBlockState(targetblock.add(0, 2, 0)).isAir()){
            place(targetblock.add(0, 2, 0));
        }
        if (mc.world.getBlockState(targetblock.add(0, 2, 0)).getBlock() == Blocks.RESPAWN_ANCHOR){
            charge((targetblock.add(0, 2, 0)));
            Abreak(targetblock.add(0, 2, 0));
        }
    }

    private void charge(BlockPos targetblock) {
        if (timer >= breakDelay.get()) {
            timer = 0;
            int preSlot = mc.player.getInventory().selectedSlot;
            FindItemResult glowstone = InvUtils.findInHotbar(Items.GLOWSTONE);
            InvUtils.swap(glowstone.getSlot(), false);
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
                new BlockHitResult(mc.player.getPos(), Direction.UP, targetblock, true));
            InvUtils.swap(preSlot, false);
        } else {
            timer++;
        }
    }

    private void Abreak(BlockPos targetblock) {
        int hand = mc.player.getInventory().selectedSlot;
        InvUtils.swap(hand, false);
        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
            new BlockHitResult(mc.player.getPos(), Direction.UP, targetblock, true));
    }
    private void place(BlockPos targetblock) {
        if (timer >= placeDelay.get()) {
            timer = 0;
            BlockState blockState = mc.world.getBlockState(targetblock);
            if (!blockState.getMaterial().isReplaceable()) return;
            if (BlockUtils.place(targetblock, InvUtils.findInHotbar(Items.RESPAWN_ANCHOR), rotate.get(), 100, true)) {
                return_ = true;
            }
            return;
        } else {
            timer++;
        }
    }
}
