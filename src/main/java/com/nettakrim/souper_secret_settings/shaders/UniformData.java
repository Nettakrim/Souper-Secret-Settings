package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.overrides.PerValueOverride;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UniformData {
    public final @Nullable UniformData defaultValue;

    public PerValueOverride override;
    public MapConfig config;

    public UniformData(@Nullable UniformData defaultValue, PerValueOverride override, MapConfig config) {
        this.defaultValue = defaultValue;
        this.override = override;
        this.config = config;
    }

    public boolean isChanged() {
        if (defaultValue == null || !override.getStrings().equals(defaultValue.override.getStrings())) {
            return true;
        }

        if (config.config().isEmpty() && defaultValue.config.config().isEmpty()) {
            return false;
        }

        if (!defaultValue.config.config().keySet().equals(config.config().keySet())) {
            return true;
        }

        for (String s : defaultValue.config.config().keySet()) {
            List<Object> defaultObjects = defaultValue.config.config().get(s);
            List<Object> currentObjects = config.config().get(s);
            if (defaultObjects.size() != currentObjects.size()) {
                return true;
            }

            for (int i = 0; i < defaultObjects.size(); i++) {
                Object defaultObject = defaultObjects.get(i);
                Object currentObject = currentObjects.get(i);

                if (defaultObject.equals(currentObject)) {
                    continue;
                }

                if (defaultObject instanceof Number defaultNumber && currentObject instanceof Number currentNumber) {
                    if (defaultNumber.doubleValue() != currentNumber.doubleValue()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
