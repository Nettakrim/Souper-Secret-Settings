package com.nettakrim.souper_secret_settings;

import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands;

public class SouperSecretSettingsClient implements ClientModInitializer {
	public static final String MODID = "souper_secret_settings";
	public static final Logger LOGGER = LoggerFactory.getLogger("souper_secret_settings");

	public static MinecraftClient client;

	private static final TextColor textColor = TextColor.fromRgb(0xAAAAAA);
	private static final TextColor nameTextColor = TextColor.fromRgb(0xB6484C);

	public static SoupRenderer soupRenderer;

	@Override
	public void onInitializeClient() {
		client = MinecraftClient.getInstance();
		soupRenderer = new SoupRenderer();

		ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of("expanded_shaders"), FabricLoader.getInstance().getModContainer(MODID).orElseThrow(), Text.literal("Expanded Shaders"), ResourcePackActivationType.DEFAULT_ENABLED);

		SouperSecretSettingsCommands.initialize();
		Keybinds.init();
	}

	public static void say(String key, Object... args) {
		sayText(Text.translatable(MODID + ".say").setStyle(Style.EMPTY.withColor(nameTextColor)).append(Text.translatable(MODID + "." + key, args).setStyle(Style.EMPTY.withColor(textColor))));
	}

	public static void sayText(MutableText text) {
		if (client.player == null) return;
		client.player.sendMessage(Text.translatable(MODID + ".say").setStyle(Style.EMPTY.withColor(nameTextColor)).append(text.setStyle(Style.EMPTY.withColor(textColor))), false);
	}
}
