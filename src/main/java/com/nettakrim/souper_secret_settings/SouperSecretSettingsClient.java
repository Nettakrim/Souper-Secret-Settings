package com.nettakrim.souper_secret_settings;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands;
import com.nettakrim.souper_secret_settings.mixin.GameRendererAccessor;

public class SouperSecretSettingsClient implements ClientModInitializer {
	public static final String MODID = "souper_secret_settings";
	public static final Logger LOGGER = LoggerFactory.getLogger("souper_secret_settings");

	public static GameRendererAccessor gameRendererAccessor;
	public static MinecraftClient client;

	public static boolean isSouped;
	public static boolean canRestore;
	public static ShaderData currentShader;

	public static ArrayList<StackData> postProcessorStack = new ArrayList<StackData>();

	public static ShaderData[] shader_datas = new ShaderData[] {
		new ShaderData("notch"),
		new ShaderData("fxaa"),
		new ShaderData("art"),
		new ShaderData("bumpy"),
		new ShaderData("blobs2"),
		new ShaderData("pencil"),
		new ShaderData("color_convolve"),
		new ShaderData("deconverge"),
		new ShaderData("flip"),
		new ShaderData("invert"),
		new ShaderData("ntsc"),
		new ShaderData("outline"),
		new ShaderData("phosphor"),
		new ShaderData("scan_pincushion"),
		new ShaderData("sobel"),
		new ShaderData("bits"),
		new ShaderData("desaturate"),
		new ShaderData("green"),
		new ShaderData("blur"),
		new ShaderData("wobble"),
		new ShaderData("blobs"),
		new ShaderData("antialias"),
		new ShaderData("creeper"),
		new ShaderData("spider"),
		//bonus shaders!
		new ShaderData("sequins", MODID),
		new ShaderData("color_blind", MODID),
		new ShaderData("toxic_waste", MODID),
		new ShaderData("blobs_outline", MODID),
		new ShaderData("glitchy", MODID),
		new ShaderData("retro", MODID),
		new ShaderData("sepia", MODID),
		new ShaderData("color_bleed", MODID),
		new ShaderData("interference", MODID),
		new ShaderData("thermal", MODID),
		new ShaderData("kaleidoscope", MODID)
	};

	@Override
	public void onInitializeClient() {
		client = MinecraftClient.getInstance();

		SouperSecretSettingsCommands.initialize();
	}

	public static boolean setShader(String id) {
		return setShader(id, false);
	}

	public static boolean setShader(String id, boolean stack) {
		if (gameRendererAccessor == null) {
			gameRendererAccessor = (GameRendererAccessor)client.gameRenderer;
		}

		if (id.equals("none")) {
			client.gameRenderer.disablePostProcessor();
		} else {
			ShaderData shaderData = getShaderFromID(id);
			if (shaderData == null) return false;
			if (!isSouped || !stack) {
				gameRendererAccessor.invokeLoadPostProcessor(shaderData.shader);
				currentShader = shaderData;
				isSouped = true;
				postProcessorStack.clear();
			} else if (stack) {
				try {
					PostEffectProcessor postProcessor = new PostEffectProcessor(client.getTextureManager(), gameRendererAccessor.getResourceManager(), client.getFramebuffer(), shaderData.shader);
					postProcessor.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
					postProcessorStack.add(new StackData(postProcessor, shaderData));
					return true;
				}
				catch (IOException iOException) {
					LOGGER.warn("Failed to load shader: {}", (Object)id, (Object)iOException);
				}
				catch (JsonSyntaxException jsonSyntaxException) {
					LOGGER.warn("Failed to parse shader: {}", (Object)id, (Object)jsonSyntaxException);
				}
				return false;
			}
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

	public static void clearShaders() {
		isSouped = false;
		canRestore = false;
		postProcessorStack.clear();
	}
}
