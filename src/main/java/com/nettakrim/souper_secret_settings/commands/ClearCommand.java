package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ClearCommand {
	public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
				.literal("soup:remove")
				.build();

		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("all")
			.executes(context -> removeAll())
			.build();

		LiteralCommandNode<FabricClientCommandSource> topNode = ClientCommandManager
				.literal("top")
				.executes(context -> removeTop())
				.build();

		removeNode.addChild(clearNode);
		removeNode.addChild(topNode);
		return removeNode;
	}

	public static int removeAll() {
		return SouperSecretSettingsClient.soupRenderer.clearShader() ? 1 : -1;
	}

	public static int removeTop() {
		return SouperSecretSettingsClient.soupRenderer.removeTop() ? 1 : -1;
	}
}
