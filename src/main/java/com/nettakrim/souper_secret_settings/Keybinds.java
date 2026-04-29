package com.nettakrim.souper_secret_settings;

import dev.dannytaylor.luminance.client.keybindings.KeybindingHelper;
import dev.dannytaylor.luminance.client.shaders.RenderLocations;
import com.nettakrim.souper_secret_settings.commands.OptionCommand;
import com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static final KeyMapping openGUI = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "open_gui", GLFW.GLFW_KEY_U);
    public static final KeyMapping toggleSoup = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "cycle_mode", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping clear = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "clear", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping undo = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "undo", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping redo = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "redo", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping random = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "random", GLFW.GLFW_KEY_UNKNOWN);
    public static final KeyMapping sound = KeybindingHelper.getKeybinding(SouperSecretSettingsClient.MODID, SouperSecretSettingsClient.MODID, "sound", GLFW.GLFW_KEY_UNKNOWN);

    public static void tick() {
        if (openGUI.consumeClick()) {
            SouperSecretSettingsClient.soupGui.open(SouperSecretSettingsClient.soupGui.getCurrentScreenType(), true);
        }
        if (toggleSoup.consumeClick()) {
            if (SouperSecretSettingsClient.soupData.config.disableState > 0) {
                SouperSecretSettingsClient.soupData.config.disableState = 0;
                OptionCommand.setRenderLocation(RenderLocations.GAME);
            } else if (SouperSecretSettingsClient.soupRenderer.getRenderLocation().equals(RenderLocations.GAME)) {
                OptionCommand.setRenderLocation(RenderLocations.UI);
            } else {
                OptionCommand.toggle(false);
            }
        }
        if (undo.consumeClick()) {
            SouperSecretSettingsClient.actions.undo();
        }
        if (redo.consumeClick()) {
            SouperSecretSettingsClient.actions.redo();
        }
        if (clear.consumeClick()) {
            SouperSecretSettingsCommands.layerCommand.removeAll(null);
        }
        if (random.consumeClick()) {
            SouperSecretSettingsCommands.shaderCommand.add(Identifier.parse("random_edible"), 1, -1, true);
        }
        if (sound.consumeClick()) {
            RandomSound.play();
        }
    }
}
