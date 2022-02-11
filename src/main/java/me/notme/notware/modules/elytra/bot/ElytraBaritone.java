package me.notme.notware.modules.elytra.bot;

import me.notme.notware.NotWare;
import me.notme.notware.utils.elytra.RenderPath;
import me.notme.notware.utils.elytra.BotUtils;
import me.notme.notware.utils.elytra.MiningUtil1;
import me.notme.notware.utils.elytra.DirectionMath;
import me.notme.notware.utils.elytra.TimerUtils;
import me.notme.notware.utils.elytra.BlockUtilsWorld;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ElytraBaritone extends Module {
    private final SettingGroup sgMode = settings.createGroup("Mode");
    private final SettingGroup sgFlight = settings.createGroup("Flight");
    private final SettingGroup sgCoordinates = settings.createGroup("Coordinates");
    private final SettingGroup sgAutomaticly = settings.createGroup("Automated");
    public final Setting<Mode> mode = sgMode.add(new EnumSetting.Builder<Mode>().name("mode").description("Pathfinding designed for overworld/highways.").defaultValue(Mode.Highway).build());
    private final Setting<TakeOff> takeoffMode = sgMode.add(new EnumSetting.Builder<TakeOff>().name("mode").description("Just jumps/use PacketFly/slowglide and tries to open the elytra.").defaultValue(TakeOff.PacketFly).build());
    private final Setting<FlyMode> flyMode = sgMode.add(new EnumSetting.Builder<FlyMode>().name("fly-mode").description("Uses fireworks/ElytraFly and rotates head to move.").defaultValue(FlyMode.ElytraFly).build());
    private final Setting<Double> elytraFlySpeed = sgFlight.add(new DoubleSetting.Builder().name("speed").description("Speed for elytrafly.").defaultValue(1.81D).min(0.0D).sliderMax(15.0D).max(15.0D).visible(() -> flyMode.get() == FlyMode.ElytraFly).build());
    private final Setting<Double> elytraFlyManuverSpeed = sgFlight.add(new DoubleSetting.Builder().name("manuver-speed").description("Speed of manuver with elytra.").defaultValue(1.0D).min(0.0D).sliderMax(15.0D).max(15.0D).visible(() -> flyMode.get() == FlyMode.ElytraFly).build());
    private final Setting<Integer> fireworkDelay = sgFlight.add(new IntSetting.Builder().name("firework-delay").description("Delay between the clicks on the fireworks in second.").defaultValue(10).min(0).sliderMax(30).max(30).visible(() -> flyMode.get() == FlyMode.ElytraFly).build());
    private final Setting<Double> minSpeed = sgFlight.add(new DoubleSetting.Builder().name("min-speed").description("Player's speed(bps) when fireworks will be used.").defaultValue(20.0D).min(0.0D).sliderMax(50.0D).max(50.0D).visible(() -> flyMode.get() == FlyMode.Firework).build());
    private final Setting<Boolean> pathfinding = sgFlight.add(new BoolSetting.Builder().name("pathfind").description("Automatically pathfinder.").defaultValue(true).build());
    private final Setting<Boolean> avoidLava = sgFlight.add(new BoolSetting.Builder().name("avoid-lava").description("Automatically avoids lava blocks.").defaultValue(true).visible(pathfinding::get).build());
    private final Setting<Boolean> coordinates = sgCoordinates.add(new BoolSetting.Builder().name("allow-coordinates").description("Allow goordinates for baritone.").defaultValue(true).build());
    private final Setting<Double> gotoX = sgCoordinates.add(new DoubleSetting.Builder().name("x-coordinates").description("X-coordinates.").defaultValue(0.0D).sliderMin(-3.0E7D).min(-3.0E7D).sliderMax(3.0E7D).max(3.0E7D).visible(coordinates::get).build());
    private final Setting<Double> gotoZ = sgCoordinates.add(new DoubleSetting.Builder().name("z-coordinates").description("Z-coordinates.").defaultValue(0.0D).sliderMin(-3.0E7D).min(-3.0E7D).sliderMax(3.0E7D).max(3.0E7D).visible(coordinates::get).build());
    private final Setting<Double> maxY = sgCoordinates.add(new DoubleSetting.Builder().name("max-y").description("Maximum of Y coordinate.").defaultValue(0.0D).min(0.0D).sliderMax(256.0D).max(256.0D).visible(coordinates::get).build());
    private final Setting<Boolean> useBaritone = sgAutomaticly.add(new BoolSetting.Builder().name("use-baritone").description("Uses baritone to walk a bit if stuck or cant find a path.").defaultValue(true).build());
    private final Setting<Integer> useBaritoneBlocks = sgAutomaticly.add(new IntSetting.Builder().name("blocks-amount").description("Amount of blocks to walk from current position.").defaultValue(50).min(0).sliderMax(500).max(500).visible(useBaritone::get).build());
    private final Setting<Boolean> autoSwitch = sgAutomaticly.add(new BoolSetting.Builder().name("auto-replace").description("Automatically replace elytra with few durability.").defaultValue(true).build());
    private final Setting<Boolean> autoEat = sgAutomaticly.add(new BoolSetting.Builder().name("auto-eat").description("Automatically eat Gaps/EGaps/other food to heal/to satiate you.").defaultValue(true).build());
    private final Setting<Double> minHealth = sgAutomaticly.add(new DoubleSetting.Builder().name("min-health").description("The minimum health required for auto gap to work.").defaultValue(18.0D).min(0.0D).sliderMax(36.0D).max(36.0D).visible(autoEat::get).build());
    private final Setting<Integer> minHunger = sgAutomaticly.add(new IntSetting.Builder().name("min-hunger").description("The level of hunger required for auto eat to work.").defaultValue(16).range(1, 19).sliderRange(1, 19).visible(autoEat::get).build());
    private final Setting<Boolean> gaps = sgAutomaticly.add(new BoolSetting.Builder().name("auto-gap").description("Automatically eat Gaps/EGaps to heal you.").defaultValue(true).visible(autoEat::get).build());
    private final Setting<Boolean> toggleOnPop = sgAutomaticly.add(new BoolSetting.Builder().name("auto-disable").description("Automatically disable when you pop a totem.").defaultValue(true).build());
    private final Setting<Boolean> kickOnPop = sgAutomaticly.add(new BoolSetting.Builder().name("auto-kick").description("Automatically kick you when you pop a totem.").defaultValue(true).build());

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static Thread thread;
    private static ArrayList<BlockPos> path;
    private static BlockPos goal;
    private static BlockPos previous;
    private static BlockPos lastSecondPos;
    private static DirectionMath direction;
    private static double x;
    private static double z;
    private static double jumpY = -1.0D;
    private static int packetsSent;
    private static int lagbackCounter;
    private static int useBaritoneCounter;
    private static boolean lagback;
    private static double blocksPerSecond;
    private static int blocksPerSecondCounter;
    private static TimerUtils blocksPerSecondTimer = new TimerUtils();
    private static TimerUtils packetTimer = new TimerUtils();
    private static TimerUtils fireworkTimer = new TimerUtils();
    private static TimerUtils takeoffTimer = new TimerUtils();
    public static int CountMode = 0;
    public static boolean aLava = false;
    public static double YCor = 0.0D;
    public static double EFspeed = 0.0D;
    public static double EFMspeed = 0.0D;
    public static int BBlocks = 0;
    public static double x1 = 0.0D;
    public static double y1 = 0.0D;
    public static double z1 = 0.0D;

    public ElytraBaritone() {
        super(NotWare.nwmisc, "elytra-baritone", "cookie client skid");
    }

    public void onActivate() {
        if (mode.get() == Mode.Highway) {
            CountMode = 1;
        } else if (mode.get() == Mode.Overworld) {
            CountMode = 2;
        } else if (mode.get() == Mode.Tunnel) {
            CountMode = 3;
        }

        x1 = gotoX.get();
        y1 = maxY.get();
        z1 = gotoZ.get();
        if (avoidLava.get()) {
            aLava = true;
        }

        YCor = maxY.get();
        EFspeed = elytraFlySpeed.get();
        EFMspeed = elytraFlyManuverSpeed.get();
        BBlocks = useBaritoneBlocks.get();
        int up = 1;
        if (!coordinates.get()) {
            if (Math.abs(Math.abs(mc.player.getX()) - Math.abs(mc.player.getZ())) <= 5.0D && Math.abs(mc.player.getX()) > 10.0D && Math.abs(mc.player.getZ()) > 10.0D && mode.get() == Mode.Highway) {
                direction = DirectionMath.getDiagonalDirection();
            } else {
                direction = DirectionMath.getDirection();
            }

            goal = generateGoalFromDirection(direction, up);
        } else {
            x = gotoX.get();
            z = gotoZ.get();
            goal = new BlockPos(gotoX.get(), mc.player.getY() + up, gotoZ.get());
        }

        thread = new Thread() {
            public void run() {
                while (ElytraBaritone.thread != null && ElytraBaritone.thread.equals(this)) {
                    try {
                        loop();
                    } catch (NullPointerException ignored) {
                        ignored.fillInStackTrace();
                    }

                    try {
                        sleep(50L);
                    } catch (InterruptedException ignored) {
                        ignored.fillInStackTrace();
                    }
                }

            }
        };
        blocksPerSecondTimer.reset();
        thread.start();
    }

    public void onDeactivate() {
        direction = null;
        path = null;
        useBaritoneCounter = 0;
        lagback = false;
        lagbackCounter = 0;
        blocksPerSecond = 0.0D;
        blocksPerSecondCounter = 0;
        lastSecondPos = null;
        jumpY = -1.0D;
        RenderPath.clearPath();
        PacketFlyUtils.toggle(false);
        ElytraFlyBot.toggle(false);
        clearStatus();
        suspend(thread);
        thread = null;
    }

    public void loop() {
        if (mc.player != null) {
            if (BlockUtilsWorld.distance(mc.player.getBlockPos(), goal) < 15) {
                mc.world.playSound(mc.player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.AMBIENT, 100.0F, 18.0F, true);
                warning("Goal reached!", false);
                toggle();
            } else {
                if (BotUtils.getItemStack(38).getItem() != Items.ELYTRA) {
                    if (!BotUtils.hasItem(Items.ELYTRA)) {
                        warning("You need an elytra", true);
                        toggle();
                        return;
                    }

                    int elytraSlot = BotUtils.getSlot(Items.ELYTRA);
                    BotUtils.clickSlot(elytraSlot);
                    BotUtils.clickSlot(38);
                    BotUtils.clickSlot(elytraSlot);
                }

                if (flyMode.get() == FlyMode.Firework && !BotUtils.hasItem(Items.FIREWORK_ROCKET)) {
                    warning("You need fireworks as your using firework mode", true);
                    toggle();
                } else if (!EntityUtils.isInRenderDistance(mc.player.getBlockPos())) {
                    info("We are in unloaded chunk. Waiting");
                    mc.player.setVelocity(0.0D, 0.0D, 0.0D);
                } else {
                    if (autoEat.get() && !BotUtils.isEating() && (flyMode.get() != FlyMode.Firework || flyMode.get() == FlyMode.Firework && !fireworkTimer.hasPassed(100))) {
                        float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
                        float hunger = (float) mc.player.getHungerManager().getFoodLevel();
                        if (health <= minHealth.get() || hunger <= (float) minHunger.get()) {
                            Item eat = null;

                            for (BotUtils.ItemStackUtil itemStack : BotUtils.getAllItems()) {
                                Item item = itemStack.itemStack.getItem();
                                if (item.isFood()) {
                                    if (item == Items.GOLDEN_APPLE && gaps.get()) {
                                        eat = item;
                                        break;
                                    }

                                    if (item != Items.CHORUS_FRUIT && item != Items.SPIDER_EYE) {
                                        eat = item;
                                    }
                                }
                            }

                            BotUtils.eatItem(eat);
                        }
                    }

                    if (autoSwitch.get()) {
                        ItemStack elytra = BotUtils.getItemStack(38);
                        if (BotUtils.getDurability(elytra) <= 5) {

                            for (BotUtils.ItemStackUtil itemStack : BotUtils.getAllItems()) {
                                if (itemStack.itemStack.getItem() == Items.ELYTRA && BotUtils.getDurability(itemStack.itemStack) >= 100) {
                                    BotUtils.clickSlot(itemStack.slotId);
                                    BotUtils.clickSlot(38);
                                    BotUtils.clickSlot(itemStack.slotId);
                                    break;
                                }
                            }
                        }
                    }

                    double preventPhase = jumpY + 0.6D;
                    if ((mc.player.isFallFlying() || mc.player.getY() < preventPhase || mc.player.isOnGround()) && PacketFlyUtils.toggled) {
                        sleep(1500);
                        if (mc.player.isFallFlying() || mc.player.getY() < preventPhase || mc.player.isOnGround()) {
                            PacketFlyUtils.toggle(false);
                            sleep(100);
                        }
                    }

                    if (!mc.player.isFallFlying()) {
                        ElytraFlyBot.toggle(false);
                        if (mc.player.isOnGround() && BotUtils.isSolid(mc.player.getBlockPos().add(0, 2, 0)) && useBaritone.get() && mode.get() == Mode.Highway) {
                            info("Using baritone because a block above is preventing takeoff");
                            useBaritone();
                        }

                        if (BotUtils.isSolid(mc.player.getBlockPos().add(0, 2, 0)) && mode.get() == Mode.Tunnel) {
                            if (BlockUtilsWorld.getBlock(mc.player.getBlockPos().add(0, 2, 0)) != Blocks.BEDROCK && BlockUtilsWorld.getBlock(mc.player.getBlockPos().add(0, 2, 0)) != Blocks.AIR) {
                                int pickaxe = InvUtils.findInHotbar(Items.NETHERITE_PICKAXE).getSlot();
                                if (pickaxe == -1) {
                                    pickaxe = InvUtils.findInHotbar(Items.DIAMOND_PICKAXE).getSlot();
                                }

                                if (pickaxe == -1) {
                                    pickaxe = InvUtils.findInHotbar(Items.IRON_PICKAXE).getSlot();
                                }

                                if (pickaxe == -1) {
                                    return;
                                }

                                info("Mining above block so we can takeoff");
                                BotUtils.centerMotionFull();
                                BlockUtilsWorld.Mine(mc.player.getBlockPos().add(0, 2, 0), pickaxe);
                            } else {
                                if (!useBaritone.get()) {
                                    warning("Above block is bedrock and usebaritone is false");
                                    toggle();
                                    return;
                                }

                                info("Using baritone to walk because above block is bedrock");
                                useBaritone();
                            }
                        }

                        if (jumpY != -1.0D && Math.abs(mc.player.getY() - jumpY) >= 2.0D && useBaritone.get() && direction != null && mode.get() == Mode.Highway) {
                            info("Using baritone to get back to the highway");
                            useBaritone();
                        }

                        if (packetsSent < 20) {
                            info("Trying to takeoff");
                        }

                        fireworkTimer.ms = 0L;
                        if (mc.player.isOnGround()) {
                            jumpY = mc.player.getY();
                            generatePath();
                            mc.player.jump();
                        } else if (mc.player.getY() < mc.player.prevY) {
                            if (takeoffMode.get() == TakeOff.PacketFly) {
                                if (mc.player.getY() > preventPhase && !PacketFlyUtils.toggled) {
                                    PacketFlyUtils.toggle(true);
                                }
                            } else if (takeoffMode.get() == TakeOff.Velocity) {
                                mc.player.setVelocity(0.0D, -0.04D, 0.0D);
                            }

                            if (packetsSent <= 15) {
                                if (takeoffTimer.hasPassed(650)) {
                                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                                    takeoffTimer.reset();
                                    packetTimer.reset();
                                    packetsSent++;
                                }
                            } else if (packetTimer.hasPassed(15000)) {
                                packetsSent = 0;
                            } else {
                                info("Waiting for 15s before sending elytra open packets again");
                            }
                        }

                    } else {
                        if (!PacketFlyUtils.toggled) {
                            packetsSent = 0;
                            double speed = Utils.getPlayerSpeed();
                            if (speed < 0.1D) {
                                useBaritoneCounter++;
                                if (useBaritoneCounter >= 15) {
                                    useBaritoneCounter = 0;
                                    if (!useBaritone.get()) {
                                        warning("We are stuck. UseBaritone setting would help");
                                        toggle();
                                        return;
                                    }

                                    info("Using baritone to walk a bit because we are stuck");
                                    useBaritone();
                                }
                            } else {
                                useBaritoneCounter = 0;
                            }

                            if (flyMode.get() == FlyMode.Firework) {
                                if (speed <= minSpeed.get()) {
                                    clickOnFirework();
                                }
                            } else if (flyMode.get() == FlyMode.ElytraFly) {
                                if (speed > 3.0D) {
                                    lagback = true;
                                }

                                if (lagback) {
                                    if (speed < 1.0D) {
                                        lagbackCounter++;
                                        if (lagbackCounter > 3) {
                                            lagback = false;
                                            lagbackCounter = 0;
                                        }
                                    } else {
                                        lagbackCounter = 0;
                                    }
                                }

                                if (fireworkTimer.hasPassed(fireworkDelay.get() * 1000) && !lagback) {
                                    clickOnFirework();
                                }
                            }
                        }

                        if (path == null || path.size() <= 20 || isNextPathTooFar()) {
                            generatePath();
                        }

                        int distance = 12;
                        if (mode.get() == Mode.Highway || flyMode.get() == FlyMode.ElytraFly) {
                            distance = 2;
                        }

                        boolean remove = false;
                        ArrayList<BlockPos> removePositions = new ArrayList();
                        Iterator var20 = path.iterator();

                        BlockPos pos;
                        while (var20.hasNext()) {
                            pos = (BlockPos) var20.next();
                            if (!remove && BlockUtilsWorld.distanceBetween(pos, mc.player.getBlockPos()) <= distance) {
                                remove = true;
                            }

                            if (remove) {
                                removePositions.add(pos);
                            }
                        }

                        for (var20 = removePositions.iterator(); var20.hasNext(); previous = pos) {
                            pos = (BlockPos) var20.next();
                            path.remove(pos);
                        }

                        if (path.size() > 0) {
                            if (direction != null) {
                                info("Going to " + direction.name);
                            } else {
                                info("Going to X: " + x + " Z: " + z);
                                if (blocksPerSecondTimer.hasPassed(1000)) {
                                    blocksPerSecondTimer.reset();
                                    if (lastSecondPos != null) {
                                        blocksPerSecondCounter++;
                                        blocksPerSecond += BlockUtilsWorld.distanceBetween(mc.player.getBlockPos(), lastSecondPos);
                                    }

                                    lastSecondPos = mc.player.getBlockPos();
                                }

                                int seconds = (int) (BlockUtilsWorld.distanceBetween(mc.player.getBlockPos(), goal) / (blocksPerSecond / blocksPerSecondCounter));
                                int h = seconds / 3600;
                                int m = seconds % 3600 / 60;
                                int s = seconds % 60;
                                info("Estimated arrival in " + Color.GREEN + h + "h " + m + "m " + s + "s");
                            }

                            if (flyMode.get() == FlyMode.Firework) {
                                MiningUtil1.BlockRotate((new BlockPos((Vec3i) path.get(path.size() - 1))).add(0.5D, 0.5D, 0.5D));
                            } else if (flyMode.get() == FlyMode.ElytraFly) {
                                ElytraFlyBot.toggle(true);
                                BlockPos next = null;
                                if (path.size() > 1) {
                                    next = path.get(path.size() - 2);
                                }

                                ElytraFlyBot.setMotion(path.get(path.size() - 1), next, previous);
                            }
                        }
                    }
                }
            }
        }
    }

    public void generatePath() {
        BlockPos[] positions = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(1, 0, 1), new BlockPos(-1, 0, -1), new BlockPos(-1, 0, 1), new BlockPos(1, 0, -1), new BlockPos(0, -1, 0), new BlockPos(0, 1, 0)};
        ArrayList<BlockPos> checkPositions = new ArrayList();
        if (mode.get() == Mode.Highway) {
            BlockPos[] list = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1), new BlockPos(1, 0, 1), new BlockPos(-1, 0, -1), new BlockPos(-1, 0, 1), new BlockPos(1, 0, -1)};
            checkPositions = new ArrayList(Arrays.asList(list));
        } else if (mode.get() == Mode.Overworld) {
            int radius = 3;

            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    for (int y = radius; y > -radius; y--) {
                        checkPositions.add(new BlockPos(x, y, z));
                    }
                }
            }
        } else if (mode.get() == Mode.Tunnel) {
            positions = new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)};
            checkPositions = new ArrayList(Arrays.asList(new BlockPos(0, -1, 0)));
        }

        if (path != null && path.size() != 0 && !isNextPathTooFar() && !mc.player.isOnGround()) {
            ArrayList temp = BotUtils.generatePath(path.get(0), goal, positions, checkPositions, 500);

            try {
                temp.addAll(path);
            } catch (NullPointerException ignored) {
                ignored.fillInStackTrace();
            }

            path = temp;
        } else {
            BlockPos start;
            if (mode.get() == Mode.Overworld) {
                start = mc.player.getBlockPos().add(0, 4, 0);
            } else if (Math.abs(jumpY - mc.player.getY()) <= 2.0D) {
                start = new BlockPos(mc.player.getX(), jumpY + 1.0D, mc.player.getZ());
            } else {
                start = mc.player.getBlockPos().add(0, 1, 0);
            }

            if (isNextPathTooFar()) {
                start = mc.player.getBlockPos();
            }

            path = BotUtils.generatePath(start, goal, positions, checkPositions, 500);
        }

        RenderPath.setPath(path, new Color(255, 0, 0, 150));
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player.getHealth() <= 0.0F && toggleOnPop.get()) {
            warning("You popped a totem.");
            toggle();
        }

        if (mc.player.getHealth() <= 0.0F && kickOnPop.get()) {
            warning("You popped a totem.");
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(new LiteralText("[AutoKick] You popped 1 totem!")));
        }

    }

    public static void useBaritone() {
        ElytraFlyBot.toggle(false);
        if (direction == DirectionMath.ZM) {
            z = (-BBlocks);
        } else if (direction == DirectionMath.XM) {
            x = (-BBlocks);
        } else if (direction == DirectionMath.XP) {
            x = BBlocks;
        } else if (direction == DirectionMath.ZP) {
            z = BBlocks;
        } else if (direction == DirectionMath.XP_ZP) {
            x = BBlocks;
            z = BBlocks;
        } else if (direction == DirectionMath.XM_ZM) {
            x = (-BBlocks);
            z = (-BBlocks);
        } else if (direction == DirectionMath.XP_ZM) {
            x = BBlocks;
            z = (-BBlocks);
        } else if (direction == DirectionMath.XM_ZP) {
            x = (-BBlocks);
            z = BBlocks;
        }

        mc.player.sendChatMessage("#goto " + x1 + " 60 " + z1);
        sleep(5000);
    }

    public static void clickOnFirework() {
        int s = InvUtils.findInHotbar(Items.FIREWORK_ROCKET).getSlot();
        if (mc.player.getMainHandStack().getItem() != Items.FIREWORK_ROCKET) {
            InvUtils.swap(s, false);
        }

        mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
        fireworkTimer.reset();
    }

    public static BlockPos generateGoalFromDirection(DirectionMath direction, int up) {
        if (direction == DirectionMath.ZM) {
            return new BlockPos(0.0D, mc.player.getY() + up, mc.player.getZ() - 4.2042069E7D);
        } else if (direction == DirectionMath.ZP) {
            return new BlockPos(0.0D, mc.player.getY() + up, mc.player.getZ() + 4.2042069E7D);
        } else if (direction == DirectionMath.XM) {
            return new BlockPos(mc.player.getX() - 4.2042069E7D, mc.player.getY() + up, 0.0D);
        } else if (direction == DirectionMath.XP) {
            return new BlockPos(mc.player.getX() + 4.2042069E7D, mc.player.getY() + up, 0.0D);
        } else if (direction == DirectionMath.XP_ZP) {
            return new BlockPos(mc.player.getX() + 4.2042069E7D, mc.player.getY() + up, mc.player.getZ() + 4.2042069E7D);
        } else if (direction == DirectionMath.XM_ZM) {
            return new BlockPos(mc.player.getX() - 4.2042069E7D, mc.player.getY() + up, mc.player.getZ() - 4.2042069E7D);
        } else {
            return direction == DirectionMath.XP_ZM ? new BlockPos(mc.player.getX() + 4.2042069E7D, mc.player.getY() + up, mc.player.getZ() - 4.2042069E7D) : new BlockPos(mc.player.getX() - 4.2042069E7D, mc.player.getY() + up, mc.player.getZ() + 4.2042069E7D);
        }
    }

    public static boolean isNextPathTooFar() {
        return BlockUtilsWorld.distanceBetween(mc.player.getBlockPos(), path.get(path.size() - 1)) > 15.0D;
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {
            ignored.fillInStackTrace();
        }
    }

    public static void clearStatus() {
        RenderPath.status = null;
    }

    public static void suspend(Thread thread) {
        if (thread != null) {
            thread.suspend();
        }

    }

    public enum Mode {
        Overworld,
        Highway,
        Tunnel
    }

    public enum TakeOff {
        PacketFly,
        Jump,
        Velocity
    }

    public enum FlyMode {
        ElytraFly,
        Firework
    }
}
