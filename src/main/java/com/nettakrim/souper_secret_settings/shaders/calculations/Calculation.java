package com.nettakrim.souper_secret_settings.shaders.calculations;

import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ParameterOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;

import java.util.Optional;

public abstract class Calculation {
    public OverrideSource[] inputs = new OverrideSource[inputCount()];
    public String[] outputs = new String[outputCount()];

    protected float[] inputValues = new float[inputs.length];
    protected float[] outputValues = new float[outputs.length];

    public void update(ShaderStack stack) {
        for (int i = 0; i < inputs.length; i++) {
            // the only types overrideSource should be is ParameterOverrideSource and FixedValueSource
            OverrideSource overrideSource = inputs[i];
            if (overrideSource == null) continue;

            Float f = overrideSource.get();
            if (f == null) continue;

            if (overrideSource instanceof ParameterOverrideSource parameterOverrideSource && !parameterOverrideSource.hasParameter()) {
                Optional<Float> min = parameterOverrideSource.source.getUniform().getMin();
                Optional<Float> max = parameterOverrideSource.source.getUniform().getMax();
                if (min.isPresent() && max.isPresent()) {
                    f = (f-min.get())/(max.get()-min.get());
                }
            }

            inputValues[i] = f;
        }

        calculateOutputValues();

        for (int i = 0; i < outputs.length; i++) {
            stack.parameterValues.put(outputs[i], outputValues[i]);
        }
    }

    protected abstract void calculateOutputValues();

    protected abstract int inputCount();

    protected abstract int outputCount();

    public abstract String getName();
}
