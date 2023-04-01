package com.nettakrim.souper_secret_settings.commands;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.mixin.GameRendererAccessor;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.Identifier;

public class SouperSecretSettingsCommands {
    public static SuggestionProvider<FabricClientCommandSource> shaders = (context, builder) -> {
        for (Identifier identifier : GameRendererAccessor.getSuperSecretSettings()) {
            String id = identifier.toString();
            builder.suggest(id.substring(23, id.length()-5));
        }
        return CompletableFuture.completedFuture(builder.build());
    };

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            RootCommandNode<FabricClientCommandSource> root = dispatcher.getRoot();

            registerSetShaderNode(root);
            registerRandomShaderNode(root);
            registerClearShaderNode(root);
        });
    }

    public static void registerSetShaderNode(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
        .literal("soup:set")
        .then(
            ClientCommandManager.argument("shader", StringArgumentType.string())
            .suggests(shaders)
            .executes(new SetShaderCommand())
        )
        .build();

        root.addChild(setNode);
    }

    public static void registerRandomShaderNode(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> randomNode = ClientCommandManager
        .literal("soup:random")
        .executes(new RandomShaderCommand())
        .build();

        root.addChild(randomNode);
    }

    public static void registerClearShaderNode(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
        .literal("soup:clear")
        .executes(new ClearShaderCommand())
        .build();

        root.addChild(clearNode);
    }
}
