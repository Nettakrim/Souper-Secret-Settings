package com.nettakrim.souper_secret_settings.screen;

import com.mclegoman.luminance.client.shaders.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.ShaderProgramInterface;
import com.mclegoman.luminance.client.shaders.uniforms.UniformOverride;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

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

        //TODO this needs to get the default values, instead of whatever the values happen to be
        List<Float> currentValues = ((ShaderProgramInterface)pass.getProgram()).luminance$getCurrentUniformValues(uniform.getName());
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
