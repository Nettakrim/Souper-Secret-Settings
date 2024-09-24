package com.nettakrim.souper_secret_settings.commands;

import java.util.concurrent.CompletableFuture;

import com.mclegoman.luminance.client.shaders.ShaderDataloader;
import com.mclegoman.luminance.client.shaders.ShaderRegistry;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.RootCommandNode;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.MessageArgumentType.MessageFormat;

public class SouperSecretSettingsCommands {
    public static final SuggestionProvider<FabricClientCommandSource> postShaders = (context, builder) -> {
        for (ShaderRegistry shaderRegistry : ShaderDataloader.registry) {
            builder.suggest(shaderRegistry.getID().toString());
        }
        if (ShaderDataloader.getShaderAmount() > 1) builder.suggest("random");
        return CompletableFuture.completedFuture(builder.build());
    };

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            RootCommandNode<FabricClientCommandSource> root = dispatcher.getRoot();

            root.addChild(TestCommand.getCommandNode());
        });
    }

    public static String getMessageText(CommandContext<FabricClientCommandSource> context, String name) {
        //a lot of digging through #SayCommand to make a MessageArgumentType that works clientside
        MessageFormat messageFormat = context.getArgument(name, MessageFormat.class);
        return messageFormat.contents();
    }
}
