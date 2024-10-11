package com.nettakrim.souper_secret_settings.screen;

import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UniformWidget extends ParameterWidget {
    public PassWidget pass;

    public GlUniform uniform;

    public LuminanceUniformOverride override;

    public UniformWidget(PassWidget pass, GlUniform uniform, Text name, int x, int width, CollapseScreen collapseScreen) {
        super(uniform.getCount(), name, x, width, collapseScreen);
        this.pass = pass;
        this.uniform = uniform;
        initValues(collapseScreen);
    }

    @Override
    protected String[] getValues() {
        String[] values = new String[uniform.getCount()];

        List<Float> baseValues = getBaseValues();
        for (int i = 0; i < values.length; i++) {
            values[i] = Float.toString(baseValues.get(i));
        }

        UniformOverride uniformOverride = pass.shader.shaderData.overrides.get(pass.passIndex).get(uniform.getName());
        if (uniformOverride == null) {
            uniformOverride = ((PostEffectPassInterface)pass.postEffectPass).luminance$getUniformOverrides().get(uniform.getName());
        }

        if (uniformOverride instanceof LuminanceUniformOverride luminanceUniformOverride) {
            for (int i = 0; i < values.length; i++) {
                String s = luminanceUniformOverride.getStrings().get(i);
                if (s != null) {
                    values[i] = s;
                }
            }
            override = luminanceUniformOverride;
        } else {
            List<String> overrideStrings = new ArrayList<>(values.length);
            for (int i = 0; i < values.length; i++) {
                overrideStrings.add(null);
            }
            override = new LuminanceUniformOverride(overrideStrings);
        }

        return values;
    }

    protected List<Float> getBaseValues() {
        for (PostEffectPipeline.Uniform u : ((PostEffectPassInterface)pass.postEffectPass).luminance$getUniforms()) {
            if (u.name().equals(uniform.getName())) {
                return u.values();
            }
        }
        return Objects.requireNonNull(pass.postEffectPass.getProgram().getUniformDefinition(uniform.getName())).values();
    }

    @Override
    protected void onValueChanged(int i, String s) {
        super.onValueChanged(i, s);
        override.overrideSources.set(i, LuminanceUniformOverride.sourceFromString(s));
        pass.shader.shaderData.overrides.get(pass.passIndex).put(uniform.getName(), override);
    }

    @Override
    protected List<Float> getDisplayFloats() {
        List<Float> display = override.getOverride();
        List<Float> base = getBaseValues();
        for (int i = 0; i < display.size(); i++) {
            if (display.get(i) == null) {
                display.set(i, base.get(i));
            }
        }
        return display;
    }
}
