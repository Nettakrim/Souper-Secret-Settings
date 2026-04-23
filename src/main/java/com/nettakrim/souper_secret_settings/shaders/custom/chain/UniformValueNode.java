package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.mclegoman.luminance.client.shaders.IVec2Uniform;
import com.mclegoman.luminance.client.shaders.IVec4Uniform;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.ValueNode;
import net.minecraft.client.renderer.UniformValue;
import org.joml.*;

import java.util.List;
import java.util.function.Supplier;

public class UniformValueNode extends ValueNode {
    private final List<Number> values;

    UniformValueNode(List<Number> values) {
        this.values = values;
        initialisePorts();
    }

    @Override
    protected PortType getType() {
        return PortType.UNIFORM_VALUE;
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        int size = values.size();
        boolean isInt = values.getFirst() instanceof Integer;

        UniformValue uniformValue;
        if (size == 1) {
            uniformValue = isInt ? new UniformValue.IntUniform(values.getFirst().intValue()) : new UniformValue.FloatUniform(values.getFirst().floatValue());
        }
        else if (size == 2) {
            uniformValue = isInt ? new IVec2Uniform(new Vector2i(values.get(0).intValue(), values.get(1).intValue())) : new UniformValue.Vec2Uniform(new Vector2f(values.get(0).floatValue(), values.get(1).floatValue()));
        }
        else if (size == 3) {
            uniformValue = isInt ? new UniformValue.IVec3Uniform(new Vector3i(values.get(0).intValue(), values.get(1).intValue(), values.get(2).intValue())) : new UniformValue.Vec3Uniform(new Vector3f(values.get(0).floatValue(), values.get(1).floatValue(), values.get(2).floatValue()));
        }
        else if (size == 4) {
            uniformValue = isInt ? new IVec4Uniform(new Vector4i(values.get(0).intValue(), values.get(1).intValue(), values.get(2).intValue(), values.get(3).intValue())) : new UniformValue.Vec4Uniform(new Vector4f(values.get(0).floatValue(), values.get(1).floatValue(), values.get(2).floatValue(), values.get(3).floatValue()));
        } else {
            uniformValue = null;
        }

        outputPorts.getFirst().outputData = uniformValue;
    }
}
