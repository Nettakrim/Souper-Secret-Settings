package com.nettakrim.souper_secret_settings;

import net.minecraft.util.Identifier;

public class ShaderData {
    public final String id;
    public final Identifier shader;

    public ShaderData(String id, Identifier shader) {
        this.id = id;
        this.shader = shader;
    }

    public ShaderData(String namespace, String id) {
        this(id, new Identifier(namespace, "shaders/post/"+id+".json"));
    }
}
