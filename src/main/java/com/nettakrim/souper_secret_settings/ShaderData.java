package com.nettakrim.souper_secret_settings;

import net.minecraft.util.Identifier;

public class ShaderData {
    public String id;
    public Identifier shader;
    public boolean disableVignette;

    public ShaderData(String id, Identifier shader, boolean disableVignette) {
        this.id = id;
        this.shader = shader;
        this.disableVignette = disableVignette;
    }

    public ShaderData(String id, String namespace, boolean disableVignette) {
        this(id, new Identifier(namespace, "shaders/post/"+id+".json"), disableVignette);
    }

    public ShaderData(String id, boolean disableVignette) {
        this(id, new Identifier("shaders/post/"+id+".json"), disableVignette);
    }
}
