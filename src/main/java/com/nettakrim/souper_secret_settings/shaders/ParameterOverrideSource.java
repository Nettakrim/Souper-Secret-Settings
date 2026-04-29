package com.nettakrim.souper_secret_settings.shaders;

import dev.dannytaylor.luminance.client.shaders.ShaderTime;
import dev.dannytaylor.luminance.client.shaders.overrides.NullSource;
import dev.dannytaylor.luminance.client.shaders.overrides.PerValueOverride;
import dev.dannytaylor.luminance.client.shaders.overrides.OverrideSource;
import dev.dannytaylor.luminance.client.shaders.overrides.UniformSource;
import dev.dannytaylor.luminance.client.shaders.uniforms.config.DefaultableConfig;
import dev.dannytaylor.luminance.client.shaders.uniforms.config.MapConfig;
import dev.dannytaylor.luminance.client.shaders.uniforms.config.UniformConfig;
import com.mojang.blaze3d.platform.InputConstants;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.mixin.KeyAccessor;
import net.minecraft.client.KeyMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterOverrideSource implements OverrideSource {
    private final OverrideSource source;
    private Float lastValue = 0f;

    private static final Map<InputConstants.Key, KeyMapping> keyBindingCache = new HashMap<>();
    private static long keyBindingUUID;

    public ParameterOverrideSource(OverrideSource source) {
        this.source = source;
    }

    @Override
    public Float get(UniformConfig uniformConfig, ShaderTime shaderTime) {
        ShaderLayer layer = ShaderLayer.getRenderingLayer();
        if (layer == null) {
            layer = SouperSecretSettingsClient.soupRenderer.activeLayer;
        }

        if (layer != null) {
            String parameter = source.getString();

            boolean replaced = false;

            if (!parameter.isEmpty()) {
                if (layer.parameterValues.containsKey(parameter)) {
                    lastValue = layer.parameterValues.get(parameter);
                    replaced = true;
                } else {
                    InputConstants.Key foundKey = KeyAccessor.getNameMap().get(parameter);
                    if (foundKey != null) {
                        KeyMapping mapping = keyBindingCache.computeIfAbsent(foundKey, (key) -> new KeyMapping("soup.key"+(keyBindingUUID++), key.getValue(), KeyMapping.Category.MISC));
                        lastValue = mapping.isDown() ? 1f : 0f;
                        replaced = true;
                    }
                }
            }

            if (replaced) {
                List<Object> range = uniformConfig.getObjects("range");
                if (range != null && range.size() >= 2) {
                    lastValue = UniformSource.remapRange(lastValue, 0f, 1f, range.get(0), range.get(1));
                }
            } else {
                lastValue = source.get(uniformConfig, shaderTime);
            }
        }

        return lastValue;
    }

    @Override
    public String getString() {
        return source.getString();
    }

    @Override
    public UniformConfig getTemplateConfig() {
        UniformConfig config = source.getTemplateConfig();
        return new DefaultableConfig(config, template);
    }

    private static final UniformConfig template = new MapConfig(Map.of("range", new ArrayList<>(List.of(0.0f, 1.0f))));

    public static OverrideSource parameterSourceFromString(String s) {
        OverrideSource overrideSource = PerValueOverride.sourceFromString(s);
        if (overrideSource instanceof UniformSource || overrideSource instanceof NullSource) {
            return new ParameterOverrideSource(overrideSource);
        }
        return overrideSource;
    }
}
