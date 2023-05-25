package com.nettakrim.souper_secret_settings;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands;
import com.nettakrim.souper_secret_settings.mixin.GameRendererAccessor;

public class SouperSecretSettingsClient implements ClientModInitializer {
	public static final String MODID = "souper_secret_settings";
	public static final Logger LOGGER = LoggerFactory.getLogger("souper_secret_settings");

	private static GameRendererAccessor gameRendererAccessor;
	public static MinecraftClient client;

	public static boolean isSoupToggledOff;
	public static boolean soupToggleStay;

	public static final ArrayList<StackData> postProcessorStack = new ArrayList<>();

	public static ShaderResourceLoader shaderResourceLoader;
	public static RecipeManager recipeManager;

	public static final ArrayList<ShaderData> shaderDatas = new ArrayList<>();
	public static final HashMap<String, String> entityLinks = new HashMap<>();

	private static boolean warningPrimed = true;
	private final static int shaderLimit = 100;

	private static final TextColor textColor = TextColor.fromRgb(0xAAAAAA);
	private static final TextColor nameTextColor = TextColor.fromRgb(0xB6484C);

	@Override
	public void onInitializeClient() {
		client = MinecraftClient.getInstance();

		shaderResourceLoader = new ShaderResourceLoader();

		recipeManager = new RecipeManager();

		ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("expanded_shaders"), FabricLoader.getInstance().getModContainer(MODID).orElseThrow(), Text.literal("Expanded Shaders"), ResourcePackActivationType.DEFAULT_ENABLED);

		SouperSecretSettingsCommands.initialize();
	}

	public static GameRendererAccessor getGameRendererAccessor() {
		if (gameRendererAccessor == null) {
			gameRendererAccessor = (GameRendererAccessor)client.gameRenderer;
		}
		return gameRendererAccessor;
	}

	public static boolean setShader(String id) {
		return stackShader(id, 0);
	}

	public static boolean stackShader(String id, int stack) {
		if (warningPrimed && stack+postProcessorStack.size() > shaderLimit) {
			say("shader.warn_stacking", shaderLimit);
			stack = shaderLimit-postProcessorStack.size();
			warningPrimed = false;
			if (stack <= 0) return false;
		}

		ShaderData shaderData = getShaderFromID(id);
		if (shaderData == null) {
			say("shader.missing", id);
			return false;
		}

		return stackShaderData(shaderData, stack);
	}

	public static boolean stackShaderData(ShaderData shaderData, int stack) {
		updateToggle();

		if (stack == 0) {
			clearShaders();
			stack = 1;
		}

		try {
			PostEffectProcessor postProcessor = new PostEffectProcessor(client.getTextureManager(), getGameRendererAccessor().getResourceManager(), client.getFramebuffer(), shaderData.shader);
			postProcessor.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
			StackData data = new StackData(postProcessor, shaderData);
			for (int x = 0; x < stack; x++) {
				postProcessorStack.add(data);
			}
			return true;
		}
		catch (IOException | JsonSyntaxException e) {
			LOGGER.warn("Failed to load shader \"{}\":\n{}", shaderData.id, e);
		}
		return false;
	}

	public static ShaderData getShaderFromID(String id) {
		if (id.equals("random")) {
			ShaderData shaderData = shaderDatas.get(getGameRendererAccessor().getRandom().nextInt(shaderDatas.size()));
			int size = postProcessorStack.size();
			if (size > 0 && shaderData == postProcessorStack.get(size-1).data()) {
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

	public static boolean tryLoadEntityShader(String type) {
		String recipeData = SouperSecretSettingsClient.entityLinks.get(type);
		if (recipeData == null) return false;
		return RecipeManager.loadFromRecipeData(recipeData, false);
	}

	public static void clearShaders() {
		warningPrimed = true;
		postProcessorStack.clear();
		updateToggle();
	}

	public static void clearResources() {
		shaderDatas.clear();
		entityLinks.clear();
	}

	public static void shaderListAdd(String namespace, String id) {
		shaderDatas.add(new ShaderData(namespace, id));
	}

	public static void shaderListClearNamespace(String namespace) {
		shaderDatas.removeIf(data -> data.shader.getNamespace().equals(namespace));
	}

	public static void entityLinksAdd(String id, String shader) {
		entityLinks.put(id, shader);
	}

	public static void say(String key, Object... args) {
		if (client.player == null) return;
		Text text = Text.translatable(MODID+".say").setStyle(Style.EMPTY.withColor(nameTextColor)).append(Text.translatable(MODID+"."+key, args).setStyle(Style.EMPTY.withColor(textColor)));
		client.player.sendMessage(text);
	}

	public static void say(MutableText text) {
		if (client.player == null) return;
		client.player.sendMessage(Text.translatable(MODID+".say").setStyle(Style.EMPTY.withColor(nameTextColor)).append(text.setStyle(Style.EMPTY.withColor(textColor))));
	}

	public static void updateToggle() {
		if (isSoupToggledOff && !soupToggleStay) {
			say("shader.toggle_prompt");
			isSoupToggledOff = false;
		}
	}
}
