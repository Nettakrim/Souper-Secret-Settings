package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.overrides.UniformSource;

import java.util.Optional;

public class ParameterOverrideSource implements OverrideSource {
    public UniformSource source;
    private Float lastValue = 0f;

    public ParameterOverrideSource(UniformSource source) {
        this.source = source;
    }

    @Override
    public Float get() {
        ShaderStack stack = ShaderStack.getRenderingStack();
        if (stack != null) {
            if (hasParameter(stack)) {
                lastValue = stack.parameterValues.get(source.getString());
            } else {
                Float f = source.get();
                if (f == null) return f;

                Optional<Float> min = source.getUniform().getMin();
                Optional<Float> max = source.getUniform().getMax();
                if (min.isPresent() && max.isPresent()) {
                    f = (f - min.get()) / (max.get() - min.get());
                }
                lastValue = f;
            }
        }

        return lastValue;
    }

    @Override
    public String getString() {
        return source.getString();
    }

    public boolean hasParameter(ShaderStack stack) {
        return stack.parameterValues.containsKey(source.getString());
    }

    public static OverrideSource parameterSourceFromString(String s) {
        OverrideSource overrideSource = LuminanceUniformOverride.sourceFromString(s);
        if (overrideSource instanceof UniformSource uniformSource) {
            return new ParameterOverrideSource(uniformSource);
        }
        return overrideSource;
    }
}
