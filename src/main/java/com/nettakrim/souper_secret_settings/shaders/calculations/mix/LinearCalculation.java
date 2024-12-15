package com.nettakrim.souper_secret_settings.shaders.calculations.mix;

import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;

public class LinearCalculation extends Calculation {
    public LinearCalculation(String id) {
        super(id);
    }

    @Override
    protected String[] getInputs() {
        return new String[]{"0","1","0.5"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void calculateOutputValues() {
        // addition generalised is the same as lerp
        // it also generalises as a form of multiplication when a=x, c=y
        float a = inputValues[0];
        float b = inputValues[1];
        float c = inputValues[2];
        outputValues[0] = a + c*(b-a);
    }
}
