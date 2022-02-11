package me.notme.notware.modules.hud.visual;


import me.notme.notware.NotWare;
import me.notme.notware.modules.chat.ChatTweaks;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.hud.HUD;
import meteordevelopment.meteorclient.systems.modules.render.hud.modules.DoubleTextHudElement;

public class Watermark extends DoubleTextHudElement {
    public Watermark(HUD hud) {
        super(hud, "notware-watermark", "display notware watermark", "");
    }

    @Override
    protected String getRight() {
        ChatTweaks chatTweaks = Modules.get().get(ChatTweaks.class);
        if (chatTweaks.isActive() && chatTweaks.customPrefix.get()) {
            return chatTweaks.prefixText.get() + " " + NotWare.VERSION;
        }
        return "Notware " + NotWare.VERSION; }
}
