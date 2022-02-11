package me.notme.notware.modules.main;

import me.notme.notware.NotWare;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class nwprefix extends Module {

    public nwprefix() {
        super(NotWare.nwmisc, "notware-prefix", "flexing");
    }

    @Override
    public void onActivate() {
        ChatUtils.registerCustomPrefix("notware", this::getPrefix);
        ChatUtils.registerCustomPrefix("meteordevelopment.meteorclient", this::getPrefix);
    }

    public LiteralText getPrefix() {
        BaseText logo = new LiteralText("NotWare");
        LiteralText prefix = new LiteralText("");
        logo.setStyle(logo.getStyle().withFormatting(Formatting.RED));
        prefix.setStyle(prefix.getStyle().withFormatting(Formatting.GRAY));
        prefix.append("[");
        prefix.append(logo);
        prefix.append("] ");
        return prefix;
    }
}
