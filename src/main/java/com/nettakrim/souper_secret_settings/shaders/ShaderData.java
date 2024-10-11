package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import com.mclegoman.luminance.mixin.client.shaders.PostEffectProcessorAccessor;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ObjectAllocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShaderData {
    public Shader shader;

    public final List<Map<String, UniformOverride>> overrides;

    public ShaderData(Shader shader) {
        this.shader = shader;
        this.shader.setPostProcessor();

        int size = ((PostEffectProcessorAccessor)this.shader.getPostProcessor()).getPasses().size();
        this.overrides = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            //default initial capacity is 16, the majority of shaders do not have nearly that many uniforms
            this.overrides.add(new HashMap<>(4));
        }
    }

    public void render(Framebuffer framebuffer, ObjectAllocator objectAllocator) {
        OverrideManager.applyOverrides(this);
        Shaders.renderUsingAllocator(shader.getPostProcessor(), framebuffer, objectAllocator);
        OverrideManager.resetOverrides();
    }
}
