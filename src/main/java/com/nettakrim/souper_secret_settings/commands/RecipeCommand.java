package com.nettakrim.souper_secret_settings.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.MessageArgumentType;

import java.util.concurrent.CompletableFuture;

public class RecipeCommand {
    public static final SuggestionProvider<FabricClientCommandSource> recipes = (context, builder) -> {
        SouperSecretSettingsClient.recipeManager.getSuggestions(builder);
        return CompletableFuture.completedFuture(builder.build());
    };

    public static final SuggestionProvider<FabricClientCommandSource> recipesWithRandomAndClipboard = (context, builder) -> {
        if (SouperSecretSettingsClient.recipeManager.getSuggestions(builder)) {
            builder.suggest("random");
        }
        builder.suggest("clipboard");
        return CompletableFuture.completedFuture(builder.build());
    };

    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> recipeNode = ClientCommandManager
            .literal("soup:recipe")
            .build();

        LiteralCommandNode<FabricClientCommandSource> loadNode = ClientCommandManager
            .literal("load")
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                .suggests(recipesWithRandomAndClipboard)
                .then(
                    ClientCommandManager.literal("stack")
                    .executes(RecipeCommand::loadStack)
                )
                .executes(RecipeCommand::load)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> stackNode = ClientCommandManager
            .literal("stack")
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                .suggests(recipesWithRandomAndClipboard)
                .executes(RecipeCommand::loadStack)
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

        LiteralCommandNode<FabricClientCommandSource> shareNode = ClientCommandManager
            .literal("share")
            .then(
                ClientCommandManager.argument("name", StringArgumentType.string())
                .suggests(recipes)
                .executes(RecipeCommand::shareRecipe)
            )
            .executes(RecipeCommand::shareCurrent)
            .build();

        recipeNode.addChild(loadNode);
        recipeNode.addChild(stackNode);
        recipeNode.addChild(removeNode);
        recipeNode.addChild(saveNode);
        recipeNode.addChild(shareNode);
        return recipeNode;
    }

    private static int load(CommandContext<FabricClientCommandSource> context) {
        String data = SouperSecretSettingsCommands.getMessageText(context, "name");
        return load(data, false);
    }

    private static int loadStack(CommandContext<FabricClientCommandSource> context) {
        String data = SouperSecretSettingsCommands.getMessageText(context, "name");
        return load(data, true);
    }

    private static int load(String data, boolean stack) {
        if (data.equals("clipboard")) {
            String recipe = SouperSecretSettingsClient.client.keyboard.getClipboard();
            SouperSecretSettingsClient.recipeManager.loadFromRecipeData(recipe, stack);
        } else if (data.contains("/")) {
            SouperSecretSettingsClient.recipeManager.loadFromRecipeData(data, stack);
        } else {
            SouperSecretSettingsClient.recipeManager.loadFromName(data, stack);
        }
        return 1;
    }

    private static int remove(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        SouperSecretSettingsClient.recipeManager.removeRecipe(name);
        return 1;
    }

    private static int save(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        name = name.replace("/","");
        SouperSecretSettingsClient.recipeManager.setCurrentRecipe(name);
        return 1;
    }

    private static int shareRecipe(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "name");
        String recipe = SouperSecretSettingsClient.recipeManager.getRecipeData(name);
        return share("share.send", recipe, name);
    }

    private static int shareCurrent(CommandContext<FabricClientCommandSource> context) {
        String recipe = SouperSecretSettingsClient.recipeManager.getRecipeData(SouperSecretSettingsClient.layer.postProcessorStack);
        return share("share.send.current", recipe);
    }

    private static int share(String actionKey, String recipe, Object... formatting) {
        if (recipe.length() > 237) {
            SouperSecretSettingsClient.say("share.long");
            SouperSecretSettingsClient.client.keyboard.setClipboard(recipe);
            return 0;
        }
        SouperSecretSettingsClient.sayClickHere(
            actionKey,
            "soup:"+recipe+"|",
            false,
            formatting
        );
        return 1;
    }
}
