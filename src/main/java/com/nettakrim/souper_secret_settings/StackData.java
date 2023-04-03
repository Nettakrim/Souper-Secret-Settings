package com.nettakrim.souper_secret_settings;

import net.minecraft.client.gl.PostEffectProcessor;

public class StackData {
    public PostEffectProcessor processor;
    public ShaderData data;

    public StackData(PostEffectProcessor processor, ShaderData data) {
        this.processor = processor;
        this.data = data;
    }
}
