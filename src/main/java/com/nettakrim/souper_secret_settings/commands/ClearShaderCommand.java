package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ClearShaderCommand implements Command<FabricClientCommandSource> {
	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        SouperSecretSettingsClient.clearShaders();
		return 1;
	}
}

