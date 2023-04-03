package com.nettakrim.souper_secret_settings;

import net.minecraft.util.Identifier;

public class ShaderData {
    public String id;
    public Identifier shader;

    public ShaderData(String id, Identifier shader) {
        this.id = id;
        this.shader = shader;
    }

    public ShaderData(String id, String namespace) {
        this(id, new Identifier(namespace, "shaders/post/"+id+".json"));
    }

    public ShaderData(String id) {
        this(id, new Identifier("shaders/post/"+id+".json"));
    }
}
