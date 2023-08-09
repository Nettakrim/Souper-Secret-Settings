package com.nettakrim.souper_secret_settings.shaders;

import net.minecraft.util.Identifier;

public class ShaderData {
    public final String id;
    public final Identifier shader;

    public ShaderData(String id, Identifier shader) {
        this.id = id;
        this.shader = shader;
    }

    public ShaderData(String namespace, String id, boolean isLayerEffect) {
        this(id, new Identifier(namespace, "shaders/"+(isLayerEffect ? "layer_effects" : "post")+"/"+id+".json"));
    }
}
