package com.nettakrim.souper_secret_settings.shaders.calculations.oscillator;

import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;

public abstract class PeriodicCalculation extends Calculation {
    public PeriodicCalculation(String id) {
        super(id);
    }

    @Override
    protected String[] getInputs() {
        return new String[]{"luminance_gameTime", "1", "0"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void calculateOutputValues() {
        float t = inputValues[0];
        if (inputs[0].getString().contains("gameTime")) {
            t *= 1200;
        }

        outputValues[0] = periodicCalculation(t / inputValues[1] + inputValues[2]);
    }

    protected abstract float periodicCalculation(float t);
}
