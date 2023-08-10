package com.nettakrim.souper_secret_settings.shaders;

import com.google.gson.JsonSyntaxException;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;
import java.util.List;

public class PostLayerEffect extends AbstractLayerEffect {
    protected final LayeredPostEffectProcessor layeredPostEffectProcessor;

    public PostLayerEffect(TextureManager textureManager, ResourceManager resourceManager, Framebuffer framebuffer, ShaderData shaderData) throws IOException, JsonSyntaxException {
        super(shaderData);
        layeredPostEffectProcessor = new LayeredPostEffectProcessor(textureManager, resourceManager, framebuffer, shaderData.shader);
    }

    public void resize(int width, int height) {
        layeredPostEffectProcessor.setupDimensions(width, height);
    }

    public void beforeStackRender(List<StackData> postProcessorStack, float tickDelta) {
        layeredPostEffectProcessor.updateTime(tickDelta);
        layeredPostEffectProcessor.beforeStackRender();
    }

    public void afterStackRender(List<StackData> postProcessorStack, float tickDelta) {
        layeredPostEffectProcessor.afterStackRender();
    }

    public void beforeShaderRender(StackData stackData, float tickDelta) {
        layeredPostEffectProcessor.beforeShaderRender();
    }

    public void afterShaderRender(StackData stackData, float tickDelta) {
        layeredPostEffectProcessor.afterShaderRender();
    }
}
