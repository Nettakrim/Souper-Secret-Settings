package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands.activeShaders;
import static com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands.shaders;

public class StackShaderCommand {
	public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
		LiteralCommandNode<FabricClientCommandSource> stackNode = ClientCommandManager
			.literal("soup:stack")
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("add")
			.then(
				ClientCommandManager.argument("shader", StringArgumentType.string())
				.suggests(shaders)
				.executes(StackShaderCommand::add)
				.then(
					ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
					.executes(StackShaderCommand::addMultiple)
				)
			)
			.build();

		LiteralCommandNode<FabricClientCommandSource> randomNode = ClientCommandManager
			.literal("random")
			.executes(StackShaderCommand::random)
			.then(
				ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
				.executes(StackShaderCommand::randomMultiple)
			)
			.build();

		LiteralCommandNode<FabricClientCommandSource> popNode = ClientCommandManager
			.literal("undo")
			.executes(StackShaderCommand::pop)
			.then(
				ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
				.executes(StackShaderCommand::popMultiple)
			)
			.build();

		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
			.literal("remove")
			.then(
				ClientCommandManager.argument("shader", StringArgumentType.string())
				.suggests(activeShaders)
				.executes(StackShaderCommand::remove)
			)
			.build();

		stackNode.addChild(popNode);
		stackNode.addChild(randomNode);
		stackNode.addChild(removeNode);
		stackNode.addChild(setNode);
		return stackNode;
	}

	private static int add(CommandContext<FabricClientCommandSource> context) {
        return SouperSecretSettingsClient.stackShader(StringArgumentType.getString(context, "shader"), 1) ? 1 : -1;
	}

	private static int addMultiple(CommandContext<FabricClientCommandSource> context) {
		String shader = StringArgumentType.getString(context, "shader");
		int amount = IntegerArgumentType.getInteger(context, "amount");
		return SouperSecretSettingsClient.stackShader(shader, amount) ? 1 : -1;
	}

	private static int random(CommandContext<FabricClientCommandSource> context) {
		return SouperSecretSettingsClient.stackShader("random", 1) ? 1 : -1;
    }

	private static int randomMultiple(CommandContext<FabricClientCommandSource> context) {
		int amount = IntegerArgumentType.getInteger(context, "amount");
		for (int x = 0; x < amount; x++) {
			if(!SouperSecretSettingsClient.stackShader("random", 1)) return -1;
		}
		return 1;
	}

	private static int pop(CommandContext<FabricClientCommandSource> context) {
		SouperSecretSettingsClient.layer.pop(0);
		SouperSecretSettingsClient.updateToggle();
        return 1;
    }

	private static int popMultiple(CommandContext<FabricClientCommandSource> context) {
		int amount = IntegerArgumentType.getInteger(context, "amount");
		SouperSecretSettingsClient.layer.pop(amount);
		SouperSecretSettingsClient.updateToggle();
		return 1;
	}

	private static int remove(CommandContext<FabricClientCommandSource> context) {
		String shader = StringArgumentType.getString(context, "shader");
		SouperSecretSettingsClient.layer.remove(shader);
		SouperSecretSettingsClient.updateToggle();
		return 1;
	}
}
