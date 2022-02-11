package me.notme.notware.modules.main;

import me.notme.notware.NotWare;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.LogManager;

public class AutoCrystalHead extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder().name("delay").description("The amount of delay in ticks before placing.").defaultValue(4).min(0).sliderMax(20).build());
    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder().name("range").description("The break range.").defaultValue(5.0D).min(0.0D).build());
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder().name("rotate").description("Automatically faces the blocks being mined.").defaultValue(false).build());

    BlockPos pos = null;
    int pause = 0;
    boolean isDone = false;
    boolean firtDone = false;

    public AutoCrystalHead() {
        super(NotWare.nwcombat, "auto-crystal-head", "cevbreaker lite version");
    }

    @EventHandler
    private void BlockUpdate(PacketEvent.Receive e) {
        if (e.packet instanceof BlockUpdateS2CPacket p) {
            if (BRUHLOGIC.equalsBlockPos(p.getPos(), pos) && p.getState().isAir()) {
                isDone = true;
            }
        }
    }

    private void s(String s) {
        LogManager.getLogger().info(s);
    }

    @EventHandler
    private void AntiClick(PacketEvent.Send e) {
        if (e.packet instanceof PlayerActionC2SPacket) {
            PlayerActionC2SPacket p = (PlayerActionC2SPacket) e.packet;
            PlayerActionC2SPacket.Action var10001 = p.getAction();
            s(var10001 + " " + p.getPos());
            if ((p.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK || p.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK || p.getAction() == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) && !BRUHLOGIC.equalsBlockPos(p.getPos(), pos)) {
                s("cancel!");
                e.cancel();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre e) {
        if (mc.world != null && mc.player != null) {
            if (pause > 0) {
                pause--;
            } else {
                pause = delay.get();
                PlayerEntity player = TargetUtils.getPlayerTarget(7.0D, SortPriority.LowestDistance);
                if (player != null) {
                    BlockPos obsidianPos = new BlockPos(player.getBlockPos().getX(), player.getBlockPos().getY() + 2, player.getBlockPos().getZ());
                    if (!(BRUHLOGIC.DistanceTo(obsidianPos) > range.get())) {
                        BlockPos head = new BlockPos(player.getBlockPos().getX(), player.getBlockPos().getY() + 1, player.getBlockPos().getZ());
                        if (mc.world.getBlockState(head).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(head).getBlock() != Blocks.BEDROCK) {
                            BlockPos crystalPos = new BlockPos(player.getBlockPos().getX(), player.getBlockPos().getY() + 3, player.getBlockPos().getZ());
                            if (mc.world.getBlockState(obsidianPos).isAir() || mc.world.getBlockState(obsidianPos).getBlock() == Blocks.OBSIDIAN) {
                                int pickaxe = InvUtils.findInHotbar(Items.IRON_PICKAXE).getSlot();
                                if (pickaxe == -1) {
                                    pickaxe = InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).getSlot();
                                }

                                if (pickaxe == -1) {
                                    pickaxe = InvUtils.findInHotbar(Items.DIAMOND_PICKAXE).getSlot();
                                }

                                if (pickaxe == -1) {
                                    error("monke u got no pickaxe on ur hotbar");
                                    toggle();
                                } else {
                                    if (mc.world.getBlockState(obsidianPos).getBlock() != Blocks.OBSIDIAN) {
                                        BRUHLOGIC.BlockPlace(obsidianPos, InvUtils.findInHotbar(Items.OBSIDIAN).getSlot(), rotate.get());
                                    }

                                    if (!BRUHLOGIC.equalsBlockPos(pos, obsidianPos)) {
                                        pos = obsidianPos;
                                        BRUHLOGIC.swap(pickaxe);
                                        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
                                        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
                                        isDone = false;
                                    } else if (isDone) {
                                        EndCrystalEntity crystal = null;

                                        for (Entity findcrystal : mc.world.getEntities()) {
                                            if (findcrystal instanceof EndCrystalEntity && BRUHLOGIC.equalsBlockPos(findcrystal.getBlockPos(), crystalPos)) {
                                                crystal = (EndCrystalEntity) findcrystal;
                                                break;
                                            }
                                        }

                                        if (crystal != null) {
                                            if (rotate.get()) {
                                                float[] rotation = PlayerUtils.calculateAngle(crystal.getPos());
                                                Rotations.rotate(rotation[0], rotation[1]);
                                            }

                                            int preSlot = mc.player.getInventory().selectedSlot;
                                            BRUHLOGIC.swap(pickaxe);
                                            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
                                            BRUHLOGIC.swap(preSlot);
                                            BRUHLOGIC.attackEntity(crystal);
                                        } else {
                                            placeCrystal(player, obsidianPos);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean placeCrystal(PlayerEntity player, BlockPos obsidianPos) {
        BlockPos crystalPos = new BlockPos(player.getBlockPos().getX(), player.getBlockPos().getY() + 3, player.getBlockPos().getZ());
        if (!BlockUtils.canPlace(crystalPos, true)) {
            return false;
        } else if (!mc.world.getBlockState(crystalPos).isAir()) {
            return false;
        } else {
            int crystalSlot = InvUtils.findInHotbar(Items.END_CRYSTAL).getSlot();
            if (crystalSlot == -1) {
                error("monke u got no crystal on ur hotbar");
                toggle();
                return false;
            } else {
                BRUHLOGIC.interact(obsidianPos, crystalSlot, Direction.DOWN);
                return true;
            }
        }
    }
}
