package com.nettakrim.souper_secret_settings;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
	public static boolean isSoupToggledOff;
	public static ShaderData currentShader;

	public static ArrayList<StackData> postProcessorStack = new ArrayList<StackData>();

	public static ShaderResourceLoader shaderResourceLoader;

	public static ArrayList<ShaderData> shaderDatas = new ArrayList<ShaderData>();

	@Override
	public void onInitializeClient() {
		client = MinecraftClient.getInstance();

		shaderResourceLoader = new ShaderResourceLoader();

		ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("expanded_shaders"), FabricLoader.getInstance().getModContainer(MODID).orElseThrow(), Text.literal("Expanded Shaders"), ResourcePackActivationType.DEFAULT_ENABLED);

		SouperSecretSettingsCommands.initialize();
	}

	public static boolean setShader(String id) {
		return setShader(id, false);
	}

	public static boolean setShader(String id, boolean stack) {
		if (gameRendererAccessor == null) {
			gameRendererAccessor = (GameRendererAccessor)client.gameRenderer;
		}

		if (isSoupToggledOff) {
			client.player.sendMessage(Text.translatable(MODID+".toggle_prompt"));
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
			ShaderData shaderData = shaderDatas.get(gameRendererAccessor.getRandom().nextInt(shaderDatas.size()));
			int size = postProcessorStack.size();
			if (size == 0) {
				if (shaderData == currentShader) {
					return getShaderFromID(id);
				}
			} else if (shaderData == postProcessorStack.get(size-1).data) {
				return getShaderFromID(id);
			}
			return shaderData;
		} else {
			for (ShaderData shaderData : shaderDatas) {
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

	public static void shaderListClear() {
		shaderDatas.clear();
	}

	public static void shaderListAdd(String namespace, String id) {
		shaderDatas.add(new ShaderData(namespace, id));
	}

	public static void shaderListClearNamespace(String namespace) {
		shaderDatas.removeIf(data -> data.shader.getNamespace().equals(namespace));
	}
}
