package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.util.Identifier;

public class AddCommand {
	public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
		LiteralCommandNode<FabricClientCommandSource> testNode = ClientCommandManager
			.literal("soup:add")
			.then(
				ClientCommandManager.argument("shader", IdentifierArgumentType.identifier())
				.suggests(SouperSecretSettingsCommands.postShaders)
				.executes((context -> add(context.getArgument("shader", Identifier.class), 1)))
				.then(
					ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
					.executes((context -> add(context.getArgument("shader", Identifier.class), IntegerArgumentType.getInteger(context, "amount"))))
				)
			)
			.build();

		return testNode;
	}

	public static int add(Identifier id, int amount) {
		return SouperSecretSettingsClient.soupRenderer.addShader(id, amount) ? 1 : -1;
	}
}
