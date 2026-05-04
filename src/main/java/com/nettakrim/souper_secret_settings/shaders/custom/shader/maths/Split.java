package com.nettakrim.souper_secret_settings.shaders.custom.shader.maths;

import com.nettakrim.souper_secret_settings.shaders.custom.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Supplier;

public class Split extends Node {
    private PortType dynamicType;

    public Split() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("Vector", PortType.VECN);
        addOutput("X", PortType.VECN);
        addOutput("Y", PortType.VECN);
        addOutput("Z", PortType.VECN);
        addOutput("W", PortType.VECN);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        int count = dynamicType.ordinal() - PortType.VECN.ordinal();

        for (int i = 0; i < count; i++) {
            outputPorts.get(i).outputData = uuid.get();
        }
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) throws GraphCompilationException {
        StringBuilder stringBuilder = new StringBuilder();
        String inputVariable = organisedNode.getVectorInput(0, dynamicType);
        int count = dynamicType.ordinal() - PortType.VECN.ordinal();
        for (int i = 0; i < count; i++) {
            if (!stringBuilder.isEmpty()) {
                stringBuilder.append(";\n");
            }
            stringBuilder.append("float ").append(outputPorts.get(i).outputData).append(" = ").append(inputVariable);
            if (count > 1) {
                stringBuilder.append('.').append("xyzw".charAt(i));
            }
        }

        return stringBuilder.toString();
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Split");
    }

    @Override
    public void updateConnections(HashMap<InputPort, Wire> wires) {
        dynamicType = PortType.VECN;

        InputPort port = inputPorts.getFirst();
        Wire wire = wires.get(port);
        if (wire != null) {
            dynamicType = wire.source.portType;
        } else if (port.docked != null) {
            dynamicType = port.portType;
        }

        port.setPortType(dynamicType, wires);

        int count = dynamicType.ordinal() - PortType.VECN.ordinal();

        for (int i = 0; i < 4; i++) {
            outputPorts.get(i).setPortType(i < count ? PortType.VEC1 : PortType.VECN, wires);
        }
    }
}
