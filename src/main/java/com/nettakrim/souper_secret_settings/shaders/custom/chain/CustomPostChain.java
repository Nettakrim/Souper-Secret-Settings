package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.mclegoman.luminance.client.shaders.interfaces.PostChainInterface;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class CustomPostChain implements PostChainInterface {
    private PostChainInterface stored;
    private ChainGraph chainGraph;

    public PostChainInterface get() {
        if (stored == null || chainGraph.changed) {
            try {
                stored = (PostChainInterface) chainGraph.compile();
            } catch (ShaderManager.CompilationException compilationException) {
                SouperSecretSettingsClient.log("Failed to compile: "+compilationException.getMessage());
            }
        }
        return stored;
    }

    @Override
    public @Nullable List<PostPass> luminance$getPasses(@Nullable Identifier chain) {
        return get().luminance$getPasses(chain);
    }

    @Override
    public void luminance$render(FrameGraphBuilder frameGraphBuilder, int width, int height, PostChain.TargetBundle targetBundle, @Nullable Identifier chain) {
        get().luminance$render(frameGraphBuilder, width, height, targetBundle, chain);
    }

    @Override
    public Set<Identifier> luminance$getCustomChainNames() {
        return get().luminance$getCustomChainNames();
    }

    @Override
    public boolean luminance$usesDepth() {
        return get().luminance$usesDepth();
    }

    @Override
    public boolean luminance$usesImprovedTransparency() {
        return get().luminance$usesImprovedTransparency();
    }

    @Override
    public boolean luminance$usesPersistentBuffers() {
        return get().luminance$usesPersistentBuffers();
    }

    @Override
    public void luminance$setPersistentBufferSource(@Nullable Identifier source) {
        get().luminance$setPersistentBufferSource(source);
    }
}
