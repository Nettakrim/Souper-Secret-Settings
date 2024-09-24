package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.events.Events;
import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.ShaderDataloader;
import com.mclegoman.luminance.client.shaders.ShaderRegistry;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class SoupRenderer {
    public ArrayList<ShaderStack> shaderStacks;

    public int activeStack = 0;

    public SoupRenderer() {
        shaderStacks = new ArrayList<>();
        shaderStacks.add(new ShaderStack());

        Events.AfterHandRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "rendering"), this::render);
    }

    public void render() {
        for (ShaderStack stack : shaderStacks) {
            stack.render();
        }
    }

    public boolean setShader(Identifier id) {
        return stackShader(id, 0);
    }

    public boolean stackShader(Identifier id, int stack) {
        //temp clear
        if (id.getPath().equals("clear")) {
            getActiveStack().clear();
            return true;
        }

        ShaderRegistry shaderRegistry = getShaderRegistry(id);
        if (shaderRegistry == null) {
            SouperSecretSettingsClient.say("shader.missing", id);
            return false;
        }

        //needs to do stack count
        getActiveStack().addShaderData(getShaderData(shaderRegistry));
        return true;
    }

    public ShaderData getShaderData(ShaderRegistry shaderRegistry) {
        Shader shader = new Shader(shaderRegistry, this::getRenderType);
        return new ShaderData(shader);
    }

    private Shader.RenderType getRenderType() {
        return Shader.RenderType.WORLD;
    }

    public ShaderRegistry getShaderRegistry(Identifier identifier) {
        for (ShaderRegistry shaderRegistry : ShaderDataloader.registry) {
            if (shaderRegistry.getID().equals(identifier)) {
                return shaderRegistry;
            }
        }
        return null;
    }

    public ShaderStack getActiveStack() {
        return shaderStacks.get(activeStack);
    }
}
