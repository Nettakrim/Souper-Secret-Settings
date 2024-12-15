package com.nettakrim.souper_secret_settings.shaders.calculations.logic;

import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;

public class CompareCalculation extends Calculation {
    public CompareCalculation(String id) {
        super(id);
    }

    @Override
    protected String[] getInputs() {
        return new String[]{"","","0","1"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void calculateOutputValues() {
        outputValues[0] = inputValues[0] > inputValues[1] ? inputValues[2] : inputValues[3];
    }
}
