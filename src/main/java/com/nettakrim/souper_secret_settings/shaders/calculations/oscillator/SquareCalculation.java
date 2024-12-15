package com.nettakrim.souper_secret_settings.shaders.calculations.oscillator;

public class SquareCalculation extends PeriodicCalculation {
    public SquareCalculation(String id) {
        super(id);
    }

    @Override
    protected String[] getInputs() {
        return new String[]{"luminance_gameTime", "1", "0", "0.5"};
    }

    @Override
    protected float periodicCalculation(float t) {
        return (t%1) > inputValues[3] ? 0 : 1;
    }
}

