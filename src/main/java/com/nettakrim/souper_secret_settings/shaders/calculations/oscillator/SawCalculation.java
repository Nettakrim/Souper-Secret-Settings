package com.nettakrim.souper_secret_settings.shaders.calculations.oscillator;

public class SawCalculation extends PeriodicCalculation {
    public SawCalculation(String id) {
        super(id);
    }

    @Override
    protected float periodicCalculation(float t) {
        return t%1;
    }
}

