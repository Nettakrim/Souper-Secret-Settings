package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ToggleShadersCommand implements Command<FabricClientCommandSource> {
	public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
		LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
			.literal("soup:toggle")
			.executes(new ToggleShadersCommand())
			.build();

		LiteralCommandNode<FabricClientCommandSource> stayNode = ClientCommandManager
			.literal("stay")
			.executes(ToggleShadersCommand::toggleStay)
			.build();

		toggleNode.addChild(stayNode);
		return toggleNode;
	}

	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        SouperSecretSettingsClient.isSoupToggledOff = !SouperSecretSettingsClient.isSoupToggledOff;
		SouperSecretSettingsClient.soupToggleStay = false;
        return 1;
	}

	private static int toggleStay(CommandContext<FabricClientCommandSource> context) {
		boolean to = !SouperSecretSettingsClient.isSoupToggledOff;
		SouperSecretSettingsClient.isSoupToggledOff = to;
		SouperSecretSettingsClient.soupToggleStay = to;
		return 1;
	}
}
