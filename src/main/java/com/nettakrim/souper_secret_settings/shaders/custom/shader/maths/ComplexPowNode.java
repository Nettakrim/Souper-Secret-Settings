package com.nettakrim.souper_secret_settings.shaders.custom.shader.maths;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.GraphCompilationException;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.FunctionDependency;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ComplexPowNode extends Node implements FunctionDependency {
    public ComplexPowNode() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("Base", PortType.VEC2);
        addInput("Exponent", PortType.VEC2);
        addOutput("Out", PortType.VEC2);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) throws GraphCompilationException {
        return outputPorts.getFirst().getGlVariableDeclaration() + " = ComplexPow(" + organisedNode.getVectorInput(0, PortType.VEC2) + "," + organisedNode.getVectorInput(1, PortType.VEC2) + ")";
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Complex Pow");
    }

    @Override
    public String functionName() {
        return "ComplexPow";
    }

    @Override
    public String functionImplementation() {
        return """
vec2 ComplexPow(vec2 z, vec2 n) {
    float r = length(z);
    float theta = atan(z.y, z.x);
    float realPart = pow(r, n.x) * cos(n.x * theta) * exp(-n.y * theta);
    float imagPart = pow(r, n.x) * sin(n.x * theta) * exp(n.y * theta);
    return vec2(realPart, imagPart);
}
""";
    }
}
