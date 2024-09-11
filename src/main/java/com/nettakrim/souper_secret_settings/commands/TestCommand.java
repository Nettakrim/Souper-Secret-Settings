package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.util.Identifier;

public class TestCommand implements Command<FabricClientCommandSource> {
	public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
		LiteralCommandNode<FabricClientCommandSource> testNode = ClientCommandManager
			.literal("soup:test")
			.then(
				ClientCommandManager.argument("shader", IdentifierArgumentType.identifier())
				.suggests(SouperSecretSettingsCommands.postShaders)
				.executes(new TestCommand())
			)
			.build();

		return testNode;
	}

	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return SouperSecretSettingsClient.soupRenderer.setShader((Identifier)context.getArgument("shader", Identifier.class)) ? 1 : -1;
	}
}
