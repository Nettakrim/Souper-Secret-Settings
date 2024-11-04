package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.overrides.UniformSource;

public class ParameterOverrideSource implements OverrideSource {
    public ShaderStack stack;
    public UniformSource source;

    public ParameterOverrideSource(ShaderStack stack, UniformSource source) {
        this.stack = stack;
        this.source = source;
    }

    @Override
    public Float get() {
        return hasParameter() ? stack.parameterValues.get(source.getString()) : source.get();
    }

    @Override
    public String getString() {
        return source.getString();
    }

    public boolean hasParameter() {
        return stack.parameterValues.containsKey(source.getString());
    }
}
