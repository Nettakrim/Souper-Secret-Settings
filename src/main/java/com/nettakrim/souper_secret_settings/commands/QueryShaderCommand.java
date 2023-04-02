package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class QueryShaderCommand implements Command<FabricClientCommandSource> {
	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		if (SouperSecretSettingsClient.currentShader == null || !SouperSecretSettingsClient.isSouped) {
			SouperSecretSettingsClient.client.player.sendMessage(Text.literal("none"));
		} else {
			SouperSecretSettingsClient.client.player.sendMessage(Text.literal(SouperSecretSettingsClient.currentShader.id));
		}
        return 1;
	}
}
