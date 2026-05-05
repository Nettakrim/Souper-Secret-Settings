package com.nettakrim.souper_secret_settings.shaders.custom.shader.maths;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.GraphCompilationException;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CrossNode extends Node {
    public CrossNode() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("A", PortType.VEC3);
        addInput("B", PortType.VEC3);
        addOutput("Out", PortType.VEC3);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) throws GraphCompilationException {
        return outputPorts.getFirst().getGlVariableDeclaration() + " = cross(" + organisedNode.getVectorInput(0, PortType.VEC3)+", "+organisedNode.getVectorInput(1, PortType.VEC3)+")";
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Cross");
    }
}
