package me.notme.notware.modules.main;

import me.notme.notware.NotWare;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AutoTotem;
import meteordevelopment.meteorclient.systems.modules.combat.Offhand;
import meteordevelopment.meteorclient.systems.modules.player.AutoMend;
import meteordevelopment.meteorclient.systems.modules.player.OffhandCrash;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotme extends Module {
    private final SettingGroup sgDelay = settings.createGroup("Delay");
    private final Setting<Mode> takeTotemMode = sgDelay.add(new EnumSetting.Builder<Mode>().name("take-mode").description("The take totem in your hand.").defaultValue(Mode.NoAntiCheat).build());
    private final Setting<Integer> tickDelay = sgDelay.add(new IntSetting.Builder().name("take-delay").description("Totem's take delay.").defaultValue(1).min(0).sliderMax(20).build());

    private int ticks;
    private String totemCountString = "0";

    public AutoTotme() {
        super(NotWare.nwcombat, "auto-totme", "very epic auto totem 100% fail");
    }

    public void onActivate() {
        ticks = 0;
        if ((Modules.get().get(AutoMend.class)).isActive()) {
            (Modules.get().get(AutoMend.class)).toggle();
        }

        if ((Modules.get().get(Offhand.class)).isActive()) {
            (Modules.get().get(Offhand.class)).toggle();
        }

        if ((Modules.get().get(AutoTotem.class)).isActive()) {
            (Modules.get().get(AutoTotem.class)).toggle();
        }

        if ((Modules.get().get(OffhandCrash.class)).isActive()) {
            (Modules.get().get(OffhandCrash.class)).toggle();
        }
    }

    @EventHandler(priority = Integer.MAX_VALUE)
    private void POPS(PacketEvent.Receive e) {
        if (e.packet instanceof EntityStatusS2CPacket) {
            EntityStatusS2CPacket p = (EntityStatusS2CPacket) e.packet;
            if (p.getStatus() != 35) {
                return;
            }

            Entity entity = p.getEntity(mc.world);
            if (entity == null || !entity.equals(mc.player)) {
                return;
            }

            if (mc.currentScreen instanceof GenericContainerScreen) {
                CHTotem();
            }

            if (mc.currentScreen instanceof ShulkerBoxScreen) {
                SHTotem();
            }

            if (mc.currentScreen instanceof CraftingScreen) {
                CRTotem();
            }

            if (mc.currentScreen instanceof AnvilScreen) {
                ATotem();
            }
        }
    }

    private void CHTotem() {
        GenericContainerScreenHandler container = ((GenericContainerScreen) mc.currentScreen).getScreenHandler();
        if (container != null) {
            int slot = -1;

            for (int i = 0; i < container.slots.size(); i++) {
                if ((container.slots.get(i)).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                    slot = i;
                    break;
                }
            }

            if (slot > -1) {
                MOVE_TOTEM(slot);
                (container.slots.get(slot)).setStack(new ItemStack(Items.AIR));
            }
        }
    }

    private void SHTotem() {
        ShulkerBoxScreenHandler container = ((ShulkerBoxScreen) mc.currentScreen).getScreenHandler();
        if (container != null) {
            int slot = -1;

            for (int i = 0; i < container.slots.size(); i++) {
                if ((container.slots.get(i)).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                    slot = i;
                    break;
                }
            }

            if (slot > -1) {
                MOVE_TOTEM(slot);
                (container.slots.get(slot)).setStack(new ItemStack(Items.AIR));
            }
        }
    }

    private void ATotem() {
        AnvilScreenHandler container = ((AnvilScreen) mc.currentScreen).getScreenHandler();
        if (container != null) {
            int slot = -1;

            for (int i = 0; i < container.slots.size(); i++) {
                if (((Slot) container.slots.get(i)).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                    slot = i;
                    break;
                }
            }

            if (slot > -1) {
                MOVE_TOTEM(slot);
                (container.slots.get(slot)).setStack(new ItemStack(Items.AIR));
            }
        }
    }

    private void CRTotem() {
        CraftingScreenHandler container = ((CraftingScreen) mc.currentScreen).getScreenHandler();
        if (container != null) {
            int slot = -1;

            for (int i = 0; i < container.slots.size(); i++) {
                if ((container.slots.get(i)).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                    slot = i;
                    break;
                }
            }

            if (slot > -1) {
                MOVE_TOTEM(slot);
                (container.slots.get(slot)).setStack(new ItemStack(Items.AIR));
            }
        }
    }

    private void MOVE_TOTEM(int slot) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 40, SlotActionType.SWAP, mc.player);
    }

    private void InvTotem() {
        if (ticks >= tickDelay.get()) {
            ticks = 0;
            FindItemResult RESULT = InvUtils.find(new Item[]{Items.TOTEM_OF_UNDYING});
            int slot;
            if (takeTotemMode.get() == Mode.ForAntiCheat && RESULT.getCount() > 0) {
                slot = BRUHLOGIC.invIndexToSlotId(RESULT.getSlot());
                MOVE_TOTEM(slot);
            }

            if (takeTotemMode.get() == Mode.NoAntiCheat && RESULT.getCount() > 0) {
                slot = BRUHLOGIC.invIndexToSlotId(RESULT.getSlot());
                MOVE_TOTEM(slot);
            }
        } else {
            ticks++;
        }
    }

    @EventHandler(priority = 2147483646)
    private void onTick(TickEvent.Pre e) {
        Screen s = mc.currentScreen;
        if (mc.player != null && mc.world != null) {
            SET_TOTEM_COUNT();
            if (mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                if (s instanceof GenericContainerScreen) {
                    CHTotem();
                } else if (s instanceof ShulkerBoxScreen) {
                    SHTotem();
                } else if (s instanceof CraftingScreen) {
                    CRTotem();
                } else {
                    InvTotem();
                }
            }
        }
    }

    private void SET_TOTEM_COUNT() {
        totemCountString = Integer.toString(InvUtils.find(new Item[]{Items.TOTEM_OF_UNDYING}).getCount());
    }

    public String getInfoString() {
        return totemCountString;
    }

    public enum Mode {
        ForAntiCheat,
        NoAntiCheat
    }
}
