package com.nettakrim.souper_secret_settings.commands;

import dev.dannytaylor.luminance.client.shaders.Shaders;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.RootCommandNode;

import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.function.Function;

public class SouperSecretSettingsCommands {
    public static LayerCommand layerCommand;
    public static ShaderListCommand shaderCommand;
    public static ShaderListCommand modifierCommand;
    public static ParameterCommand parameterCommand;

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            RootCommandNode<FabricClientCommandSource> root = dispatcher.getRoot();

            layerCommand = new LayerCommand();
            shaderCommand = new ShaderListCommand("shader", Shaders.getMainRegistryId(), 100);
            modifierCommand = new ShaderListCommand("modifier", SoupRenderer.modifierRegistry, 25);
            parameterCommand = new ParameterCommand();

            GuiCommand.register(root);
            OptionCommand.register(root, registryAccess);
            ActionCommand.register(root);

            layerCommand.register(root);
            shaderCommand.register(root);
            modifierCommand.register(root);
            parameterCommand.register(root);
        });
    }

    static <T> SuggestionProvider<FabricClientCommandSource> createIndexSuggestion(Function<CommandContext<FabricClientCommandSource>, List<T>> listFunction, Function<T, Component> messageFunction) {
        return (context, builder) -> {
            List<T> list = listFunction.apply(context);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    builder.suggest(i, messageFunction.apply(list.get(i)));
                }
            }

            return builder.buildFuture();
        };
    }

    static <T> SuggestionProvider<FabricClientCommandSource> createValueSuggestion(Function<CommandContext<FabricClientCommandSource>, List<T>> listFunction, Function<T, String> valueFunction, String indexArgument) {
        return (context, builder) -> {
            List<T> list = listFunction.apply(context);
            if (list != null) {
                int index = IntegerArgumentType.getInteger(context, indexArgument);
                if (index < list.size()) {
                    String s = valueFunction.apply(list.get(index));
                    if (!s.isBlank()) {
                        builder.suggest(s);
                    }
                }
            }

            return builder.buildFuture();
        };
    }
}
