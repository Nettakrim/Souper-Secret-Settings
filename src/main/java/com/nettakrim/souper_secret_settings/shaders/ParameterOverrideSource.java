package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.overrides.UniformSource;

import java.util.Optional;

public class ParameterOverrideSource implements OverrideSource {
    public ShaderStack stack;
    public UniformSource source;

    public ParameterOverrideSource(ShaderStack stack, UniformSource source) {
        this.stack = stack;
        this.source = source;
    }

    @Override
    public Float get() {
        if (hasParameter()) {
            return stack.parameterValues.get(source.getString());
        } else {
            Float f = source.get();
            if (f == null) return f;

            Optional<Float> min = source.getUniform().getMin();
            Optional<Float> max = source.getUniform().getMax();
            if (min.isPresent() && max.isPresent()) {
                f = (f-min.get())/(max.get()-min.get());
            }
            return f;
        }
    }

    @Override
    public String getString() {
        return source.getString();
    }

    public boolean hasParameter() {
        return stack.parameterValues.containsKey(source.getString());
    }
}
