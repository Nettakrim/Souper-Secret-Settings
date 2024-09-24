package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.Shaders;

public class ShaderData {
    public Shader shader;

    public ShaderData(Shader shader) {
        this.shader = shader;
        this.shader.setPostProcessor();
    }

    public void render() {
        Shaders.render(shader.getPostProcessor());
    }
}
