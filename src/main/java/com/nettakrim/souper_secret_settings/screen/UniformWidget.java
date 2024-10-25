package com.nettakrim.souper_secret_settings.screen;

import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.MixOverrideSource;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPipeline;
import net.minecraft.client.gui.widget.ClickableWidget;
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
        initValues();
    }

    @Override
    protected String[] getChildData() {
        String[] values = new String[uniform.getCount()];

        List<Float> baseValues = getBaseValues();
        for (int i = 0; i < values.length; i++) {
            values[i] = Float.toString(baseValues.get(i));
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
    protected ClickableWidget createChildWidget(String data, int i) {
        float a = 0;
        float b = 1;
        if (data.startsWith("mix(")) {
            try {
                String[] parts = data.split("/");
                a = Float.parseFloat(parts[0].substring(4));
                b = Float.parseFloat(parts[1]);
                data = parts[2].substring(0, parts[2].length()-1);
            } catch (Exception ignored) {}
        }

        UniformParameterWidget widget = new UniformParameterWidget(SouperSecretSettingsClient.client.textRenderer, getX(), width, width/2, 20, Text.literal("value"), data);
        widget.widgetA.setText(Float.toString(a));
        widget.widgetB.setText(Float.toString(b));
        widget.onChange((w) -> onValueChanged(i, w));

        widget.setCursorToStart(false);
        widget.widgetA.setCursorToStart(false);
        widget.widgetB.setCursorToStart(false);

        collapseScreen.addSelectable(widget.widgetA);
        collapseScreen.addSelectable(widget.widgetB);
        return widget;
    }

    protected void onValueChanged(int i, UniformParameterWidget widget) {
        override.overrideSources.set(i, new MixOverrideSource(widget.a, widget.b, LuminanceUniformOverride.sourceFromString(widget.value)));
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
