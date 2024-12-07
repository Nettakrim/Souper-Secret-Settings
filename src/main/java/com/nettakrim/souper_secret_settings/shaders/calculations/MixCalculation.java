package com.nettakrim.souper_secret_settings.shaders.calculations;

public class MixCalculation extends Calculation {
    public MixCalculation(String id) {
        super(id);
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

    @Override
    protected int inputCount() {
        return 3;
    }

    @Override
    protected int outputCount() {
        return 1;
    }
}
