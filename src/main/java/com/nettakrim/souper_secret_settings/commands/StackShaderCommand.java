package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class StackShaderCommand {
	public static int add(CommandContext<FabricClientCommandSource> context) {
        return SouperSecretSettingsClient.stackShader(StringArgumentType.getString(context, "shader"), 1) ? 1 : -1;
	}

	public static int addMultiple(CommandContext<FabricClientCommandSource> context) {
		String shader = StringArgumentType.getString(context, "shader");
		int amount = IntegerArgumentType.getInteger(context, "amount");
		return SouperSecretSettingsClient.stackShader(shader, amount) ? 1 : -1;
	}

	public static int random(CommandContext<FabricClientCommandSource> context) {
		return SouperSecretSettingsClient.stackShader("random", 1) ? 1 : -1;
    }

	public static int randomMultiple(CommandContext<FabricClientCommandSource> context) {
		int amount = IntegerArgumentType.getInteger(context, "amount");
		for (int x = 0; x < amount; x++) {
			if(!SouperSecretSettingsClient.stackShader("random", 1)) return -1;
		}
		return 1;
	}

	public static int pop(CommandContext<FabricClientCommandSource> context) {
		int size = SouperSecretSettingsClient.postProcessorStack.size();
		SouperSecretSettingsClient.postProcessorStack.remove(size-1);
        return 1;
    }

	public static int popMultiple(CommandContext<FabricClientCommandSource> context) {
		int amount = IntegerArgumentType.getInteger(context, "amount");
		int size = SouperSecretSettingsClient.postProcessorStack.size();
		int index = Math.max(size-amount, 0);
		if (index == 0) {
			SouperSecretSettingsClient.clearShaders();
			return 1;
		}
		SouperSecretSettingsClient.postProcessorStack.subList(index, size).clear();
		return 1;
	}

	public static int remove(CommandContext<FabricClientCommandSource> context) {
		String shader = StringArgumentType.getString(context, "shader");
		boolean cleared = SouperSecretSettingsClient.postProcessorStack.removeIf(x -> (x.data().id.equals(shader)));
		if (SouperSecretSettingsClient.postProcessorStack.size() == 0) {
			SouperSecretSettingsClient.clearShaders();
		}
		return cleared ? 1 : -1;
	}
}
