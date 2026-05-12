package com.nettakrim.souper_secret_settings.shaders.custom.shader.color;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.GraphCompilationException;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.FunctionDependency;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class HSVtoRGBNode extends Node implements FunctionDependency {
    public HSVtoRGBNode() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("HSV", PortType.VEC3);
        addOutput("RGB", PortType.VEC3);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) throws GraphCompilationException {
        return outputPorts.getFirst().getGlVariableDeclaration()+" = HSVtoRGB("+organisedNode.getVectorInput(0, PortType.VEC3)+")";
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("HSV to RGB");
    }

    @Override
    public String functionName() {
        return "HSVtoRGB";
    }

    @Override
    public String functionImplementation() {
        return """
vec3 HueToRGB(float h) {
    float r = abs(h * 6.0 - 3.0) - 1.0;
    float g = 2.0 - abs(h * 6.0 - 2.0);
    float b = 2.0 - abs(h * 6.0 - 4.0);
    return clamp(vec3(r,g,b), 0.0, 1.0);
}

vec3 HSVtoRGB(vec3 hsv) {
    return ((HueToRGB(fract(hsv.x)) - 1.0) * hsv.y + 1.0) * hsv.z;
}
""";
    }
}
