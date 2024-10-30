package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.overrides.UniformSource;

import java.util.Optional;

public class MixOverrideSource implements OverrideSource {
    public float a;
    public float b;
    public OverrideSource overrideSource;

    public MixOverrideSource(float a, float b, OverrideSource overrideSource) {
        this.a = a;
        this.b = b;
        this.overrideSource = overrideSource;
    }

    @Override
    public Float get() {
        Float t = overrideSource.get();
        if (t == null) return null;
        if (overrideSource instanceof UniformSource uniformSource) {
            Optional<Float> min = uniformSource.getUniform().getMin();
            Optional<Float> max = uniformSource.getUniform().getMax();
            if (min.isPresent() && max.isPresent()) {
                t = (t-min.get())/(max.get()-min.get());
            }
        }
        return ( 1.0f - t) * a + b * t;
    }

    @Override
    public String getString() {
        return "mix("+a+"/"+b+"/"+overrideSource.getString()+")";
    }
}
