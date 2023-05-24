package com.nettakrim.souper_secret_settings.commands;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.ShaderData;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import com.nettakrim.souper_secret_settings.StackData;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class SouperSecretSettingsCommands {
    public static final SuggestionProvider<FabricClientCommandSource> shaders = (context, builder) -> {
        for (ShaderData shaderData : SouperSecretSettingsClient.shaderDatas) {
            builder.suggest(shaderData.id);
        }
        builder.suggest("random");
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

            registerSetShaderNode(root);
            registerRandomShaderNode(root);
            registerClearShaderNode(root);
            registerQueryShaderNode(root);
            registerStackShaderNode(root);
            registerToggleShadersNode(root);
        });
    }

    public static void registerSetShaderNode(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
        .literal("soup:set")
        .then(
            ClientCommandManager.argument("shader", StringArgumentType.string())
            .suggests(shaders)
            .executes(new SetShaderCommand())
            .then(
                ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                .executes(SetShaderCommand::setMultiple)
            )
        )
        .build();

        root.addChild(setNode);
    }

    public static void registerRandomShaderNode(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> randomNode = ClientCommandManager
        .literal("soup:random")
        .executes(new RandomShaderCommand())
        .then(
            ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
            .executes(RandomShaderCommand::randomMultiple)
        )
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

    public static void registerQueryShaderNode(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> queryNode = ClientCommandManager
        .literal("soup:query")
        .executes(new QueryShaderCommand())
        .build();

        root.addChild(queryNode);
    }

    public static void registerStackShaderNode(RootCommandNode<FabricClientCommandSource> root) {
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
        root.addChild(stackNode);
    }

    public static void registerToggleShadersNode(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
        .literal("soup:toggle")
        .executes(new ToggleShadersCommand())
        .build();

        root.addChild(toggleNode);
    }
}
