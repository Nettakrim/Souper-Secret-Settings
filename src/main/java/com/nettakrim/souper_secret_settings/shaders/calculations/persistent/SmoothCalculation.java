package com.nettakrim.souper_secret_settings.shaders.calculations.persistent;

import dev.dannytaylor.luminance.client.shaders.ShaderTime;
import dev.dannytaylor.luminance.client.shaders.Uniforms;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.util.Mth;

public class SmoothCalculation extends Calculation {
    public SmoothCalculation(String id) {
        super(id);
    }

    float current;

    @Override
    protected String[] getInputs() {
        return new String[]{"", String.valueOf(ShaderTime.defaultSpeed)};
    }

    @Override
    protected String[] getInputNames() {
        return new String[] {"input", "speed"};
    }

    @Override
    protected String[] getOutputs() {
        return new String[]{""};
    }

    @Override
    protected void calculateOutputValues() {
        current = Mth.lerp(Uniforms.shaderTime.getExpDeltaTime(inputValues[1]), current, inputValues[0]);
        outputValues[0] = current;
    }
}
