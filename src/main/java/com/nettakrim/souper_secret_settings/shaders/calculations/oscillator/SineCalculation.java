package com.nettakrim.souper_secret_settings.shaders.calculations.oscillator;

import net.minecraft.util.math.MathHelper;

public class SineCalculation extends PeriodicCalculation {
    public SineCalculation(String id) {
        super(id);
    }

    @Override
    protected float periodicCalculation(float t) {
        return (MathHelper.cos(t*MathHelper.TAU)+1)/2f;
    }
}

