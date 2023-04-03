package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class StackShaderCommand {
	public static int add(CommandContext<FabricClientCommandSource> context) {
        return SouperSecretSettingsClient.setShader(StringArgumentType.getString(context, "shader"), true) ? 1 : -1;
	}

	public static int random(CommandContext<FabricClientCommandSource> context) {
		return SouperSecretSettingsClient.setShader("random", true) ? 1 : -1;
    }

	public static int pop(CommandContext<FabricClientCommandSource> context) {
		int size = SouperSecretSettingsClient.postProcessorStack.size();
		if (size == 0) {
			if (SouperSecretSettingsClient.isSouped) {
				SouperSecretSettingsClient.client.gameRenderer.disablePostProcessor();
				return 1;
			}
			return -1;
		}
		SouperSecretSettingsClient.postProcessorStack.remove(size-1);
        return 1;
    }	
}
