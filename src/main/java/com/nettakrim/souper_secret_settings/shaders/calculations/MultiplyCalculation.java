package com.nettakrim.souper_secret_settings.shaders.calculations;

public class MultiplyCalculation extends Calculation {
    public MultiplyCalculation(String id) {
        super(id);
    }

    @Override
    protected void calculateOutputValues() {
        // c changes whether a and c multiplies towards 0, or towards 1, same as lerp at c = 0.5, interestingly symmetrical
        float a = inputValues[0];
        float b = inputValues[1];
        float c = inputValues[2];
        outputValues[0] = -2*a*b*c + a*b + b*c + c*a;
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
