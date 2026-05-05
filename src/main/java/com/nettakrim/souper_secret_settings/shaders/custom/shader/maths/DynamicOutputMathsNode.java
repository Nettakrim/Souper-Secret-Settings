package com.nettakrim.souper_secret_settings.shaders.custom.shader.maths;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.custom.CreationNode;
import com.nettakrim.souper_secret_settings.shaders.custom.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Supplier;

public class DynamicOutputMathsNode extends Node {
    protected PortType dynamicType;

    protected final String format;
    protected final Component name;
    protected final String[] inputNames;

    public static CreationNode get(Component name, String format, String... inputs) {
        return new CreationNode(name, () -> new DynamicOutputMathsNode(format, name, inputs));
    }

    public DynamicOutputMathsNode(String format, Component name, String... inputs) {
        this.format = format;
        this.name = name;
        this.inputNames = inputs;

        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        int values = 0;
        for (int i = 0; i < format.length(); i++) {
            if (format.charAt(i) == '%') {
                values++;
            }
        }

        if (values == 0) {
            SouperSecretSettingsClient.log("invalid formatting",format);
        } else if (values == 1) {
            addInput(inputNames.length > 0 ? inputNames[0] : "In", PortType.VECN);
        } else {
            for (int i = 0; i < values; i++) {
                addInput(i < inputNames.length ? inputNames[i] : String.valueOf((char)('A'+i)), PortType.VECN);
            }
        }

        addOutput("Out", PortType.VECN);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) throws GraphCompilationException {
        Object[] args = new Object[inputPorts.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = organisedNode.getVectorInput(i, dynamicType);
        }
        return outputPorts.getFirst().getGlVariableDeclaration() + " = " + format.formatted(args);
    }

    @Override
    protected @NotNull Component getTitle() {
        return name;
    }

    @Override
    public void updateConnections(HashMap<InputPort, Wire> wires) {
        dynamicType = setInputsToWidestType(wires);

        for (OutputPort outputPort : outputPorts) {
            outputPort.setPortType(dynamicType, wires);
        }
    }
}
