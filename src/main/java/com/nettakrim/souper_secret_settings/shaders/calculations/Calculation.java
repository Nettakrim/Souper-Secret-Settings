package com.nettakrim.souper_secret_settings.shaders.calculations;

import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ParameterOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;

import java.util.ArrayList;
import java.util.List;

public abstract class Calculation {
    public OverrideSource[] inputs;
    public String[] outputs;

    protected float[] inputValues;
    protected float[] outputValues;

    private final String id;

    public Calculation(String id) {
        this.id = id;

        String[] inputStrings = getInputs();

        this.inputs = new OverrideSource[inputStrings.length];
        for (int i = 0; i < inputs.length; i++) {
            this.inputs[i] = ParameterOverrideSource.parameterSourceFromString(inputStrings[i]);
        }

        this.outputs = getOutputs();

        inputValues = new float[inputs.length];
        outputValues = new float[outputs.length];
    }

    protected abstract String[] getInputs();
    protected abstract String[] getOutputs();

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
            String output = outputs[i];
            if (!output.isBlank()) {
                stack.parameterValues.put(output, outputValues[i]);
            }
        }
    }

    protected abstract void calculateOutputValues();

    public String getID() {
        return id;
    }

    public List<Float> getLastOutput() {
        List<Float> list = new ArrayList<>(outputValues.length);
        for (float outputValue : outputValues) {
            list.add(outputValue);
        }
        return list;
    }
}
