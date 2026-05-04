package com.nettakrim.souper_secret_settings.shaders.custom.shader.vector;

import com.nettakrim.souper_secret_settings.shaders.custom.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MergeNode extends Node {
    private final PortType type;

    public MergeNode(PortType type) {
        this.type = type;
        initialisePorts();

        for (InputPort inputPort : inputPorts) {
            inputPort.docked = new FloatNode();
        }
    }

    @Override
    protected void initialisePorts() {
        int count = type.ordinal() - PortType.VECN.ordinal();
        for (int i = 0; i < count; i++) {
            addInput(String.valueOf("XYZW".charAt(i)), PortType.VEC1);
        }

        addOutput("Vector", type);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) throws GraphCompilationException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(outputPorts.getFirst().getGlVariableDeclaration()).append(" = ").append(outputPorts.getFirst().portType.glType).append('(');

        int count = type.ordinal() - PortType.VECN.ordinal();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                stringBuilder.append(',');
            }
            stringBuilder.append(organisedNode.getVectorInput(i, PortType.VEC1));
        }
        stringBuilder.append(')');

        return stringBuilder.toString();
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Merge");
    }
}
