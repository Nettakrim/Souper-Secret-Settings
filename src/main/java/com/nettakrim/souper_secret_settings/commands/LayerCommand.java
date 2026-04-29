package com.nettakrim.souper_secret_settings.commands;

import com.google.gson.JsonElement;
import dev.dannytaylor.luminance.client.data.ClientData;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.serialization.JsonOps;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.*;
import com.nettakrim.souper_secret_settings.data.LayerCodecs;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import java.util.List;
import java.util.Optional;

public class LayerCommand extends ListCommand<ShaderLayer> {
    String saveConfirm = null;
    String deleteConfirm = null;

    public LayerCommand() {

    }

    public void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:layer").build();
        root.addChild(commandNode);

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
                .literal("add")
                .then(
                        ClientCommandManager.argument("name", StringArgumentType.string())
                                .suggests(files)
                                .executes(context -> add(StringArgumentType.getString(context, "name"), -1))
                                .then(
                                        ClientCommandManager.argument("position", IntegerArgumentType.integer(-1))
                                                .executes(context -> add(StringArgumentType.getString(context, "name"), IntegerArgumentType.getInteger(context, "position")))
                                )
                )
                .build();
        commandNode.addChild(addNode);

        LiteralCommandNode<FabricClientCommandSource> activeNode = ClientCommandManager
                .literal("active")
                .then(
                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                .suggests(layerIndexes)
                                .executes(context -> setActive(IntegerArgumentType.getInteger(context, "index")))
                )
                .executes(context -> queryActive())
                .build();
        commandNode.addChild(activeNode);

        LiteralCommandNode<FabricClientCommandSource> nameNode = ClientCommandManager
                .literal("name")
                .then(
                        ClientCommandManager.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> builder.suggest(SouperSecretSettingsClient.soupRenderer.activeLayer.name).buildFuture())
                                .executes(context -> setName(StringArgumentType.getString(context, "name")))
                )
                .executes(context -> queryName())
                .build();
        commandNode.addChild(nameNode);

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
                .literal("toggle")
                .executes(context -> toggle())
                .build();
        commandNode.addChild(toggleNode);

        LiteralCommandNode<FabricClientCommandSource> saveNode = ClientCommandManager
                .literal("save")
                .then(
                        ClientCommandManager.literal("create").then(
                                ClientCommandManager.argument("name", StringArgumentType.string())
                                        .suggests(files)
                                        .executes(context -> save(StringArgumentType.getString(context, "name"), false))
                                        .then(
                                                ClientCommandManager.literal("force")
                                                        .executes(context -> save(StringArgumentType.getString(context, "name"), true))
                                        )
                        )
                )
                .then(
                        ClientCommandManager.literal("load").then(
                                ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                        .suggests(layers)
                                        .executes(context -> load(StringArgumentType.getString(context, "name")))
                        )
                )
                .then(
                        ClientCommandManager.literal("remove").then(
                                ClientCommandManager.argument("name", StringArgumentType.string())
                                        .suggests(files)
                                        .executes(context -> delete(StringArgumentType.getString(context, "name"), false))
                                        .then(
                                                ClientCommandManager.literal("force")
                                                        .executes(context -> delete(StringArgumentType.getString(context, "name"), true))
                                        )
                        )
                )
                .build();
        commandNode.addChild(saveNode);


        LiteralCommandNode<FabricClientCommandSource> infoNode = ClientCommandManager
                .literal("info")
                .executes(context -> info())
                .build();
        commandNode.addChild(infoNode);

        LiteralCommandNode<FabricClientCommandSource> copyNode = ClientCommandManager
                .literal("copy")
                .executes(context -> copyCurrent(-1))
                .then(
                        ClientCommandManager.literal("current")
                                .then(
                                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                                .suggests(layerIndexes)
                                                .executes(context -> copyCurrent(IntegerArgumentType.getInteger(context, "index")))
                                )
                                .executes(context -> copyCurrent(-1))
                )
                .then(
                        ClientCommandManager.literal("saved")
                                .then(
                                        ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                                .suggests(layers)
                                                .executes(context -> copyFile(StringArgumentType.getString(context, "name")))
                                )
                )
                .then(
                        ClientCommandManager.literal("load")
                                .executes(context -> loadFromString(ClientData.minecraft.keyboardHandler.getClipboard()))
                                .then(
                                        ClientCommandManager.literal("clipboard")
                                                .executes(context -> loadFromString(ClientData.minecraft.keyboardHandler.getClipboard()))
                                )
                                .then(
                                        ClientCommandManager.literal("text")
                                                .then(
                                                        ClientCommandManager.argument("value", StringArgumentType.greedyString())
                                                                .executes(context -> loadFromString(StringArgumentType.getString(context, "value")))
                                                )
                                )
                )
                .build();
        commandNode.addChild(copyNode);

        registerList(commandNode);
    }

    private int add(String name, int position) {
        if (position < 0 || position > SouperSecretSettingsClient.soupRenderer.shaderLayers.size()) {
            position = SouperSecretSettingsClient.soupRenderer.shaderLayers.size();
        }

        ShaderLayer layer = new ShaderLayer(name);
        new ListAddAction<>(SouperSecretSettingsClient.soupRenderer.shaderLayers, layer, position).addToHistory();
        SouperSecretSettingsClient.soupRenderer.shaderLayers.add(position, layer);

        SouperSecretSettingsClient.soupRenderer.activeLayer = layer;
        return 1;
    }

    @Override
    protected void onRemove() {
        if (SouperSecretSettingsClient.soupRenderer.shaderLayers.isEmpty()) {
            SouperSecretSettingsClient.soupRenderer.clearAll();
            SouperSecretSettingsClient.soupRenderer.loadDefault();
            new LayerClearAction().addToHistory();
        } else if (!SouperSecretSettingsClient.soupRenderer.shaderLayers.contains(SouperSecretSettingsClient.soupRenderer.activeLayer)) {
            SouperSecretSettingsClient.soupRenderer.activeLayer = SouperSecretSettingsClient.soupRenderer.shaderLayers.getLast();
        }
        SouperSecretSettingsClient.soupGui.updateActiveLayerMessageOrScreen();
    }

    private int setActive(int index) {
        if (index < SouperSecretSettingsClient.soupRenderer.shaderLayers.size()) {
            ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.shaderLayers.get(index);
            SouperSecretSettingsClient.soupRenderer.activeLayer = layer;
            SouperSecretSettingsClient.say("layer.active.set", 0, layer.name);
            return 1;
        }
        SouperSecretSettingsClient.say("layer.error.index", 1, index, SouperSecretSettingsClient.soupRenderer.shaderLayers.size()-1);
        return 0;
    }

    private int queryActive() {
        SouperSecretSettingsClient.say("layer.active.query", 1, SouperSecretSettingsClient.soupRenderer.activeLayer.name);
        return 1;
    }

    private int setName(String name) {
        new LayerRenameAction(SouperSecretSettingsClient.soupRenderer.activeLayer).addToHistory();
        SouperSecretSettingsClient.soupRenderer.activeLayer.name = name;
        SouperSecretSettingsClient.soupRenderer.activeLayer.disambiguateName();
        SouperSecretSettingsClient.say("layer.name.set", 0, SouperSecretSettingsClient.soupRenderer.activeLayer.name);
        return 1;
    }

    private int queryName() {
        SouperSecretSettingsClient.say("layer.name.query", 1, SouperSecretSettingsClient.soupRenderer.activeLayer.name);
        return 1;
    }

    private int toggle() {
        new ToggleAction(SouperSecretSettingsClient.soupRenderer.activeLayer).addToHistory();
        SouperSecretSettingsClient.soupRenderer.activeLayer.toggle();
        return 1;
    }

    private int save(String name, boolean force) {
        if (!SouperSecretSettingsClient.soupData.isValidName(name)) {
            SouperSecretSettingsClient.say("layer.save.invalid", 1, name);
            return 0;
        }
        if (force || name.equals(saveConfirm) || !SouperSecretSettingsClient.soupData.savedLayerExists(name)) {
            ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;
            String nameTemp = layer.name;
            layer.name = name;

            SouperSecretSettingsClient.soupData.saveLayer(layer, () -> SouperSecretSettingsClient.say("layer.save", 1, name));

            layer.name = nameTemp;
            saveConfirm = null;
        } else {
            SouperSecretSettingsClient.say("layer.save.prompt", 1, name);
            saveConfirm = name;
        }
        deleteConfirm = null;
        return 1;
    }

    private int delete(String name, boolean force) {
        if (SouperSecretSettingsClient.soupData.savedLayerExists(name)) {
            if (force || name.equals(deleteConfirm)) {
                SouperSecretSettingsClient.soupData.deleteSavedLayer(name);
                SouperSecretSettingsClient.say("layer.delete", 1, name);
                deleteConfirm = null;
            } else {
                SouperSecretSettingsClient.say("layer.delete.prompt", 1, name);
                deleteConfirm = name;
            }
        } else {
            SouperSecretSettingsClient.say("layer.missing", 1, name);
        }
        saveConfirm = null;
        return 1;
    }

    private int load(String name) {
        saveConfirm = null;
        deleteConfirm = null;

        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;
        String nameTemp = layer.name;
        layer.name = name;

        new ShaderLoadAction(layer, layer.name).addToHistory();
        layer.clear();
        if (!SouperSecretSettingsClient.soupData.loadLayer(layer)) {
            SouperSecretSettingsClient.say("layer.missing", 1, name);
        }

        layer.name = nameTemp;
        return 1;
    }

    private int info() {
        SouperSecretSettingsClient.sayStyled(Component.translatable(SouperSecretSettingsClient.MODID+".layer.info.name", SouperSecretSettingsClient.soupRenderer.activeLayer.name), 1);
        for (MutableComponent text : SouperSecretSettingsClient.soupRenderer.activeLayer.getInfo()) {
            SouperSecretSettingsClient.sayRaw(text.setStyle(Style.EMPTY.withColor(SouperSecretSettingsClient.textColor)), 1);
        }
        return 1;
    }

    private int copyFile(String name) {
        return copyCodec(SouperSecretSettingsClient.soupData.getLayerCodec(name), name, "layer.copy_saved");
    }

    private int copyCurrent(int index) {
        if (index < SouperSecretSettingsClient.soupRenderer.shaderLayers.size()) {
            ShaderLayer layer = index < 0 ? SouperSecretSettingsClient.soupRenderer.activeLayer : SouperSecretSettingsClient.soupRenderer.shaderLayers.get(index);
            return copyCodec(LayerCodecs.from(layer), index < 0 ? layer.name : String.valueOf(index), index < 0 ? "layer.copy_active" : "layer.copy");
        }
        SouperSecretSettingsClient.say("layer.error.index", 1, index, SouperSecretSettingsClient.soupRenderer.shaderLayers.size()-1);
        return 0;
    }

    private int copyCodec(LayerCodecs layerCodec, String name, String key) {
        String text = LayerCodecs.CODEC.encodeStart(JsonOps.INSTANCE, layerCodec).getOrThrow().toString();
        ClientData.minecraft.keyboardHandler.setClipboard(text);
        SouperSecretSettingsClient.say(key, 0, name, text.length());
        return 1;
    }

    private int loadFromString(String value) {
        Optional<LayerCodecs> layerCodecs = Optional.empty();
        try {
            layerCodecs = LayerCodecs.CODEC.parse(JsonOps.INSTANCE, SouperSecretSettingsClient.soupData.gson.fromJson(value, JsonElement.class)).result();
        } catch (Exception ignored) {}

        if (layerCodecs.isEmpty()) {
            SouperSecretSettingsClient.say("layer.error.data", 1);
            return 0;
        }

        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;
        new ShaderLoadAction(layer, value).addToHistory();
        layer.clear();
        layerCodecs.get().apply(layer);
        return 1;
    }

    private static final SuggestionProvider<FabricClientCommandSource> files = getSavedLayerSuggestion(false);

    private static final SuggestionProvider<FabricClientCommandSource> layers = getSavedLayerSuggestion(true);

    private static SuggestionProvider<FabricClientCommandSource> getSavedLayerSuggestion(boolean includeResources) {
        return (context, builder) -> {
            for (String name : SouperSecretSettingsClient.soupData.getSavedLayers(includeResources)) {
                builder.suggest(name);
            }

            return builder.buildFuture();
        };
    }

    private static final SuggestionProvider<FabricClientCommandSource> layerIndexes = SouperSecretSettingsCommands.createIndexSuggestion(
            (context) -> SouperSecretSettingsClient.soupRenderer.shaderLayers,
            (message) -> Component.literal(message.name)
    );

    @Override
    List<ShaderLayer> getList() {
        return SouperSecretSettingsClient.soupRenderer.shaderLayers;
    }

    @Override
    String getID(ShaderLayer value) {
        return value.name;
    }

    @Override
    SuggestionProvider<FabricClientCommandSource> getIndexSuggestions() {
        return layerIndexes;
    }
}
