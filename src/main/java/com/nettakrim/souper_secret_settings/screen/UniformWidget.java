package com.nettakrim.souper_secret_settings.screen;

import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UniformWidget extends ParameterWidget {
    public PostEffectPass pass;
    public GlUniform uniform;

    public LuminanceUniformOverride override;

    public UniformWidget(PostEffectPass pass, GlUniform uniform, Text name, int x, int width, CollapseScreen collapseScreen) {
        super(uniform.getCount(), name, x, width, collapseScreen);
        this.pass = pass;
        this.uniform = uniform;
        initValues(collapseScreen);
    }

    @Override
    protected String[] getValues() {
        String[] values = new String[uniform.getCount()];

        List<Float> currentValues = null;
        for (PostEffectPipeline.Uniform u : ((PostEffectPassInterface)pass).luminance$getUniforms()) {
            if (u.name().equals(uniform.getName())) {
                currentValues = u.values();
                break;
            }
        }
        if (currentValues == null) {
            currentValues = Objects.requireNonNull(pass.getProgram().getUniformDefinition(uniform.getName())).values();
        }

        for (int i = 0; i < values.length; i++) {
            values[i] = Float.toString(currentValues.get(i));
        }

        UniformOverride uniformOverride = ((PostEffectPassInterface)pass).luminance$getUniformOverrides().get(uniform.getName());
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

    @Override
    protected void onValueChanged(int i, String s) {
        super.onValueChanged(i, s);
        override.overrideSources.set(i, LuminanceUniformOverride.sourceFromString(s));
        ((PostEffectPassInterface)pass).luminance$getUniformOverrides().put(uniform.getName(), override);
    }
}
