package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.concurrent.CompletableFuture;

public class RecipeCommand {
    public static final SuggestionProvider<FabricClientCommandSource> recipes = (context, builder) -> {
        SouperSecretSettingsClient.recipeManager.getSuggestions(builder);
        return CompletableFuture.completedFuture(builder.build());
    };

    public static final SuggestionProvider<FabricClientCommandSource> recipesWithRandom = (context, builder) -> {
        if (SouperSecretSettingsClient.recipeManager.getSuggestions(builder)) {
            builder.suggest("random");
        }
        return CompletableFuture.completedFuture(builder.build());
    };

    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> recipeNode = ClientCommandManager
            .literal("soup:recipe")
            .build();

        LiteralCommandNode<FabricClientCommandSource> loadNode = ClientCommandManager
            .literal("load")
            .then(
                ClientCommandManager.argument("name", StringArgumentType.string())
                .suggests(recipesWithRandom)
                .then(
                    ClientCommandManager.literal("stack")
                    .executes(RecipeCommand::loadStack)
                )
                .executes(RecipeCommand::load)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
            .literal("remove")
            .then(
                ClientCommandManager.argument("name", StringArgumentType.string())
                .suggests(recipes)
                .executes(RecipeCommand::remove)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> saveNode = ClientCommandManager
            .literal("save")
            .then(
                ClientCommandManager.argument("name", StringArgumentType.string())
                .executes(RecipeCommand::save)
            )
            .build();

        recipeNode.addChild(loadNode);
        recipeNode.addChild(removeNode);
        recipeNode.addChild(saveNode);
        return recipeNode;
    }

    private static int load(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        SouperSecretSettingsClient.recipeManager.loadFromName(name, false);
        return 1;
    }

    private static int loadStack(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        SouperSecretSettingsClient.recipeManager.loadFromName(name, true);
        return 1;
    }

    private static int remove(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        SouperSecretSettingsClient.recipeManager.removeRecipe(name);
        return 1;
    }

    private static int save(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        SouperSecretSettingsClient.recipeManager.setCurrentRecipe(name);
        return 1;
    }
}
