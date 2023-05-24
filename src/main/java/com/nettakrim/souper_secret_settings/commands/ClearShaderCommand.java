package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ClearShaderCommand implements Command<FabricClientCommandSource> {
	public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("soup:clear")
			.executes(new ClearShaderCommand())
			.build();

		return clearNode;
	}

	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        SouperSecretSettingsClient.clearShaders();
		return 1;
	}
}

