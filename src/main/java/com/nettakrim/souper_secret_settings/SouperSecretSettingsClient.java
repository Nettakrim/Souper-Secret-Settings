package com.nettakrim.souper_secret_settings;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.client.events.Events;
import com.mclegoman.luminance.client.texture.ResourcePackHelper;
import com.nettakrim.souper_secret_settings.actions.Actions;
import com.nettakrim.souper_secret_settings.data.SoupData;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.shaders.SoupReloader;
import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import com.nettakrim.souper_secret_settings.shaders.SoupUniforms;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculations;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands;

public class SouperSecretSettingsClient implements ClientModInitializer {
	public static final String MODID = "souper_secret_settings";
	private static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final TextColor textColor = TextColor.fromRgb(0xAAAAAA);
	public static final TextColor nameTextColor = TextColor.fromRgb(0xB6484C);

	public static SoupData soupData;
	public static SoupRenderer soupRenderer;
	public static SoupGui soupGui;
	public static Actions actions;

	@Override
	public void onInitializeClient() {
		soupData = new SoupData();
		soupRenderer = new SoupRenderer();
		soupGui = new SoupGui();
		actions = new Actions();

		ResourcePackHelper.register(Identifier.parse("soup"), FabricLoader.getInstance().getModContainer(MODID).orElseThrow(), translate("resourcepack_soup"), PackActivationType.DEFAULT_ENABLED);
		Keybinds.tick();

		SoupUniforms.register();
		Calculations.register();
		SouperSecretSettingsCommands.initialize();

		ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			if (!ClientData.minecraft.isGameLoadFinished()) {
				return;
			}

			Keybinds.tick();
			soupData.tick();
			soupRenderer.tick();
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> soupData.saveConfig());

		Events.ClientResourceReloaders.register(Identifier.fromNamespaceAndPath(MODID, "shaders"), new SoupReloader());

		Identifier transfer = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "transfer");
		Events.AfterClientResourceReload.register(transfer, () -> ClientData.minecraft.schedule(() -> {
            soupData.config.transferOldData();
            Events.AfterClientResourceReload.remove(transfer);
        }));
	}

	public static void say(String key, int priority, Object... args) {
		sayStyled(translate(key, args).setStyle(Style.EMPTY.withColor(textColor)), priority);
	}

	public static void sayStyled(MutableComponent text, int priority) {
		sayRaw(Component.translatable(MODID + ".say").setStyle(Style.EMPTY.withColor(nameTextColor)).append(text.setStyle(Style.EMPTY.withColor(textColor))), priority);
	}

	public static void sayRaw(MutableComponent text, int priority) {
		if (ClientData.minecraft.player == null || priority < soupData.config.messageFilter) return;
		ClientData.minecraft.player.displayClientMessage(text, false);
	}

	public static MutableComponent translate(String key, Object... args) {
		return Component.translatable(MODID+"."+key, args);
	}

	public static void log(Object... args) {
		StringBuilder s = new StringBuilder();
		for (Object object : args) {
			if (!s.isEmpty()) {
				s.append(" ");
			}
			s.append(object);
		}
		LOGGER.info(s.toString());
	}

	public static void consumeItem(ItemStack stack) {
		if (stacksMatch(stack, soupData.config.randomItem)) {
			SouperSecretSettingsClient.soupRenderer.randomTimer = 0;
			SouperSecretSettingsCommands.shaderCommand.removeAll(null);
			SouperSecretSettingsCommands.shaderCommand.add(Identifier.parse(SouperSecretSettingsClient.soupData.config.randomShader), SouperSecretSettingsClient.soupData.config.randomCount, -1, true);
			SouperSecretSettingsClient.soupRenderer.randomTimer = SouperSecretSettingsClient.soupData.config.randomDuration;
			if (SouperSecretSettingsClient.soupData.config.randomSound) {
				RandomSound.play();
			}
		} else if (stacksMatch(stack, soupData.config.clearItem)) {
			SouperSecretSettingsClient.soupRenderer.randomTimer = 0;
			SouperSecretSettingsCommands.shaderCommand.removeAll(null);
		}
	}

	private static boolean stacksMatch(ItemStack stack, ItemStack reference) {
		return stack.is(reference.getItem()) && stack.getComponentsPatch().toString().equals(reference.getComponentsPatch().toString());
	}
}
