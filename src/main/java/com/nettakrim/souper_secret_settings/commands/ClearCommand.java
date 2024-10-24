package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ClearCommand {
	public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("soup:clear")
			.executes(context -> clear())
			.build();

		return clearNode;
	}

	public static int clear() {
		return SouperSecretSettingsClient.soupRenderer.clearShader() ? 1 : -1;
	}
}
