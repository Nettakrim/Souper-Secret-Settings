package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class RandomShaderCommand implements Command<FabricClientCommandSource> {
	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return SouperSecretSettingsClient.setShader("random") ? 1 : -1;
	}

	public static int randomMultiple(CommandContext<FabricClientCommandSource> context) {
		int amount = IntegerArgumentType.getInteger(context, "amount");
		SouperSecretSettingsClient.clearShaders();
		for (int x = 0; x < amount; x++) {
			if (!SouperSecretSettingsClient.stackShader("random", 1)) return -1;
		}
		return 1;
	}
}
