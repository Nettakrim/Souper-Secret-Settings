package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.ShaderDataloader;
import com.mclegoman.luminance.client.shaders.ShaderRegistry;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Objects;

public class SoupRenderer {
    public ArrayList<ShaderStack> shaderStacks;

    public int activeStack = 0;

    public SoupRenderer() {
        shaderStacks = new ArrayList<>();
        shaderStacks.add(new ShaderStack());
    }



    public boolean setShader(Identifier id) {
        return stackShader(id, 0);
    }

    public boolean stackShader(Identifier id, int stack) {
        ShaderRegistry shaderRegistry = getShaderRegistry(id);
        if (shaderRegistry == null) {
            SouperSecretSettingsClient.say("shader.missing", id);
            return false;
        }

        getActiveStack().addShaderData(getShaderData(shaderRegistry));
        //stackShaderData(shaderData, stack);
        return true;
    }

    public ShaderData getShaderData(ShaderRegistry shaderRegistry) {
        Shaders.get(Shaders.getShaderIndex(shaderRegistry.getNamespace(), shaderRegistry.getKey()));
        return null;
    }

    public ShaderRegistry getShaderRegistry(Identifier identifier) {
        for (ShaderRegistry shaderRegistry : ShaderDataloader.registry) {
            if (Objects.equals(shaderRegistry.getNamespace(), identifier.getNamespace()) && Objects.equals(shaderRegistry.getKey(), identifier.getPath())) {
                return shaderRegistry;
            }
        }
        return null;
    }

    public ShaderStack getActiveStack() {
        return shaderStacks.get(activeStack);
    }
}
