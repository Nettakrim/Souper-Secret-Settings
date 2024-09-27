package com.nettakrim.souper_secret_settings;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.client.keybindings.KeybindingHelper;
import com.nettakrim.souper_secret_settings.screen.StackScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static final KeyBinding openGUI;
    static {
        openGUI = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "open_gui", GLFW.GLFW_KEY_K);
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (ClientData.minecraft.isFinishedLoading()) {
                tick();
            }
        });
    }

    public static void tick() {
        if (openGUI.wasPressed()) {
            ClientData.minecraft.setScreen(new StackScreen());
            SouperSecretSettingsClient.LOGGER.info("open gui");
        }
    }
}
