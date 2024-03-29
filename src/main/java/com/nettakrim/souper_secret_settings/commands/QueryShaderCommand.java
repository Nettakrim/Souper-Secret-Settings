package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.StackData;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class QueryShaderCommand implements Command<FabricClientCommandSource> {
	public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
		LiteralCommandNode<FabricClientCommandSource> queryNode = ClientCommandManager
			.literal("soup:query")
			.executes(new QueryShaderCommand())
			.build();

		return queryNode;
	}

	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ArrayList<StackData> postProcessorStack = SouperSecretSettingsClient.layer.postProcessorStack;
		if (postProcessorStack.size() == 0) {
			SouperSecretSettingsClient.say("query.none");
		} else {
			MutableText text = Text.empty();
			String lastId = postProcessorStack.get(0).data().id;
			int stacks = 0;
			boolean join = false;
			for (StackData stackData : postProcessorStack) {
				if (stackData.data().id.equals(lastId)) {
					stacks++;
				} else {
					text.append(get(lastId, stacks, join));
					lastId = stackData.data().id;
					stacks = 1;
					join = true;
				}
			}
			text.append(get(lastId, stacks, join));
			SouperSecretSettingsClient.say(text);
		}
        return 1;
	}

	private Text get(String id, int stacks, boolean join) {
		String key = ".query";
		if (stacks > 1) {
			key+=".multiple";
		}
		if (join) {
			key+=".join";
		}
		return Text.translatable(SouperSecretSettingsClient.MODID+key, id, stacks);
	}
}
