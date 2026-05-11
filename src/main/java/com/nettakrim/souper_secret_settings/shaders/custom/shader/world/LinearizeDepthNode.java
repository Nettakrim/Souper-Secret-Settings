package com.nettakrim.souper_secret_settings.shaders.custom.shader.world;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.GraphCompilationException;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.FunctionDependency;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class LinearizeDepthNode extends Node implements FunctionDependency {
    public LinearizeDepthNode() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("Depth", PortType.VEC1);
        addOutput("Distance", PortType.VEC1);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) throws GraphCompilationException {
        return outputPorts.getFirst().getGlVariableDeclaration()+" = LinearizeDepth("+organisedNode.getVectorInput(0, PortType.VEC1)+")";
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Linearize Depth");
    }

    @Override
    public String functionName() {
        return "LinearizeDepth";
    }

    @Override
    public String functionImplementation() {
        // TODO: needs to use clipping variable
        return """
float LinearizeDepth(float depth) {
    return (0.05*2048.0) / (depth * (0.05 - 2048.0) + 2048.0);
}
""";
    }
}
