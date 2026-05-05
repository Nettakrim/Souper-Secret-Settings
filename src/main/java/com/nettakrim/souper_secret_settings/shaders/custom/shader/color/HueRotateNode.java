package com.nettakrim.souper_secret_settings.shaders.custom.shader.color;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.GraphCompilationException;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.FunctionDependency;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.vector.FloatNode;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class HueRotateNode extends Node implements FunctionDependency {
    public HueRotateNode() {
        initialisePorts();
        inputPorts.get(1).setDock(new FloatNode());
    }

    @Override
    protected void initialisePorts() {
        addInput("Color", PortType.VEC3);
        addInput("Rotation", PortType.VEC1);
        addOutput("Out", PortType.VEC3);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) throws GraphCompilationException {
        return outputPorts.getFirst().getGlVariableDeclaration()+" = hueShift("+organisedNode.getVectorInput(0, PortType.VEC3)+","+organisedNode.getVectorInput(1, PortType.VEC1)+")";
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Hue Rotate");
    }

    @Override
    public String functionName() {
        return "hueShift";
    }

    @Override
    public String functionImplementation() {
        return """
vec3 hueShift(vec3 color, float hue) {
    const vec3 k = vec3(0.57735, 0.57735, 0.57735);
    float cosAngle = cos(hue);
    return vec3(color * cosAngle + cross(k, color) * sin(hue) + k * dot(k, color) * (1.0 - cosAngle));
}
""";
    }
}
