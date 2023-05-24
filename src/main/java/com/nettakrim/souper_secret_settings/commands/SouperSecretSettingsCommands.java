package com.nettakrim.souper_secret_settings.commands;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.ShaderData;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import com.nettakrim.souper_secret_settings.StackData;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class SouperSecretSettingsCommands {
    public static final SuggestionProvider<FabricClientCommandSource> shaders = (context, builder) -> {
        for (ShaderData shaderData : SouperSecretSettingsClient.shaderDatas) {
            builder.suggest(shaderData.id);
        }
        if (SouperSecretSettingsClient.shaderDatas.size() > 1) builder.suggest("random");
        return CompletableFuture.completedFuture(builder.build());
    };

    public static final SuggestionProvider<FabricClientCommandSource> activeShaders = (context, builder) -> {
        for (StackData stackData : SouperSecretSettingsClient.postProcessorStack) {
            builder.suggest(stackData.data().id);
        }
        return CompletableFuture.completedFuture(builder.build());
    };

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            RootCommandNode<FabricClientCommandSource> root = dispatcher.getRoot();

            root.addChild(ClearShaderCommand.getCommandNode());
            root.addChild(QueryShaderCommand.getCommandNode());
            root.addChild(RandomShaderCommand.getCommandNode());
            root.addChild(RecipeCommand.getCommandNode());
            root.addChild(SetShaderCommand.getCommandNode());
            root.addChild(StackShaderCommand.getCommandNode());
            root.addChild(ToggleShadersCommand.getCommandNode());
        });
    }
}
