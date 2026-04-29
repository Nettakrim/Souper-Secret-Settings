package com.nettakrim.souper_secret_settings.commands;

import dev.dannytaylor.luminance.client.shaders.overrides.OverrideSource;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.ArrSetAction;
import com.nettakrim.souper_secret_settings.actions.ListAddAction;
import com.nettakrim.souper_secret_settings.actions.ToggleAction;
import com.nettakrim.souper_secret_settings.shaders.ParameterOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculations;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ParameterCommand extends ListCommand<Calculation> {
    public ParameterCommand() {

    }

    public void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:parameter").build();
        root.addChild(commandNode);

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
                .literal("add")
                .then(
                        ClientCommandManager.argument("id", StringArgumentType.string())
                                .suggests(calculationIDs)
                                .executes(context -> add(StringArgumentType.getString(context, "id"), -1))
                                .then(
                                        ClientCommandManager.argument("position", IntegerArgumentType.integer(-1))
                                                .executes(context -> add(StringArgumentType.getString(context, "id"), IntegerArgumentType.getInteger(context, "position")))
                                )
                )
                .build();
        commandNode.addChild(addNode);

        LiteralCommandNode<FabricClientCommandSource> modifyNode = ClientCommandManager
                .literal("modify")
                .then(
                        ClientCommandManager.argument("parameter", IntegerArgumentType.integer(0))
                                .suggests(calculationIndexes)
                                .then(
                                        ClientCommandManager.literal("input")
                                                .then(
                                                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                                                .suggests(calculationInputs)
                                                                .then(
                                                                        ClientCommandManager.argument("value", StringArgumentType.string())
                                                                                .suggests(calculationInputValue)
                                                                                .executes(this::setInput)
                                                                )
                                                )
                                )
                                .then(
                                        ClientCommandManager.literal("output")
                                                .then(
                                                        ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                                                                .suggests(calculationOutputs)
                                                                .then(
                                                                        ClientCommandManager.argument("value", StringArgumentType.string())
                                                                                .suggests(calculationOutputValue)
                                                                                .executes(this::setOutput)
                                                                )
                                                )
                                )
                                .then(
                                        ClientCommandManager.literal("toggle")
                                                .executes(this::toggle)
                                )
                )
                .build();
        commandNode.addChild(modifyNode);

        registerList(commandNode);
    }

    int add(String id, int position) {
        Calculation calculation = Calculations.createCalculation(id);
        if (calculation == null) {
            SouperSecretSettingsClient.say("parameter.missing", 1, id);
            return 0;
        }

        if (position < 0 || position > SouperSecretSettingsClient.soupRenderer.activeLayer.calculations.size()) {
            position = SouperSecretSettingsClient.soupRenderer.activeLayer.calculations.size();
        }

        new ListAddAction<>(SouperSecretSettingsClient.soupRenderer.activeLayer.calculations, calculation, position).addToHistory();
        SouperSecretSettingsClient.soupRenderer.activeLayer.calculations.add(position, calculation);
        return 1;
    }

    int toggle(CommandContext<FabricClientCommandSource> context) {
        Calculation calculation = getCalculation(context, true);
        if (calculation == null) {
            return 0;
        }

        new ToggleAction(calculation).addToHistory();
        calculation.toggle();
        return 1;
    }

    int setInput(CommandContext<FabricClientCommandSource> context) {
        Calculation calculation = getCalculation(context, true);
        if (calculation == null) {
            return 0;
        }

        int index = IntegerArgumentType.getInteger(context, "index");
        if (index >= calculation.inputs.length) {
            SouperSecretSettingsClient.say("parameter.error.input", 1, index, calculation.inputs.length-1);
            return 0;
        }

        String value = StringArgumentType.getString(context, "value");
        new ArrSetAction<>(calculation.inputs, index);
        calculation.inputs[index] = ParameterOverrideSource.parameterSourceFromString(value);

        return 1;
    }

    int setOutput(CommandContext<FabricClientCommandSource> context) {
        Calculation calculation = getCalculation(context, true);
        if (calculation == null) {
            return 0;
        }

        int index = IntegerArgumentType.getInteger(context, "index");
        if (index >= calculation.outputs.length) {
            SouperSecretSettingsClient.say("parameter.error.output", 1, index, calculation.outputs.length-1);
            return 0;
        }

        String value = StringArgumentType.getString(context, "value");
        new ArrSetAction<>(calculation.outputs, index);
        calculation.outputs[index] = value;

        return 1;
    }

    @Nullable
    static Calculation getCalculation(CommandContext<FabricClientCommandSource> context, boolean feedback) {
        int index = IntegerArgumentType.getInteger(context, "parameter");
        if (index < SouperSecretSettingsClient.soupRenderer.activeLayer.calculations.size()) {
            return SouperSecretSettingsClient.soupRenderer.activeLayer.calculations.get(index);
        }
        if (feedback) {
            SouperSecretSettingsClient.say("parameter.error.index", 1, index, SouperSecretSettingsClient.soupRenderer.activeLayer.calculations.size() - 1);
        }
        return null;
    }

    static SuggestionProvider<FabricClientCommandSource> calculationIDs = (context, builder) -> {
        for (String id : Calculations.getIds()) {
            builder.suggest(id);
        }

        return builder.buildFuture();
    };

    static SuggestionProvider<FabricClientCommandSource> calculationIndexes = SouperSecretSettingsCommands.createIndexSuggestion(
            (context) -> SouperSecretSettingsClient.soupRenderer.activeLayer.calculations,
            (value) -> Component.literal(value.getID())
    );

    static SuggestionProvider<FabricClientCommandSource> calculationInputs = SouperSecretSettingsCommands.createIndexSuggestion(
            (context) -> {
                Calculation calculation = getCalculation(context, false);
                return calculation == null ? null : List.of(calculation.inputNames);
            },
            Component::literal
    );

    static SuggestionProvider<FabricClientCommandSource> calculationInputValue = SouperSecretSettingsCommands.createValueSuggestion(
            (context) -> {
                Calculation calculation = getCalculation(context, false);
                return calculation == null ? null : List.of(calculation.inputs);
            },
            OverrideSource::getString,
            "index"
    );

    static SuggestionProvider<FabricClientCommandSource> calculationOutputs = SouperSecretSettingsCommands.createIndexSuggestion(
            (context) -> {
                Calculation calculation = getCalculation(context, false);
                return calculation == null ? null : List.of(calculation.outputs);
            },
            (message) -> null
    );

    static SuggestionProvider<FabricClientCommandSource> calculationOutputValue = SouperSecretSettingsCommands.createValueSuggestion(
            (context) -> {
                Calculation calculation = getCalculation(context, false);
                return calculation == null ? null : List.of(calculation.outputs);
            },
            (value) -> value,
            "index"
    );


    @Override
    List<Calculation> getList() {
        return SouperSecretSettingsClient.soupRenderer.activeLayer.calculations;
    }

    @Override
    String getID(Calculation value) {
        return value.getID();
    }

    @Override
    SuggestionProvider<FabricClientCommandSource> getIndexSuggestions() {
        return calculationIndexes;
    }
}
