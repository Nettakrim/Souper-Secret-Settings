package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.Shaders;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ObjectAllocator;

public class ShaderData {
    public Shader shader;

    public ShaderData(Shader shader) {
        this.shader = shader;
        this.shader.setPostProcessor();
    }

    public void render(Framebuffer framebuffer, ObjectAllocator objectAllocator) {
        Shaders.renderUsingAllocator(shader.getPostProcessor(), framebuffer, objectAllocator);
    }
}
