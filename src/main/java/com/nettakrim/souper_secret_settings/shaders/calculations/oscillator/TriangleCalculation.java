package com.nettakrim.souper_secret_settings.shaders.calculations.oscillator;

import net.minecraft.util.math.MathHelper;

public class TriangleCalculation extends PeriodicCalculation {
    public TriangleCalculation(String id) {
        super(id);
    }

    @Override
    protected float periodicCalculation(float t) {
        return MathHelper.abs(MathHelper.floorMod(t,1) - 0.5f)*2f;
    }
}

