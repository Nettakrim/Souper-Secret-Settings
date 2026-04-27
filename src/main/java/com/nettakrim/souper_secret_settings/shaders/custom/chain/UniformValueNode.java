package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.mclegoman.luminance.client.shaders.IVec2Uniform;
import com.mclegoman.luminance.client.shaders.IVec4Uniform;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.ValueNode;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.network.chat.Component;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class UniformValueNode extends ValueNode {
    private final List<Number> values;

    UniformValueNode(List<Number> template) {
        this.values = new ArrayList<>(template);
        initialisePorts();
    }

    @Override
    protected PortType getType() {
        return PortType.UNIFORM_VALUE;
    }

    @Override
    protected List<String> getValues() {
        return values.stream().map(Objects::toString).toList();
    }

    @Override
    protected void onSetValue(int index, String value) {
        try {
            float f = Float.parseFloat(value);
            boolean isInt = values.getFirst() instanceof Integer;
            values.set(index, isInt ? Math.round(f) : f);
        } catch (Exception ignored) {}
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = getUniformValue(values);
    }

    @Override
    protected Component getTitle() {
        return Component.literal("Uniform");
    }

    public static UniformValue getUniformValue(List<Number> values) {
        int size = values.size();
        boolean isInt = values.getFirst() instanceof Integer;

        if (size == 1) {
            return isInt ? new UniformValue.IntUniform(values.getFirst().intValue()) : new UniformValue.FloatUniform(values.getFirst().floatValue());
        }
        else if (size == 2) {
            return isInt ? new IVec2Uniform(new Vector2i(values.get(0).intValue(), values.get(1).intValue())) : new UniformValue.Vec2Uniform(new Vector2f(values.get(0).floatValue(), values.get(1).floatValue()));
        }
        else if (size == 3) {
            return isInt ? new UniformValue.IVec3Uniform(new Vector3i(values.get(0).intValue(), values.get(1).intValue(), values.get(2).intValue())) : new UniformValue.Vec3Uniform(new Vector3f(values.get(0).floatValue(), values.get(1).floatValue(), values.get(2).floatValue()));
        }
        else if (size == 4) {
            return isInt ? new IVec4Uniform(new Vector4i(values.get(0).intValue(), values.get(1).intValue(), values.get(2).intValue(), values.get(3).intValue())) : new UniformValue.Vec4Uniform(new Vector4f(values.get(0).floatValue(), values.get(1).floatValue(), values.get(2).floatValue(), values.get(3).floatValue()));
        } else {
            return null;
        }
    }
}
