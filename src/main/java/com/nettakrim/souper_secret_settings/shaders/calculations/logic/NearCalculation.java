package com.nettakrim.souper_secret_settings.shaders.calculations.logic;

import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;

public class NearCalculation extends Calculation {
    public NearCalculation(String id) {
        super(id);
    }

    @Override
    protected String[] getInputs() {
        return new String[]{"","0.5","0.25"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void calculateOutputValues() {
        float a = inputValues[0];
        float b = inputValues[1];
        float t = inputValues[2];
        outputValues[0] = a >= b-t ? (a <= b+t ? 1 : 0) : 0;
    }
}
