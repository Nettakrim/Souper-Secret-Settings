package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands.activePostShaders;
import static com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands.postShaders;
import static com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands.layerEffects;


public class StackShaderCommand {
	public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
		LiteralCommandNode<FabricClientCommandSource> stackNode = ClientCommandManager
			.literal("soup:stack")
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("add")
			.then(
				ClientCommandManager.argument("shader", StringArgumentType.string())
				.suggests(postShaders)
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
				.suggests(activePostShaders)
				.executes(StackShaderCommand::remove)
			)
			.build();

		stackNode.addChild(popNode);
		stackNode.addChild(randomNode);
		stackNode.addChild(removeNode);
		stackNode.addChild(setNode);


		LiteralCommandNode<FabricClientCommandSource> effectNode = ClientCommandManager
			.literal("effect")
			.build();

		LiteralCommandNode<FabricClientCommandSource> effectAddNode = ClientCommandManager
			.literal("add")
			.then(
				ClientCommandManager.argument("shader", StringArgumentType.string())
				.suggests(layerEffects)
				.executes(StackShaderCommand::addLayerEffect)
			)
			.build();

		LiteralCommandNode<FabricClientCommandSource> effectPopNode = ClientCommandManager
			.literal("undo")
			.executes(StackShaderCommand::popLayerEffect)
			.then(
				ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
				.executes(StackShaderCommand::popMultipleLayerEffect)
			)
			.build();

		stackNode.addChild(effectNode);
		effectNode.addChild(effectAddNode);
		effectNode.addChild(effectPopNode);
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
		SouperSecretSettingsClient.layer.pop(1);
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

	private static int addLayerEffect(CommandContext<FabricClientCommandSource> context) {
		String shader = StringArgumentType.getString(context, "shader");
		SouperSecretSettingsClient.updateToggle();
		return SouperSecretSettingsClient.addLayerEffect(shader) ? 1 : 0;
	}

	private static int popMultipleLayerEffect(CommandContext<FabricClientCommandSource> context) {
		int amount = IntegerArgumentType.getInteger(context, "amount");
		SouperSecretSettingsClient.layer.popLayerEffect(amount);
		SouperSecretSettingsClient.updateToggle();
		return 1;
	}

	private static int popLayerEffect(CommandContext<FabricClientCommandSource> context) {
		SouperSecretSettingsClient.layer.popLayerEffect(1);
		SouperSecretSettingsClient.updateToggle();
		return 1;
	}
}
