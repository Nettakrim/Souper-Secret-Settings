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

            PostEffectPassInterface pass = ((PostEffectPassInterface)postEffectPass);

            currentShader.overrides.get(currentPassIndex).forEach((uniform, override) -> {
                UniformOverride previous = pass.luminance$addUniformOverride(uniform, override);
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

            PostEffectPassInterface pass = ((PostEffectPassInterface)postEffectPass);

            replacedOverrides.forEach(pass::luminance$addUniformOverride);
            replacedOverrides.clear();

            nullOverrides.forEach(pass::luminance$removeUniformOverride);
            nullOverrides.clear();

            currentPassIndex++;
        }
    }
}
