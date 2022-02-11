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


public class AutoTNT extends Module {
    public AutoTNT(){
        super(NotWare.nwcombat,"AutoTNT", "for nns that cant use crystal");
    }

    public enum tntPlaceMode{
        Head,
        Legs
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The radius in which players get targeted.")
        .defaultValue(5)
        .min(0)
        .sliderMax(10)
        .build()
    );

    private final Setting<tntPlaceMode> tntPlaceModeSetting = sgGeneral.add(new EnumSetting.Builder<tntPlaceMode>()
        .name("mode")
        .description("How to select the player to target.")
        .defaultValue(tntPlaceMode.Head)
        .build()
    );

    private final Setting<Integer> tntDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("How many ticks between block placements.")
        .defaultValue(1)
        .build()
    );

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How to select the player to target.")
        .defaultValue(SortPriority.LowestHealth)
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
    public void onActivate() {
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (TargetUtils.isBadTarget(target, targetRange.get())) target = TargetUtils.getPlayerTarget(targetRange.get(), priority.get());
        if (TargetUtils.isBadTarget(target, targetRange.get())) return;

        targetblock = target.getBlockPos();

        if (tntPlaceModeSetting.get() == tntPlaceMode.Head) {
            place(targetblock.add(0, 2, 0));
            if (mc.world.getBlockState(targetblock.add(0, 2, 0)).getBlock() == Blocks.TNT) {

                flint_steel(targetblock.add(0, 2, 0));
            }

        } else if (tntPlaceModeSetting.get() == tntPlaceMode.Legs) {
            if (mc.world.getBlockState(targetblock.add(1, 0, 0)).isAir()) {
                place(targetblock.add(1, 0, 0));
                if (mc.world.getBlockState(targetblock.add(1, 0, 0)).getBlock() == Blocks.TNT) {
                    flint_steel(targetblock.add(1, 0, 0));
                }
            } else {
                if (mc.world.getBlockState(targetblock.add(-1, 0, 0)).isAir()) {
                    place(targetblock.add(-1, 0, 0));
                    if (mc.world.getBlockState(targetblock.add(-1, 0, 0)).getBlock() == Blocks.TNT) {
                        flint_steel(targetblock.add(-1, 0, 0));
                    }
                } else {
                    if (mc.world.getBlockState(targetblock.add(0, 0, 1)).isAir()) {
                        place(targetblock.add(0, 0, 1));
                        if (mc.world.getBlockState(targetblock.add(0, 0, 1)).getBlock() == Blocks.TNT) {
                            flint_steel(targetblock.add(0, 0, 1));
                        }
                    } else {
                        if (mc.world.getBlockState(targetblock.add(0, 0, -1)).isAir()) {
                            place(targetblock.add(0, 0, -1));
                            if (mc.world.getBlockState(targetblock.add(0, 0, -1)).getBlock() == Blocks.TNT) {
                                flint_steel(targetblock.add(0, 0, -1));
                            }
                        } else {
                            if (mc.world.getBlockState(targetblock.add(0, 2, 0)).isAir()) {
                                place(targetblock.add(0, 2, 0));
                                if (mc.world.getBlockState(targetblock.add(0, 2, 0)).getBlock() == Blocks.TNT) {
                                    flint_steel(targetblock.add(0, 2, 0));
                                }
                            }
                        }
                    }
                }
            }
        }

        if (mc.world.getBlockState(targetblock.add(1, 0, 0)).getBlock() == Blocks.TNT) {
            flint_steel(targetblock.add(1, 0, 0));
        }
        if (mc.world.getBlockState(targetblock.add(-1, 0, 0)).getBlock() == Blocks.TNT) {
            flint_steel(targetblock.add(-1, 0, 0));
        }
        if (mc.world.getBlockState(targetblock.add(0, 0, 1)).getBlock() == Blocks.TNT) {
            flint_steel(targetblock.add(0, 0, 1));
        }
        if (mc.world.getBlockState(targetblock.add(0, 0, -1)).getBlock() == Blocks.TNT) {
            flint_steel(targetblock.add(0, 0, -1));
        }
        if (mc.world.getBlockState(targetblock.add(0, 2, 0)).getBlock() == Blocks.TNT) {
            flint_steel(targetblock.add(0, 2, 0));
        }

        if (tntPlaceModeSetting.get() == tntPlaceMode.Head) {
            if (mc.world.getBlockState(targetblock.add(0, 2, 0)).isAir()) {
                place(targetblock.add(0, 2, 0));
                if (mc.world.getBlockState(targetblock.add(0, 2, 0)).getBlock() == Blocks.TNT) {
                    flint_steel(targetblock.add(0, 2, 0));
                }
            }
        }
        if (mc.world.getBlockState(targetblock.add(0, 2, 0)).getBlock() == Blocks.TNT) {
            flint_steel(targetblock.add(0, 2, 0));
        }
    }

    private boolean place(BlockPos targetblock){
        BlockState blockState = mc.world.getBlockState(targetblock);

        if (!blockState.getMaterial().isReplaceable()) return true;

        if (BlockUtils.place(targetblock, InvUtils.findInHotbar(Items.TNT), rotate.get(), 100, true)) {

            return_ = true;
        }
        return false;
    }

    private void flint_steel(BlockPos targetblock) {
        if (timer >= tntDelay.get()) {
            timer = 0;
            int preSlot = mc.player.getInventory().selectedSlot;

            FindItemResult flint = InvUtils.findInHotbar(Items.FLINT_AND_STEEL);

            InvUtils.swap(flint.getSlot(), false);

            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
                new BlockHitResult(mc.player.getPos(), Direction.UP, targetblock, true));

            InvUtils.swap(preSlot, false);
        } else {
            timer++;
        }
    }
}
