package com.nettakrim.souper_secret_settings.shaders;

import net.minecraft.util.Identifier;

public class ShaderData {
    public final String id;
    public final Identifier shader;
    public final boolean soupFriendly;

    public ShaderData(String id, Identifier shader, boolean soupFriendly) {
        this.id = id;
        this.shader = shader;
        this.soupFriendly = soupFriendly;
    }

    public ShaderData(String namespace, String id, boolean soupFriendly, boolean isLayerEffect) {
        this(id, Identifier.of(namespace, "shaders/"+(isLayerEffect ? "layer_effects" : "post")+"/"+id+".json"), soupFriendly);
    }
}
