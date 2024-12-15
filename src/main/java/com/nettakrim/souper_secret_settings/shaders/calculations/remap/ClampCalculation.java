package com.nettakrim.souper_secret_settings.shaders.calculations.remap;

import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;

public class ClampCalculation extends Calculation {
    public ClampCalculation(String id) {
        super(id);
    }

    @Override
    protected String[] getInputs() {
        return new String[]{"","0","1"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void calculateOutputValues() {
        float x = inputValues[0];
        float a = inputValues[1];
        float b = inputValues[2];
        outputValues[0] = 1-((1-Math.max(Math.min(x,b)/b,a))/(1-a));
    }
}
