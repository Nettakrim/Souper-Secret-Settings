package com.nettakrim.souper_secret_settings.shaders.calculations;

import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;

public abstract class Calculation {
    public OverrideSource[] inputs = new OverrideSource[inputCount()];
    public String[] outputs = new String[outputCount()];

    protected float[] inputValues = new float[inputs.length];
    protected float[] outputValues = new float[outputs.length];

    public void update(ShaderStack stack) {
        for (int i = 0; i < inputs.length; i++) {
            OverrideSource overrideSource = inputs[i];
            if (overrideSource == null) continue;

            Float f = overrideSource.get();
            if (f == null) continue;

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
