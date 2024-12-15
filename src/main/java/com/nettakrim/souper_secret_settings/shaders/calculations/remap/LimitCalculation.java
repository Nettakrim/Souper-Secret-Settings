package com.nettakrim.souper_secret_settings.shaders.calculations.remap;

import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;

public class LimitCalculation extends Calculation {
    public LimitCalculation(String id) {
        super(id);
    }

    @Override
    protected String[] getInputs() {
        return new String[]{"","1"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void calculateOutputValues() {
        float t = inputValues[0]*inputValues[1];
        outputValues[0] = t/(t+1);
    }
}
