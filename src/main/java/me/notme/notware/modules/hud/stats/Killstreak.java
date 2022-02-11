package me.notme.notware.modules.hud.stats;

import me.notme.notware.utils.misc.Stats;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class Killstreak extends DoubleTextHudElement {
    public Killstreak(HUD hud) {
        super(hud, "killstreak", "Display your current killstreak", "Killstreak: ");
    }

    @Override
    protected String getRight() {return String.valueOf(Stats.killStreak); }
}
