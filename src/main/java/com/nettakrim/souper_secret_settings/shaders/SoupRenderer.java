package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.events.Events;
import com.mclegoman.luminance.client.events.Runnables;
import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.ShaderDataloader;
import com.mclegoman.luminance.client.shaders.ShaderRegistry;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class SoupRenderer implements Runnables.GameRender {
    public ArrayList<ShaderStack> shaderStacks;

    public int activeStack;

    public SoupRenderer() {
        clearAll();

        Events.AfterHandRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "rendering"), this);

        Events.OnShaderDataReset.register(Identifier.of(SouperSecretSettingsClient.MODID, "reload"), this::clearAll);

        Events.BeforeShaderRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "before_render"), new OverrideManager.BeforeShaderRender());
        Events.AfterShaderRender.register(Identifier.of(SouperSecretSettingsClient.MODID, "after_render"), new OverrideManager.AfterShaderRender());
    }

    @Override
    public void run(Framebuffer framebuffer, ObjectAllocator objectAllocator) {
        for (ShaderStack stack : shaderStacks) {
            stack.render(framebuffer, objectAllocator);
        }
    }

    public boolean clearShader() {
        getActiveStack().clear();
        return true;
    }

    public boolean addShader(Identifier id, int amount) {
        ShaderRegistry shaderRegistry = getShaderRegistry(id);
        if (shaderRegistry == null) {
            SouperSecretSettingsClient.say("shader.missing", id);
            return false;
        }

        Shader shader = new Shader(shaderRegistry, this::getRenderType);
        int i = 0;
        while (i < amount) {
            getActiveStack().addShaderData(new ShaderData(shader));
            i++;
        }

        return true;
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

    public void clearAll() {
        shaderStacks = new ArrayList<>();
        shaderStacks.add(new ShaderStack());
        activeStack = 0;
    }
}
