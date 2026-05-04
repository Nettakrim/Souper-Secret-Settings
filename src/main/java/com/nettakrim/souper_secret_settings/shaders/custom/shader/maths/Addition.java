package com.nettakrim.souper_secret_settings.shaders.custom.shader.maths;

import com.nettakrim.souper_secret_settings.shaders.custom.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Supplier;

public class Addition extends Node {
    private PortType dynamicType;

    public Addition() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("A", PortType.VECN);
        addInput("B", PortType.VECN);
        addOutput("Out", PortType.VECN);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) {
        return outputPorts.getFirst().getGlVariableDeclaration() + " = "+organisedNode.getVectorInput(0, dynamicType)+" + "+organisedNode.getVectorInput(1, dynamicType);
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Add");
    }

    @Override
    public void updateConnections(HashMap<InputPort, Wire> wires) {
        // set all port types to be the widest input type
        // this currently relies on the ordinals of the types, so it is fragile
        dynamicType = PortType.VECN;

        for (InputPort inputPort : inputPorts) {
            Wire wire = wires.get(inputPort);
            if (wire != null) {
                PortType inputType = wire.source.portType;
                if (inputType.ordinal() > dynamicType.ordinal()) {
                    dynamicType = inputType;
                }
            }
        }

        for (InputPort inputPort : inputPorts) {
            inputPort.portType = dynamicType;
        }

        for (OutputPort outputPort : outputPorts) {
            outputPort.portType = dynamicType;
        }
    }
}
