package com.nettakrim.souper_secret_settings.shaders.calculations.oscillator;

import net.minecraft.util.math.MathHelper;

public class SawCalculation extends PeriodicCalculation {
    public SawCalculation(String id) {
        super(id);
    }

    @Override
    protected float periodicCalculation(float t) {
        return MathHelper.floorMod(t,1);
    }
}

