package com.nettakrim.souper_secret_settings.commands;

import dev.dannytaylor.luminance.client.shaders.RenderLocations;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.Actions;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public class OptionCommand {
    public static void register(RootCommandNode<FabricClientCommandSource> root, CommandBuildContext commandRegistryAccess) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:option").build();
        root.addChild(commandNode);

        LiteralCommandNode<FabricClientCommandSource> eatingNode = ClientCommandManager
                .literal("eating")
                .build();
        commandNode.addChild(eatingNode);

        LiteralCommandNode<FabricClientCommandSource> randomNode = ClientCommandManager
                .literal("item_random")
                .then(
                        ClientCommandManager.argument("item", ItemArgument.item(commandRegistryAccess))
                                .executes(context -> setRandomItem(ItemArgument.getItem(context, "item")))
                )
                .executes(context -> queryRandomItem())
                .build();
        eatingNode.addChild(randomNode);

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
                .literal("item_clear")
                .then(
                        ClientCommandManager.argument("item", ItemArgument.item(commandRegistryAccess))
                                .executes(context -> setClearItem(ItemArgument.getItem(context, "item")))
                )
                .executes(context -> queryClearItem())
                .build();
        eatingNode.addChild(clearNode);

        LiteralCommandNode<FabricClientCommandSource> randomShaderNode = ClientCommandManager
                .literal("shader")
                .then(
                        ClientCommandManager.argument("shader", StringArgumentType.string())
                                .suggests(SouperSecretSettingsCommands.shaderCommand.registrySuggestions)
                                .executes(context -> setRandomShader(StringArgumentType.getString(context, "shader")))
                )
                .executes(context -> queryRandomInfo(1))
                .build();
        eatingNode.addChild(randomShaderNode);

        LiteralCommandNode<FabricClientCommandSource> randomCountNode = ClientCommandManager
                .literal("count")
                .then(
                        ClientCommandManager.argument("amount", IntegerArgumentType.integer(1, 256))
                                .executes(context -> setRandomCount(IntegerArgumentType.getInteger(context, "amount")))
                )
                .executes(context -> queryRandomInfo(1))
                .build();
        eatingNode.addChild(randomCountNode);

        LiteralCommandNode<FabricClientCommandSource> randomDurationNode = ClientCommandManager
                .literal("duration")
                .then(
                        ClientCommandManager.argument("duration", TimeArgument.time())
                                .suggests(durationSuggestion)
                                .executes(context -> setRandomDuration(IntegerArgumentType.getInteger(context, "duration")))
                )
                .executes(context -> queryRandomInfo(1))
                .build();
        eatingNode.addChild(randomDurationNode);

        LiteralCommandNode<FabricClientCommandSource> randomSoundNode = ClientCommandManager
                .literal("sound")
                .then(
                        ClientCommandManager.literal("enable")
                                .executes(context -> setRandomSound(true))
                )
                .then(
                        ClientCommandManager.literal("disable")
                                .executes(context -> setRandomSound(false))
                )
                .executes(context -> queryRandomSound(1))
                .build();
        eatingNode.addChild(randomSoundNode);

        LiteralCommandNode<FabricClientCommandSource> renderLocationNode = ClientCommandManager
                .literal("render_location")
                .then(
                        // the location is called "game", but i prefer the word "world"
                        ClientCommandManager.literal("world").executes(context -> setRenderLocation(RenderLocations.GAME))
                )
                .then(
                        ClientCommandManager.literal("ui").executes(context -> setRenderLocation(RenderLocations.UI))
                )
                .executes(context -> queryRenderLocation(1))
                .build();
        commandNode.addChild(renderLocationNode);

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
                .literal("toggle")
                .then(
                        ClientCommandManager.literal("stay")
                                .executes(context -> toggle(true))
                )
                .executes(context -> toggle(false))
                .build();
        commandNode.addChild(toggleNode);

        LiteralCommandNode<FabricClientCommandSource> warningNode = ClientCommandManager
                .literal("warning")
                .then(
                        ClientCommandManager.literal("enable")
                                .executes(context -> setWarning(true))
                )
                .then(
                        ClientCommandManager.literal("disable")
                                .executes(context -> setWarning(false))
                )
                .executes(context -> queryWarning(1))
                .build();
        commandNode.addChild(warningNode);

        LiteralCommandNode<FabricClientCommandSource> filterNode = ClientCommandManager
                .literal("message_filter")
                .then(
                        ClientCommandManager.literal("all")
                                .executes(context -> setMessageFilter(0))
                )
                .then(
                        ClientCommandManager.literal("important")
                                .executes(context -> setMessageFilter(1))
                )
                .then(
                        ClientCommandManager.literal("none")
                                .executes(context -> setMessageFilter(2))
                )
                .executes(context -> queryMessageFilter(1))
                .build();
        commandNode.addChild(filterNode);

        LiteralCommandNode<FabricClientCommandSource> undoLimitNode = ClientCommandManager
                .literal("undo_limit")
                .then(
                        ClientCommandManager.argument("amount", IntegerArgumentType.integer(16))
                                .suggests(((context, builder) -> {
                                    builder.suggest(Actions.defaultLength);
                                    return builder.buildFuture();
                                }))
                                .executes(context -> setUndoLimit(IntegerArgumentType.getInteger(context, "amount")))
                )
                .executes(context -> queryUndoLimit(1))
                .build();
        commandNode.addChild(undoLimitNode);
    }

    public static int setRandomItem(ItemInput itemStack) throws CommandSyntaxException {
        SouperSecretSettingsClient.soupData.config.randomItem = itemStack.createItemStack(1, false);
        sayItem("option.random.set", SouperSecretSettingsClient.soupData.config.randomItem, 0);
        SouperSecretSettingsClient.soupData.changeData(false);
        return 1;
    }

    public static int queryRandomItem() {
        sayItem("option.random.query", SouperSecretSettingsClient.soupData.config.randomItem, 1);
        return 1;
    }

    public static int setClearItem(ItemInput itemStack) throws CommandSyntaxException {
        SouperSecretSettingsClient.soupData.config.clearItem = itemStack.createItemStack(1, false);
        sayItem("option.clear.set", SouperSecretSettingsClient.soupData.config.clearItem, 0);
        SouperSecretSettingsClient.soupData.changeData(false);
        return 1;
    }

    public static int queryClearItem() {
        sayItem("option.clear.query", SouperSecretSettingsClient.soupData.config.clearItem, 1);
        return 1;
    }

    public static int setRandomShader(String shader) {
        SouperSecretSettingsClient.soupData.config.randomShader = shader;
        SouperSecretSettingsClient.soupData.changeData(false);
        return queryRandomInfo(0);
    }

    public static int setRandomCount(int amount) {
        SouperSecretSettingsClient.soupData.config.randomCount = amount;
        SouperSecretSettingsClient.soupData.changeData(false);
        return queryRandomInfo(0);
    }

    public static int setRandomDuration(int amount) {
        SouperSecretSettingsClient.soupData.config.randomDuration = amount;
        SouperSecretSettingsClient.soupData.changeData(false);
        return queryRandomInfo(0);
    }

    public static int setRandomSound(boolean state) {
        SouperSecretSettingsClient.soupData.config.randomSound = state;
        SouperSecretSettingsClient.soupData.changeData(false);
        return queryRandomSound(0);
    }

    public static int queryRandomSound(int priority) {
        SouperSecretSettingsClient.say("option.sound."+(SouperSecretSettingsClient.soupData.config.randomSound ? "on" : "off") + (priority > 0 ? ".query" : ".set"), priority);
        return 1;
    }


    public static int queryRandomInfo(int priority) {
        int time = SouperSecretSettingsClient.soupData.config.randomDuration;
        SouperSecretSettingsClient.say("option.random_info" + (SouperSecretSettingsClient.soupData.config.randomDuration == 0 ? "" : "_duration") + (priority > 0 ? ".query" : ".set"), priority, SouperSecretSettingsClient.soupData.config.randomShader, SouperSecretSettingsClient.soupData.config.randomCount, (time/20) + (time%20 == 0 ? "" : String.valueOf((time%20)/20f).substring(1)));
        return 1;
    }

    public static void sayItem(String key, ItemStack itemStack, int priority) {
        String s = itemStack.getItem().toString();
        if (!itemStack.getComponentsPatch().isEmpty()) {
            s += " "+itemStack.getComponentsPatch();
        }
        SouperSecretSettingsClient.say(key, priority, s);
    }

    public static int setRenderLocation(RenderLocations.RenderLocation<?> renderLocation) {
        SouperSecretSettingsClient.soupRenderer.setRenderLocation(renderLocation);
        return queryRenderLocation(0);
    }

    public static int queryRenderLocation(int priority) {
        SouperSecretSettingsClient.say("option.render_type."+SouperSecretSettingsClient.soupRenderer.getRenderLocation().identifier().toString().replace(':','.') + (priority > 0 ? ".query" : ".set"), priority);
        return 1;
    }

    public static int toggle(boolean stay) {
        SouperSecretSettingsClient.soupData.config.disableState = stay ? 2 : SouperSecretSettingsClient.soupData.config.disableState > 0 ? 0 : 1;
        SouperSecretSettingsClient.say("option.toggle."+SouperSecretSettingsClient.soupData.config.disableState, 0);
        SouperSecretSettingsClient.soupData.changeData(false);
        return 1;
    }

    public static int setWarning(boolean state) {
        SouperSecretSettingsClient.soupData.config.warning = state;
        SouperSecretSettingsClient.soupData.changeData(false);
        return queryWarning(0);
    }

    public static int queryWarning(int priority) {
        SouperSecretSettingsClient.say("option.warning."+(SouperSecretSettingsClient.soupData.config.warning ? "on" : "off") + (priority > 0 ? ".query" : ".set"), priority);
        return 1;
    }

    public static int setMessageFilter(int to) {
        SouperSecretSettingsClient.soupData.config.messageFilter = to;
        SouperSecretSettingsClient.soupData.changeData(false);
        return queryMessageFilter(2);
    }

    public static int queryMessageFilter(int priority) {
        SouperSecretSettingsClient.say("option.filter."+SouperSecretSettingsClient.soupData.config.messageFilter + (priority > 0 ? ".query" : ".set"), priority);
        return 1;
    }

    public static int setUndoLimit(int to) {
        SouperSecretSettingsClient.soupData.config.undoLimit = to;
        SouperSecretSettingsClient.soupData.changeData(false);
        return queryUndoLimit(0);
    }

    public static int queryUndoLimit(int priority) {
        SouperSecretSettingsClient.say("option.undo_limit" + (priority > 0 ? ".query" : ".set"), priority, SouperSecretSettingsClient.soupData.config.undoLimit);
        return 1;
    }


    private static final SuggestionProvider<FabricClientCommandSource> durationSuggestion = (context, builder) -> {
        StringReader stringReader = new StringReader(builder.getRemaining());

        try {
            stringReader.readFloat();
        } catch (CommandSyntaxException var5) {
            builder.suggest("0t");
            return builder.buildFuture();
        }

        return SharedSuggestionProvider.suggest(List.of("t","s","d"), builder.createOffset(builder.getStart() + stringReader.getCursor()));
    };
}
