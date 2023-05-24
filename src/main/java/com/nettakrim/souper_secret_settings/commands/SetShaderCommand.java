package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class SetShaderCommand implements Command<FabricClientCommandSource> {
	public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("soup:set")
			.then(
				ClientCommandManager.argument("shader", StringArgumentType.string())
				.suggests(SouperSecretSettingsCommands.shaders)
				.executes(new SetShaderCommand())
				.then(
					ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
					.executes(SetShaderCommand::setMultiple)
				)
			)
			.build();

		return setNode;
	}

	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return SouperSecretSettingsClient.setShader(StringArgumentType.getString(context, "shader")) ? 1 : -1;
	}

	private static int setMultiple(CommandContext<FabricClientCommandSource> context) {
		String shader = StringArgumentType.getString(context, "shader");
		int amount = IntegerArgumentType.getInteger(context, "amount");
		SouperSecretSettingsClient.clearShaders();
		SouperSecretSettingsClient.stackShader(shader, amount);
		return 1;
	}
}
