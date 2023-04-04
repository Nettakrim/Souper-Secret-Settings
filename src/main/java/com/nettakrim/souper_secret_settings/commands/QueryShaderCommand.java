package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.StackData;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class QueryShaderCommand implements Command<FabricClientCommandSource> {
	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		if (SouperSecretSettingsClient.currentShader == null || !SouperSecretSettingsClient.isSouped) {
			SouperSecretSettingsClient.client.player.sendMessage(Text.literal("none"));
		} else {
			String text = SouperSecretSettingsClient.currentShader.id;
			for(StackData stackData : SouperSecretSettingsClient.postProcessorStack) {
				text += " -> "+stackData.data.id;
			}
			SouperSecretSettingsClient.client.player.sendMessage(Text.literal(text));
		}
        return 1;
	}
}
