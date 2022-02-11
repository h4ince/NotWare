package me.notme.notware;


import me.notme.notware.commands.ExploitTeleportCommand;
import me.notme.notware.modules.elytra.bot.ElytraBaritone;
import me.notme.notware.modules.hud.items.*;
import me.notme.notware.modules.chat.ArmorAlert;
import me.notme.notware.modules.chat.BurrowAlert;
import me.notme.notware.modules.chat.ChatTweaks;
import me.notme.notware.modules.chat.PopCounter;
import me.notme.notware.modules.hud.misc.Welcome;
import me.notme.notware.modules.hud.stats.*;
import me.notme.notware.modules.main.*;
import me.notme.notware.modules.hud.visual.Logo;
import me.notme.notware.modules.hud.visual.VisualBinds;
import me.notme.notware.modules.hud.visual.Watermark;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.item.Items;

import java.lang.invoke.MethodHandles;


public class NotWare extends MeteorAddon {
	public static final Logger LOG = LogManager.getLogger();
	public static final Category nwcombat = new Category("NotWare Combat", Items.END_CRYSTAL.getDefaultStack());
    public static final Category nwmisc = new Category("NotWare Misc", Items.REDSTONE.getDefaultStack());
	public static final String VERSION = "0.22";

	@Override
	public void onInitialize() {
		LOG.info("Initializing NotWare");

		MeteorClient.EVENT_BUS.registerLambdaFactory("me.notme.notware", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

		//Modules
        Modules.get().add(new AutoXP());
        Modules.get().add(new AutoRespawn());
        Modules.get().add(new AnchorAura());
        Modules.get().add(new ArmorAlert());
        Modules.get().add(new BedAura());
        Modules.get().add(new BedDisabler());
        Modules.get().add(new BurrowAlert());
        Modules.get().add(new BurrowBreaker());
        Modules.get().add(new ChatTweaks());
        Modules.get().add(new NametagsPlus());
        Modules.get().add(new PopCounter());
        Modules.get().add(new RPC());
        Modules.get().add(new SelfTrapPlus());
        Modules.get().add(new SurroundPlus());
        Modules.get().add(new SoundLocator());
        Modules.get().add(new AutoTNT());
        Modules.get().add(new SkeletonESP());
        Modules.get().add(new NotifierPlus());
        Modules.get().add(new AnchorAuraRewrite());
        Modules.get().add(new AutoWither());
        Modules.get().add(new AntiVanish());
        Modules.get().add(new AutoSalDupe());
        Modules.get().add(new PingSpoof());
        Modules.get().add(new AutoTotme());
	    Modules.get().add(new PearlPredict());
	    Modules.get().add(new ChorusPredict());
        Modules.get().add(new PacketUse());
        Modules.get().add(new AutoCrystalHead());
        Modules.get().add(new AutoLogin());
        Modules.get().add(new AutoBypass());
        Modules.get().add(new EntityFly());
        Modules.get().add(new AutoCrymstal());
        Modules.get().add(new InstaMineBypass());
        Modules.get().add(new AutoBedCraft());
        Modules.get().add(new AutoCityPlus());
        Modules.get().add(new nwprefix());
	Modules.get().add(new AutoFrameDupe());
    Modules.get().add(new SmartHoleFiller());

        Commands commands = Commands.get();
        commands.add(new ExploitTeleportCommand());


        //HUD
        HUD hud = Modules.get().get(HUD.class);
        //Item Counters
        hud.elements.add(new Beds(hud));
        hud.elements.add(new Crystals(hud));
        hud.elements.add(new Gaps(hud));
        hud.elements.add(new TextItems(hud));
        hud.elements.add(new XP(hud));
        //Stats
        hud.elements.add(new Deaths(hud));
        hud.elements.add(new Highscore(hud));
        hud.elements.add(new KDRatio(hud));
        hud.elements.add(new Killstreak(hud));
        hud.elements.add(new Kills(hud));
        //Visual
        hud.elements.add(new Logo(hud));
        hud.elements.add(new VisualBinds(hud));
        hud.elements.add(new Watermark(hud));
        hud.elements.add(new Welcome(hud));

        Config.get().customWindowTitleText = "Notware " + NotWare.VERSION;
	}

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(nwcombat);
        Modules.registerCategory(nwmisc);
	}
}
