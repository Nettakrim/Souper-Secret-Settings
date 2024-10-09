package com.nettakrim.souper_secret_settings.screen;

import com.mclegoman.luminance.client.shaders.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.uniforms.UniformOverride;
import com.mclegoman.luminance.mixin.client.shaders.PostEffectPassAccessor;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UniformWidget extends ParameterWidget implements UniformOverride {
    public PostEffectPass pass;
    public GlUniform uniform;

    //TODO: this needs to be handled persistently instead of as part of the widget
    protected List<Float> override;

    public UniformWidget(PostEffectPass pass, GlUniform uniform, Text name, int x, int width, CollapseScreen collapseScreen) {
        super(uniform.getCount(), name, x, width, collapseScreen);
        this.pass = pass;
        this.uniform = uniform;

        this.override = new ArrayList<>(uniform.getCount());
        for (int i = 0; i < uniform.getCount(); i++) {
            override.add(null);
        }

        initValues(collapseScreen);
    }

    @Override
    protected String[] getValues() {
        String[] values = new String[uniform.getCount()];

        //TODO: check overrides first
        //TODO: if uniform is dynamic itll need to be different too (this will end up needing to be done by luminance anyway for resource-side overrides, so that method should help)
        List<Float> currentValues = null;
        for (PostEffectPipeline.Uniform u : ((PostEffectPassAccessor)pass).getUniforms()) {
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

        return values;
    }

    @Override
    public List<Float> getOverride() {
        return override;
    }

    @Override
    protected void onValueChanged(int i, String s) {
        super.onValueChanged(i, s);
        updateUniform();
    }

    protected void updateOverrides() {
        for (int i = 0; i < override.size(); i++) {
            try {
                override.set(i, Float.parseFloat(values[i]));
            } catch (Exception ignored) {
                //TODO: make it fallback to dynamic uniforms
            }
        }
    }

    protected void updateUniform() {
        updateOverrides();

        ((PostEffectPassInterface)pass).luminance$getUniformOverrides().put(uniform.getName(), this);
    }
}
