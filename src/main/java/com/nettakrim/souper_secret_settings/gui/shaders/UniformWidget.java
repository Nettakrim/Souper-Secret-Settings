package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.UniformSource;
import com.mclegoman.luminance.client.shaders.uniforms.Uniform;
import com.mclegoman.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.DisplayWidget;
import com.nettakrim.souper_secret_settings.gui.ParameterRemapWidget;
import com.nettakrim.souper_secret_settings.shaders.MixOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ParameterOverrideSource;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UniformWidget extends DisplayWidget<Couple<String,String>> {
    public PassWidget pass;

    public GlUniform uniform;

    public LuminanceUniformOverride override;

    public UniformWidget(PassWidget pass, GlUniform uniform, Text name, int x, int width, ListScreen<?> listScreen) {
        super(uniform.getCount(), name, x, width, listScreen);
        this.pass = pass;
        this.uniform = uniform;
        initValues();
    }

    @Override
    protected List<Couple<String,String>> getChildData() {
        String[] values = new String[uniform.getCount()];

        List<Float> baseValues = getBaseValues();
        for (int i = 0; i < values.length; i++) {
            values[i] = Float.toString(baseValues.get(i));
        }

        String[] defaults = values.clone();

        if (defaults.length == 1) {
            if (((PostEffectPassInterface)pass.postEffectPass).luminance$getUniformOverride(uniform.getName()) instanceof LuminanceUniformOverride o && o.overrideSources.getFirst() instanceof UniformSource uniformSource && uniformSource.getUniform() != null) {
                defaults[0] = uniformSource.getString();
            } else if (LuminanceUniformOverride.sourceFromString(uniform.getName()) instanceof UniformSource uniformSource && uniformSource.getUniform() != null) {
                defaults[0] = uniformSource.getString();
            }
        }

        UniformOverride uniformOverride = pass.shader.shaderData.overrides.get(pass.passIndex).get(uniform.getName());
        if (uniformOverride == null) {
            uniformOverride = ((PostEffectPassInterface)pass.postEffectPass).luminance$getUniformOverride(uniform.getName());
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

        List<Couple<String, String>> data = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {
            data.add(new Couple<>(values[i], defaults[i]));
        }
        return data;
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
    protected ClickableWidget createChildWidget(Couple<String,String> data, int i) {
        String value = data.getFirst();
        String defaultValue = data.getSecond();

        float a = 0;
        float b = 1;
        if (value.startsWith("mix(")) {
            try {
                String[] parts = value.split("/");
                a = Float.parseFloat(parts[0].substring(4));
                b = Float.parseFloat(parts[1]);
                value = parts[2].substring(0, parts[2].length()-1);
            } catch (Exception ignored) {}
        } else {
            if (LuminanceUniformOverride.sourceFromString(value) instanceof UniformSource uniformSource) {
                Uniform override = uniformSource.getUniform();
                if (override != null) {
                    Optional<Float> min = override.getMin();
                    Optional<Float> max = override.getMax();
                    if (min.isPresent() && max.isPresent()) {
                        a = min.get();
                        b = max.get();
                    }
                }
            }
        }

        ParameterRemapWidget widget = new ParameterRemapWidget(getX(), width, width/2, 20, Text.literal("value"), pass.shader.stack, defaultValue);
        widget.setText(value);
        widget.widgetA.setText(Float.toString(a));
        widget.widgetB.setText(Float.toString(b));
        widget.onChange((w) -> onValueChanged(i, w));

        widget.setCursorToStart(false);
        widget.widgetA.setCursorToStart(false);
        widget.widgetB.setCursorToStart(false);

        listScreen.addSelectable(widget.widgetA);
        listScreen.addSelectable(widget.widgetB);
        return widget;
    }

    protected void onValueChanged(int i, ParameterRemapWidget widget) {
        override.overrideSources.set(i, new MixOverrideSource(widget.a, widget.b, ParameterOverrideSource.parameterSourceFromString(widget.value)));
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
