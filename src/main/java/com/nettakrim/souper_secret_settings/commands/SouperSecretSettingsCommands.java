package com.nettakrim.souper_secret_settings.commands;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.shaders.AbstractLayerEffect;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import com.nettakrim.souper_secret_settings.shaders.StackData;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.MessageArgumentType.MessageFormat;

public class SouperSecretSettingsCommands {
    public static final SuggestionProvider<FabricClientCommandSource> postShaders = (context, builder) -> {
        for (ShaderData shaderData : SouperSecretSettingsClient.postShaders) {
            builder.suggest(shaderData.id);
        }
        if (SouperSecretSettingsClient.postShaders.size() > 1) builder.suggest("random");
        return CompletableFuture.completedFuture(builder.build());
    };

    public static final SuggestionProvider<FabricClientCommandSource> activePostShaders = (context, builder) -> {
        for (StackData stackData : SouperSecretSettingsClient.layer.postProcessorStack) {
            builder.suggest(stackData.data().id);
        }
        return CompletableFuture.completedFuture(builder.build());
    };

    public static final SuggestionProvider<FabricClientCommandSource> layerEffects = (context, builder) -> {
        for (ShaderData shaderData : SouperSecretSettingsClient.layerEffects) {
            builder.suggest(shaderData.id);
        }
        if (SouperSecretSettingsClient.layerEffects.size() > 1) builder.suggest("random");
        return CompletableFuture.completedFuture(builder.build());
    };

    public static final SuggestionProvider<FabricClientCommandSource> activeLayerEffects = (context, builder) -> {
        for (AbstractLayerEffect abstractLayerEffect : SouperSecretSettingsClient.layer.layerEffects) {
            builder.suggest(abstractLayerEffect.shaderData.id);
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

    public static String getMessageText(CommandContext<FabricClientCommandSource> context, String name) {
        //a lot of digging through #SayCommand to make a MessageArgumentType that works clientside
        MessageFormat messageFormat = context.getArgument(name, MessageFormat.class);
        return messageFormat.getContents();
    }
}
