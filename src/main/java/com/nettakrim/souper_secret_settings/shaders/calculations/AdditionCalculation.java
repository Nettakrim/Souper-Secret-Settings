package com.nettakrim.souper_secret_settings.shaders.calculations;

import net.minecraft.util.math.MathHelper;

public class AdditionCalculation extends Calculation {
    @Override
    protected void calculateOutputValues() {
        outputValues[0] = MathHelper.lerp(inputValues[2], inputValues[0], inputValues[1]);
    }

    @Override
    protected int inputCount() {
        return 3;
    }

    @Override
    protected int outputCount() {
        return 1;
    }
}
