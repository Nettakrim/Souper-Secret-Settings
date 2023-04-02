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
	public static ShaderData currentShader;

	public static Identifier blurIdentifier = new Identifier("shaders/post/blur.json");

	public static ShaderData[] shader_datas = new ShaderData[] {
		new ShaderData("notch", false),
		new ShaderData("fxaa", false),
		new ShaderData("art", false),
		new ShaderData("bumpy", false),
		new ShaderData("blobs2", false),
		new ShaderData("pencil", false),
		new ShaderData("color_convolve", false),
		new ShaderData("deconverge", false),
		new ShaderData("flip", false),
		new ShaderData("invert", false),
		new ShaderData("ntsc", false),
		new ShaderData("outline", false),
		new ShaderData("phosphor", false),
		new ShaderData("scan_pincushion", false),
		new ShaderData("sobel", false),
		new ShaderData("bits", false),
		new ShaderData("desaturate", false),
		new ShaderData("green", false),
		new ShaderData("blur", true),
		new ShaderData("wobble", false),
		new ShaderData("blobs", false),
		new ShaderData("antialias", false),
		new ShaderData("creeper", false),
		new ShaderData("spider", false),
		//bonus shaders!
		new ShaderData("blobs_sobel", MODID, false),
		new ShaderData("color_blind", MODID, false),
		new ShaderData("toxic_waste", MODID, false),
		new ShaderData("blobs_outline", MODID, false),
		new ShaderData("glitchy", MODID, false),
		new ShaderData("retro", MODID, false)
	};

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
			ShaderData shaderData = getShaderFromID(id);
			if (shaderData == null) return false;
			gameRendererAccessor.invokeLoadPostProcessor(shaderData.shader);
			currentShader = shaderData;
			isSouped = true;
		}
		return true;
	}

	public static ShaderData getShaderFromID(String id) {
		if (id.equals("random")) {
			ShaderData shaderData = shader_datas[gameRendererAccessor.getRandom().nextInt(shader_datas.length)];
			return shaderData == currentShader ? getShaderFromID(id) : shaderData;
		} else {
			for (ShaderData shaderData : shader_datas) {
				if (id.equals(shaderData.id)) {
					return shaderData;
				}
			}
			return null;
		}
	}

	public static boolean removeVignette() {
		return isSouped && currentShader.disableVignette;
	}
}
