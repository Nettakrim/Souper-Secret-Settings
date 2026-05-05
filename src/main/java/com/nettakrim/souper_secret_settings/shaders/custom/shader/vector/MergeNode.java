package com.nettakrim.souper_secret_settings.shaders.custom.shader.vector;

import com.nettakrim.souper_secret_settings.shaders.custom.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Supplier;

public class MergeNode extends Node {
    public MergeNode() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("A", PortType.VECN);
        addInput("B", PortType.VECN);
        addInput("C", PortType.VECN);
        addInput("D", PortType.VECN);
        addOutput("Vector", PortType.VECN);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) throws GraphCompilationException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(outputPorts.getFirst().getGlVariableDeclaration()).append(" = ").append(outputPorts.getFirst().portType.glType).append('(');

        int count = 0;
        for (int i = 0; i < 4; i++) {
            PortType portType = inputPorts.get(i).portType;
            if (portType != PortType.UNUSED) {
                if (count > 0) {
                    stringBuilder.append(',');
                }
                stringBuilder.append(organisedNode.getVectorInput(i, portType));
                count++;
            }
        }
        stringBuilder.append(')');

        return stringBuilder.toString();
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Merge");
    }

    @Override
    public void updateConnections(HashMap<InputPort, Wire> wires) {
         int total = 0;

        for (InputPort inputPort : inputPorts) {
            PortType inputType = null;
            Wire wire = wires.get(inputPort);

            if (wire != null) {
                inputType = wire.source.portType;
            } else if (inputPort.docked != null) {
                inputType = inputPort.docked.outputPorts.getFirst().portType;
            }

            if (inputType != null) {
                int count = inputType.ordinal() - PortType.VECN.ordinal();
                if (total + count > 4) {
                    count = 4 - total;
                }
                inputPort.setPortType(count == 0 ? PortType.UNUSED : PortType.values()[count + PortType.VECN.ordinal()], wires);
                total += count;
            } else {
                inputPort.setPortType(PortType.UNUSED, wires);
            }
        }

        if (total > 4) {
            total = 4;
        }
        outputPorts.getFirst().setPortType(PortType.values()[total + PortType.VECN.ordinal()], wires);
    }
}
