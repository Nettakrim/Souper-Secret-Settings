package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.events.Runnables;
import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import net.minecraft.client.gl.PostEffectPass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverrideManager {
    private static ShaderData currentShader;
    private static int currentPassIndex;

    private static final Map<String, UniformOverride> replacedOverrides = new HashMap<>();
    private static final List<String> nullOverrides = new ArrayList<>();

    public static void applyOverrides(ShaderData shaderData) {
        currentShader = shaderData;
        currentPassIndex = 0;
    }

    public static void resetOverrides() {
        currentShader = null;
    }

    public static class BeforeShaderRender implements Runnables.Shader {
        @Override
        public void run(PostEffectPass postEffectPass) {
            if (currentShader == null) {
                return;
            }

            Map<String, UniformOverride> overrides = ((PostEffectPassInterface)postEffectPass).luminance$getUniformOverrides();

            currentShader.overrides.get(currentPassIndex).forEach((uniform, override) -> {
                UniformOverride previous = overrides.put(uniform, override);
                if (previous != null) {
                    replacedOverrides.put(uniform, previous);
                } else {
                    nullOverrides.add(uniform);
                }
            });
        }
    }

    public static class AfterShaderRender implements Runnables.Shader {
        @Override
        public void run(PostEffectPass postEffectPass) {
            if (currentShader == null) {
                return;
            }

            Map<String, UniformOverride> overrides = ((PostEffectPassInterface)postEffectPass).luminance$getUniformOverrides();

            overrides.putAll(replacedOverrides);
            replacedOverrides.clear();

            //TODO: it seems the glUniforms may need to be reset more properly to their original value - they dont seem to do so on their own (this may be a general issue with overrides being removed?)
            nullOverrides.forEach(overrides::remove);
            nullOverrides.clear();

            currentPassIndex++;
        }
    }
}
