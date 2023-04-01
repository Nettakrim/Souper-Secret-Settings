package com.nettakrim.souper_secret_settings;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands;
import com.nettakrim.souper_secret_settings.mixin.GameRendererAccessor;

public class SouperSecretSettingsClient implements ClientModInitializer {
	public static final String MODID = "souper_secret_settings";
	public static final Logger LOGGER = LoggerFactory.getLogger("souper_secret_settings");

	public static GameRendererAccessor gameRendererAccessor;
	public static MinecraftClient client;

	public static boolean isSouped;
	public static Identifier currentShader;

	@Override
	public void onInitializeClient() {
		client = MinecraftClient.getInstance();

		SouperSecretSettingsCommands.initialize();
	}

	public static boolean setShader(String id) {
		if (gameRendererAccessor == null) {
			gameRendererAccessor = (GameRendererAccessor)client.gameRenderer;
		}

		if (id.equals("none")) {
			client.gameRenderer.disablePostProcessor();
			isSouped = false;
		} else {
			Identifier identifier = getShaderFromID(id);
			if (identifier == null) return false;
			gameRendererAccessor.invokeLoadPostProcessor(identifier);
			currentShader = identifier;
			isSouped = true;
		}
		return true;
	}

	public static Identifier getShaderFromID(String id) {
		if (id.equals("random")) {
			Identifier identifier = GameRendererAccessor.getSuperSecretSettings()[gameRendererAccessor.getRandom().nextInt(GameRendererAccessor.getSuperSecretSettings().length)];
			return identifier == currentShader ? getShaderFromID(id) : identifier;
		} else {
			String uncroppedID = uncropID(id);
			for (Identifier identifier : GameRendererAccessor.getSuperSecretSettings()) {
				if (uncroppedID.equals(identifier.toString())) {
					return identifier;
				}
			}
			return null;
		}
	}

	public static String cropID(String id) {
		return id.substring(23, id.length()-5);
	}

	public static String uncropID(String id) {
		return "minecraft:shaders/post/"+id+".json";
	}
}
