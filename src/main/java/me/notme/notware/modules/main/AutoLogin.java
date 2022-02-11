package me.notme.notware.modules.main;

import me.notme.notware.NotWare;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import java.util.ArrayList;

public class AutoLogin extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<String> password = sgGeneral.add(new StringSetting.Builder()
        .name("password")
        .description("the password to log in with.")
        .defaultValue("password")
        .build()
    );

    private final ArrayList<String> loginMessages = new ArrayList<>() {{
        add("/login ");
        add("/login <password>");
    }};

    public AutoLogin() {
        super(NotWare.nwmisc, "auto-login", "cracked server lololololol");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMessageRecieve(ReceiveMessageEvent event) {
        for (String loginMessage: loginMessages) {
            if (event.getMessage().getString().contains(loginMessage)) mc.player.sendChatMessage("/login " + password.get());
        }
    }
}
