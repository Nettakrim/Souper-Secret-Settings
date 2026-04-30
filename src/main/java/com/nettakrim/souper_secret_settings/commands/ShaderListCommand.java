package com.nettakrim.souper_secret_settings.commands;

import dev.dannytaylor.luminance.client.shaders.ShaderRegistryEntry;
import dev.dannytaylor.luminance.client.shaders.Shaders;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostPassInterface;
import dev.dannytaylor.luminance.client.shaders.overrides.OverrideSource;
import dev.dannytaylor.luminance.client.shaders.uniforms.config.MapConfig;
import dev.dannytaylor.luminance.client.shaders.uniforms.config.UniformConfig;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.ListAddAction;
import com.nettakrim.souper_secret_settings.actions.ToggleAction;
import com.nettakrim.souper_secret_settings.actions.UniformChangeAction;
import com.nettakrim.souper_secret_settings.shaders.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShaderListCommand extends ListCommand<ShaderData> {
    protected final String name;
    protected final Identifier registry;
    protected final int warnLimit;
    protected final SuggestionProvider<FabricClientCommandSource> registrySuggestions;

    protected boolean warned;

    public ShaderListCommand(String name, Identifier registry, int warnLimit) {
        this.name = name;
        this.registry = registry;
        this.warnLimit = warnLimit;

        this.registrySuggestions = getRegistrySuggestions(registry, false);
    }

    public void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:"+name).build();
        root.addChild(commandNode);

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
                .literal("add")
                .then(
                        ClientCommandManager.argument("shader", IdentifierArgument.id())
                                .suggests(registrySuggestions)
                                .executes((context -> add(context.getArgument("shader", Identifier.class), 1, -1, false)))
                                .then(
                                        ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
                                                .executes((context -> add(context.getArgument("shader", Identifier.class), IntegerArgumentType.getInteger(context, "amount"), -1, false)))
                                                .then(
                                                        ClientCommandManager.argument("position", IntegerArgumentType.integer(-1))
                                                                .executes((context -> add(context.getArgument("shader", Identifier.class), IntegerArgumentType.getInteger(context, "amount"), IntegerArgumentType.getInteger(context, "position"), false)))
                                                                .then(
                                                                        ClientCommandManager.literal("force")
                                                                                .executes((context -> add(context.getArgument("shader", Identifier.class), IntegerArgumentType.getInteger(context, "amount"), IntegerArgumentType.getInteger(context, "position"), true)))
                                                                )
                                                )
                                )
                )
                .build();
        commandNode.addChild(addNode);

        LiteralCommandNode<FabricClientCommandSource> modifyNode = ClientCommandManager
                .literal("modify")
                .then(
                        ClientCommandManager.argument("shader", IntegerArgumentType.integer(0))
                                .suggests(shaderSuggestions)
                                .then(
                                        ClientCommandManager.literal("toggle")
                                                .executes(this::toggle)
                                )
                                .then(
                                        ClientCommandManager
                                                .literal("parameter")
                                                .then(
                                                        ClientCommandManager.argument("pass", IntegerArgumentType.integer(0))
                                                                .suggests(passSuggestions)
                                                                .then(
                                                                        ClientCommandManager.argument("block", StringArgumentType.string())
                                                                                .suggests(uniformBlockSuggestions)
                                                                                .then(
                                                                                        ClientCommandManager.argument("uniform", IntegerArgumentType.integer(0))
                                                                                                .suggests(uniformSuggestions)
                                                                                                .then(
                                                                                                        ClientCommandManager.argument("name", StringArgumentType.string())
                                                                                                                .suggests(uniformNameSuggestions)
                                                                                                                .then(
                                                                                                                        ClientCommandManager.argument("value", MessageArgument.message())
                                                                                                                                .suggests(uniformValueSuggestions)
                                                                                                                                .executes(this::setValue)
                                                                                                                )
                                                                                                )
                                                                                )
                                                                )
                                                )
                                )
                )
                .build();
        commandNode.addChild(modifyNode);

        LiteralCommandNode<FabricClientCommandSource> infoNode = ClientCommandManager
                .literal("info")
                .executes((context) -> info())
                .build();
        commandNode.addChild(infoNode);

        LiteralCommandNode<FabricClientCommandSource> groupNode = ClientCommandManager
                .literal("group")
                .then(
                        ClientCommandManager.literal("create")
                                .executes((context -> createGroup(Group.getNextName(SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry)))))
                                .then(
                                        ClientCommandManager.argument("name", StringArgumentType.string())
                                                .executes(context -> createGroup(StringArgumentType.getString(context, "name")))
                                )
                )
                .then(
                        ClientCommandManager.literal("modify")
                                .then(
                                        ClientCommandManager.argument("name", IdentifierArgument.id())
                                                .suggests(groupSuggestions)
                                                .then(
                                                        ClientCommandManager.literal("add")
                                                                .then(
                                                                        ClientCommandManager.argument("value", IdentifierArgument.id())
                                                                                .suggests(getRegistrySuggestions(registry, true))
                                                                                .executes(context -> addGroupEntry(context.getArgument("name", Identifier.class).getPath(), context.getArgument("value", Identifier.class), -1))
                                                                                .then(
                                                                                        ClientCommandManager.argument("position", IntegerArgumentType.integer(-1))
                                                                                                .executes(context -> addGroupEntry(context.getArgument("name", Identifier.class).getPath(), context.getArgument("value", Identifier.class), IntegerArgumentType.getInteger(context, "position")))
                                                                                )
                                                                )
                                                )
                                                .then(
                                                        ClientCommandManager.literal("remove")
                                                                .then(
                                                                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                                                                .suggests(groupIndexes)
                                                                                .executes(context -> removeGroupEntry(context.getArgument("name", Identifier.class).getPath(), IntegerArgumentType.getInteger(context, "index")))
                                                                )
                                                )
                                                .then(
                                                        ClientCommandManager.literal("toggle")
                                                                .then(
                                                                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                                                                .suggests(groupIndexes)
                                                                                .executes(context -> toggleGroupEntry(context.getArgument("name", Identifier.class).getPath(), IntegerArgumentType.getInteger(context, "index")))
                                                                )
                                                )
                                )
                )
                .then(
                        ClientCommandManager.literal("remove")
                                .then(
                                        ClientCommandManager.argument("name", StringArgumentType.string())
                                                .suggests(userGroupSuggestions)
                                                .executes((context -> removeGroup(StringArgumentType.getString(context, "name"))))
                                )
                )
                .build();
        commandNode.addChild(groupNode);

        registerList(commandNode);
    }

    public int add(Identifier id, int amount, int position, boolean force) {
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        List<ShaderData> shaders = SouperSecretSettingsClient.soupRenderer.getShaderAdditions(layer, registry, id, amount, position, true);
        if (shaders == null) {
            return 0;
        }

        List<ShaderData> list = layer.getList(registry);
        if (list.size()+amount > warnLimit) {
            if (!warned && SouperSecretSettingsClient.soupData.config.warning && !force) {
                warned = true;
                SouperSecretSettingsClient.say("shader.warn_stacking", warnLimit);
                return 1;
            }
        } else {
            warned = false;
        }

        if (position < 0 || position > list.size()) {
            position = list.size();
        }

        for (ShaderData shaderData : shaders) {
            new ListAddAction<>(list, shaderData, position).addToHistory();
            list.add(position, shaderData);
            position++;
        }
        return 1;
    }

    @Override
    protected void onRemove() {
        if (getList().size() < warnLimit) {
            warned = false;
        }
    }

    public int toggle(CommandContext<FabricClientCommandSource> context) {
        int shaderIndex = IntegerArgumentType.getInteger(context, "shader");
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        List<ShaderData> shaders = layer.getList(registry);
        if (shaderIndex >= shaders.size()) {
            return 0;
        }

        ShaderData shaderData = shaders.get(shaderIndex);
        new ToggleAction(shaderData).addToHistory();
        shaderData.toggle();

        return 1;
    }


    public int setValue(CommandContext<FabricClientCommandSource> context) {
        UniformData uniform = getUniformData(context, true);
        if (uniform == null) {
            return 0;
        }

        String name = StringArgumentType.getString(context, "name");
        int breakIndex = name.lastIndexOf('.');
        if (breakIndex <= 0) {
            SouperSecretSettingsClient.say("shader.error.name", 1, name);
            return 0;
        }

        int index = -1;
        try {
            index = Integer.parseInt(name.substring(breakIndex+1));
        } catch (Exception ignored) {}

        if (index < 0) {
            SouperSecretSettingsClient.say("shader.error.name", 1, name);
            return 0;
        }

        String text = name.substring(0, breakIndex);
        String value = context.getArgument("value", MessageArgument.Message.class).text();

        if (text.equals("value")) {
            if (index >= uniform.override.overrideSources.size()) {
                SouperSecretSettingsClient.say("shader.error.value", 1, index, uniform.override.overrideSources.size()-1);
                return 0;
            }

            new UniformChangeAction(IntegerArgumentType.getInteger(context, "uniform"), index, uniform.override, uniform.config).addToHistory();

            OverrideSource source = ParameterOverrideSource.parameterSourceFromString(value);
            uniform.override.overrideSources.set(index, source);

            String prefix = index+"_";
            MapConfig mapConfig = new MapConfig(Map.of());
            UniformConfig templateConfig = source.getTemplateConfig();
            for (String templateName : templateConfig.getNames()) {
                List<Object> objects = templateConfig.getObjects(templateName);
                if (objects != null) {
                    mapConfig.config().put(prefix + templateName, new ArrayList<>(objects));
                }
            }
            uniform.config.config().keySet().removeIf((s) -> s.startsWith(prefix) && !mapConfig.config().containsKey(s));
            uniform.config.mergeWithConfig(mapConfig);
        } else {
            List<Object> values = uniform.config.getObjects(text);
            if (values == null) {
                SouperSecretSettingsClient.say("shader.error.object", 1, text);
                return 0;
            }
            if (index >= values.size()) {
                SouperSecretSettingsClient.say("shader.error.object_index", 1, index, values.size()-1);
                return 0;
            }

            int variable = -1;
            try {
                variable = Integer.parseInt(text.substring(0, text.indexOf('_')));
            } catch (Exception ignored) {}

            if (variable < 0 || variable >= uniform.override.overrideSources.size()) {
                SouperSecretSettingsClient.say("shader.error.value", 1, variable, uniform.override.overrideSources.size()-1);
                return 0;
            }

            new UniformChangeAction(IntegerArgumentType.getInteger(context, "uniform"), variable, uniform.override, uniform.config).addToHistory();

            Object objectAtIndex = values.get(index);
            Object object;
            if (objectAtIndex == null || objectAtIndex instanceof Number) {
                try {
                    object = Float.parseFloat(value);
                } catch (Exception ignored) {
                    if (value.equals("null")) {
                        object = null;
                    } else {
                        SouperSecretSettingsClient.say("shader.error.number", 1, value);
                        return 0;
                    }
                }
            } else {
                object = value;
            }

            try {
                values.set(index, object);
            } catch (Exception e) {
                ArrayList<Object> valuesMutable = new ArrayList<>(values);
                valuesMutable.set(index, object);
                uniform.config.config().put(text, valuesMutable);
            }
        }

        return 1;
    }

    public int info() {
        List<ShaderData> shaders = getList().stream().filter(shaderData -> shaderData.active).toList();

        if (shaders.isEmpty()) {
            SouperSecretSettingsClient.say("shader.info.none", 1);
        } else {
            MutableComponent text = Component.empty();

            int count = 1;
            String key = SouperSecretSettingsClient.MODID+".shader.info";
            ShaderData shaderData = shaders.getFirst();
            ShaderData next = shaderData;

            int i = 0;
            boolean search;
            do {
                search = i != shaders.size()-1;
                if (search) next = shaders.get(i+1);

                if (!search || !shaderData.shaderID.equals(next.shaderID)) {
                    String s = shaderData.getTranslatedName().getString();
                    if (count == 1) {
                        text.append(Component.translatable(key, s));
                    } else {
                        text.append(Component.translatable(key+".multiple", s, count));
                    }
                    if (i == count-1) {
                        key = SouperSecretSettingsClient.MODID+".shader.info.join";
                    }
                    shaderData = next;
                    count = 0;
                }
                count++;
                i++;
            } while (search);

            SouperSecretSettingsClient.sayStyled(text, 1);
        }

        return 1;
    }

    private static SuggestionProvider<FabricClientCommandSource> getRegistrySuggestions(Identifier registry, boolean fromGroups) {
        return (context, builder) -> {
            String current = new StringReader(builder.getRemaining()).readString();
            Map<String, Identifier> paths = new HashMap<>();
            boolean searchPaths = true;

            List<ShaderRegistryEntry> registryEntries = Shaders.getRegistry(registry);
            for (ShaderRegistryEntry shaderRegistry : registryEntries) {
                Identifier identifier = shaderRegistry.getID();
                String name = identifier.toString();

                Component text = Component.translatableWithFallback("gui.luminance.shader."+name.replace(':','.')+".description", "");
                builder.suggest(name, text.getString().isBlank() ? null : text);

                if (searchPaths) {
                    if (identifier.getPath().startsWith(current)) {
                        paths.putIfAbsent(identifier.getPath(), identifier);
                    }

                    if (name.startsWith(current)) {
                        searchPaths = false;
                    }
                }
            }

            if (fromGroups) {
                builder.suggest("all", SouperSecretSettingsClient.translate("shader.group_suggestion", registryEntries.size()));
            } else if (registryEntries.size() >= 2) {
                builder.suggest("random", SouperSecretSettingsClient.translate("shader.group_suggestion", registryEntries.size()));
            }

            if (searchPaths) {
                for (Identifier identifier : paths.values()) {
                    builder.suggest(identifier.getPath(), Component.literal(identifier.toString()));
                }
            }

            SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry).forEach(((name, shaderRegistryEntries) -> builder.suggest("random_" + name, SouperSecretSettingsClient.translate("shader.group_suggestion", shaderRegistryEntries.getComputed(registry, name).size()))));

            return builder.buildFuture();
        };
    }

    protected SuggestionProvider<FabricClientCommandSource> shaderSuggestions = SouperSecretSettingsCommands.createIndexSuggestion(
            (context) -> SouperSecretSettingsClient.soupRenderer.activeLayer.getList(getRegistry()),
            ShaderData::getTranslatedName
    );

    protected SuggestionProvider<FabricClientCommandSource> passSuggestions = (context, builder) -> {
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        int shaderIndex = IntegerArgumentType.getInteger(context, "shader");

        List<ShaderData> shaders = layer.getList(getRegistry());
        if (shaderIndex < shaders.size()) {
            ShaderData shader = shaders.get(shaderIndex);

            int total = 0;
            for (Identifier identifier : SouperSecretSettingsClient.soupRenderer.getRegistryChains(getRegistry())) {
                List<PostPass> passes = shader.postChainInterface.luminance$getPasses(identifier);
                if (passes != null) {
                    for (PostPass pass : passes) {
                        builder.suggest(total, Component.literal(ChainData.getName((PostPassInterface)pass)));
                        total++;
                    }
                }
            }
        }

        return builder.buildFuture();
    };

    protected SuggestionProvider<FabricClientCommandSource> uniformBlockSuggestions = (context, builder) -> {
        Map<String, BlockData> blocks = getBlockDatas(context, false);

        if (blocks != null) {
            for (String string : blocks.keySet()) {
                builder.suggest(string);
            }
        }

        return builder.buildFuture();
    };

    protected SuggestionProvider<FabricClientCommandSource> uniformSuggestions = (context, builder) -> {
        BlockData block = getBlockData(context, false);

        if (block != null) {
            for (int i = 0; i < block.uniformDatas.size(); i++) {
                builder.suggest(i, Component.literal(block.block.uniforms.get(i).name));
            }
        }

        return builder.buildFuture();
    };

    protected SuggestionProvider<FabricClientCommandSource> uniformNameSuggestions = (context, builder) -> {
        UniformData uniform = getUniformData(context, false);

        if (uniform != null) {
            for (int i = 0; i < uniform.override.overrideSources.size(); i++) {
                builder.suggest("value." + i);
            }

            for (String string : uniform.config.getNames()) {
                List<Object> objects = uniform.config.getObjects(string);
                assert objects != null;
                for (int i = 0; i < objects.size(); i++) {
                    builder.suggest(string + "." + i);
                }
            }
        }

        return builder.buildFuture();
    };

    protected SuggestionProvider<FabricClientCommandSource> uniformValueSuggestions = (context, builder) -> {
        UniformData uniform = getUniformData(context, false);

        if (uniform != null) {
            String name = StringArgumentType.getString(context, "name");
            int breakIndex = name.lastIndexOf('.');
            if (breakIndex > 0) {
                int index = -1;
                try {
                    index = Integer.parseInt(name.substring(breakIndex + 1));
                } catch (Exception ignored) {
                }

                if (index >= 0) {
                    String defaultValue = null;
                    String currentValue = null;

                    String text = name.substring(0, breakIndex);
                    if (text.equals("value")) {
                        List<String> values = uniform.override.getStrings();
                        if (index < values.size()) {
                            currentValue = values.get(index);
                            assert uniform.defaultValue != null;
                            defaultValue = uniform.defaultValue.override.getStrings().get(index);
                        }
                    } else {
                        List<Object> values = uniform.config.getObjects(text);
                        if (values != null && index < values.size()) {
                            currentValue = values.get(index).toString();
                            assert uniform.defaultValue != null;
                            List<Object> defaultObjects = uniform.defaultValue.config.getObjects(text);
                            defaultValue = (defaultObjects == null || index >= defaultObjects.size()) ? null : defaultObjects.get(index).toString();
                        }
                    }

                    if (currentValue != null) {
                        if (!currentValue.equals(defaultValue)) {
                            builder.suggest(currentValue, SouperSecretSettingsClient.translate("shader.value.current"));
                        }
                        if (defaultValue != null) {
                            builder.suggest(defaultValue, SouperSecretSettingsClient.translate("shader.value.default"));
                        }
                    }
                }
            }
        }

        return builder.buildFuture();
    };

    @Nullable
    protected Map<String, BlockData> getBlockDatas(CommandContext<FabricClientCommandSource> context, boolean feedback) {
        int shaderIndex = IntegerArgumentType.getInteger(context, "shader");
        ShaderLayer layer = SouperSecretSettingsClient.soupRenderer.activeLayer;

        List<ShaderData> shaders = layer.getList(registry);
        if (shaderIndex >= shaders.size()) {
            if (feedback) {
                SouperSecretSettingsClient.say("shader.error.shader",1, shaderIndex, shaders.size()-1);
            }
            return null;
        }

        int passIndex = IntegerArgumentType.getInteger(context, "pass");
        int pass = passIndex;
        int total = 0;

        ShaderData shader = shaders.get(shaderIndex);

        for (Identifier identifier : SouperSecretSettingsClient.soupRenderer.getRegistryChains(registry)) {
            ChainData chainData = shader.getPassData(identifier);
            if (chainData != null) {
                int size = chainData.passBlocks.size();
                if (pass < size) {
                    return chainData.passBlocks.get(pass);
                }
                pass -= size;
                total += size;
            }
        }

        if (feedback) {
            SouperSecretSettingsClient.say("shader.error.pass", 1, passIndex, total-1);
        }
        return null;
    }

    @Nullable
    protected BlockData getBlockData(CommandContext<FabricClientCommandSource> context, boolean feedback) {
        Map<String, BlockData> blocks = getBlockDatas(context, feedback);

        if (blocks == null) {
            return null;
        }

        String block = StringArgumentType.getString(context, "block");
        BlockData blockData = blocks.get(block);

        if (blockData == null && feedback) {
            SouperSecretSettingsClient.say("shader.error.block", 1, block);
        }

        return blockData;
    }

    @Nullable
    protected UniformData getUniformData(CommandContext<FabricClientCommandSource> context, boolean feedback) {
        BlockData block = getBlockData(context, feedback);

        if (block == null) {
            return null;
        }

        int i = IntegerArgumentType.getInteger(context, "uniform");
        if (i >= block.uniformDatas.size()) {
            if (feedback) {
            SouperSecretSettingsClient.say("shader.error.uniform", 1, i, block.uniformDatas.size()-1);
            }
            return null;
        }

        return block.uniformDatas.get(i);
    }

    protected final SuggestionProvider<FabricClientCommandSource> groupSuggestions = (context, builder) -> {
        Map<String, Group> groups = SouperSecretSettingsClient.soupRenderer.getShaderGroups(getRegistry());
        groups.keySet().forEach(builder::suggest);
        return builder.buildFuture();
    };

    protected final SuggestionProvider<FabricClientCommandSource> userGroupSuggestions = (context, builder) -> {
        Map<String, Group> groups = SouperSecretSettingsClient.soupRenderer.getShaderGroups(getRegistry());
        groups.keySet().forEach(name -> {
            if (name.startsWith("user/")) {
                builder.suggest(name);
            }
        });
        return builder.buildFuture();
    };

    protected final SuggestionProvider<FabricClientCommandSource> groupIndexes = SouperSecretSettingsCommands.createIndexSuggestion(context -> SouperSecretSettingsClient.soupRenderer.getShaderGroups(getRegistry()).getOrDefault(context.getArgument("name", Identifier.class).getPath(), new Group()).entries, Component::literal);

    @Override
    List<ShaderData> getList() {
        return SouperSecretSettingsClient.soupRenderer.activeLayer.getList(registry);
    }

    @Override
    String getID(ShaderData value) {
        return value.shaderID.toString();
    }

    @Override
    SuggestionProvider<FabricClientCommandSource> getIndexSuggestions() {
        return shaderSuggestions;
    }

    public Identifier getRegistry() {
        return registry;
    }

    public int createGroup(String name) {
        if (!name.startsWith("user/")) {
            name = "user/"+name;
        }

        Map<String, Group> map = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry);

        if (map.containsKey(name)) {
            SouperSecretSettingsClient.say("group.error.exists", 1, name);
            return 0;
        }

        if (name.isBlank()) {
            SouperSecretSettingsClient.say("group.error.value", 1, name);
            return 0;
        }

        Group group = new Group();
        map.put(name, group);
        SouperSecretSettingsClient.say("group.create", 0, name);
        group.changed = true;
        groupsChanged();
        return 1;
    }

    public int addGroupEntry(String name, Identifier id, int position) {
        String entry;
        if (id.getNamespace().equals("minecraft") && (id.getPath().equals("all") || id.getPath().startsWith("random_"))) {
            entry = id.getPath();
        } else {
            entry = id.toString();
        }

        Group group = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry).get(name);
        if (group == null) {
            SouperSecretSettingsClient.say("group.missing", 1, name);
            return 0;
        }

        if (entry.isBlank()) {
            SouperSecretSettingsClient.say("group.error.value", 1, name);
            return 0;
        }

        int c = entry.charAt(0);
        if (c != '+' && c != '-') {
            entry = '+'+entry;
        }

        if (position < 0 || position > group.entries.size()) {
            group.entries.addLast(entry);
        } else {
            group.entries.add(position, entry);
        }

        SouperSecretSettingsClient.say("group.add", 0, entry);
        group.changed = true;
        groupsChanged();
        return 1;
    }

    public int removeGroupEntry(String name, int index) {
        Group group = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry).get(name);
        if (group == null) {
            SouperSecretSettingsClient.say("group.missing", 1, name);
            return 0;
        }
        if (index >= group.entries.size()) {
            SouperSecretSettingsClient.say("group.error.index", 1, index, group.entries.size()-1);
            return 0;
        }
        String entry = group.entries.remove(index);
        SouperSecretSettingsClient.say("group.remove_entry", 0, index, entry);
        group.changed = true;
        groupsChanged();
        return 1;
    }

    public int toggleGroupEntry(String name, int index) {
        Group group = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry).get(name);
        if (group == null) {
            SouperSecretSettingsClient.say("group.missing", 1, name);
            return 0;
        }
        if (index >= group.entries.size()) {
            SouperSecretSettingsClient.say("group.error.index", 1, index, group.entries.size()-1);
            return 0;
        }
        String entry = group.entries.get(index);
        entry = (entry.charAt(0) == '-' ? "+" : "-")+entry.substring(1);
        group.entries.set(index, entry);
        SouperSecretSettingsClient.say("group.toggle", 0, index, entry);
        group.changed = true;
        groupsChanged();
        return 1;
    }

    public int removeGroup(String name) {
        Map<String, Group> map = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry);
        if (!name.startsWith("user/") && !name.isBlank()) {
            name = "user/"+name;
        }

        if (!(map.containsKey(name))) {
            SouperSecretSettingsClient.say("group.missing", 1, name);
            return 0;
        }
        SouperSecretSettingsClient.say("group.remove", 0, name);
        map.remove(name).deleteFile();
        groupsChanged();
        return 1;
    }

    private void groupsChanged() {
        for (Group group : SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry).values()) {
            group.requestUpdate();
        }
        SouperSecretSettingsClient.soupData.changeData(true);
    }
}
