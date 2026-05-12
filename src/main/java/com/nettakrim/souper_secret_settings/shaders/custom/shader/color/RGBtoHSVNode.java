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

public class RGBtoHSVNode extends Node implements FunctionDependency {
    public RGBtoHSVNode() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("RGB", PortType.VEC3);
        addOutput("HSV", PortType.VEC3);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) throws GraphCompilationException {
        return outputPorts.getFirst().getGlVariableDeclaration()+" = RGBtoHSV("+organisedNode.getVectorInput(0, PortType.VEC3)+")";
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("RGB to HSV");
    }

    @Override
    public String functionName() {
        return "RGBtoHSV";
    }

    @Override
    public String functionImplementation() {
        return """
vec3 RGBtoHSV(vec3 rgb) {
    vec3 hsv = vec3(0.0);
    hsv.z = max(rgb.r, max(rgb.g, rgb.b));
    float m = min(rgb.r, min(rgb.g, rgb.b));
    float c = hsv.z - m;

    if (c != 0.0)
    {
        hsv.y = c / hsv.z;
        vec3 delta = (hsv.z - rgb) / c;
        delta.rgb -= delta.brg;
        delta.rg += vec2(2.0, 4.0);
        if (rgb.r >= hsv.z) {
            hsv.x = delta.b;
        } else if (rgb.g >= hsv.z) {
            hsv.x = delta.r;
        } else {
            hsv.x = delta.g;
        }
        hsv.x = fract(hsv.x / 6.0);
    }
    return hsv;
}
""";
    }
}
