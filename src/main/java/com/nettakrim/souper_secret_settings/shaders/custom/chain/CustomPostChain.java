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
    private final ChainGraph chainGraph;

    public CustomPostChain() {
        chainGraph = new ChainGraph();

        StoreTargetNode storeTargetNode = new StoreTargetNode();
        ReadTargetNode readTargetNode = new ReadTargetNode();
        storeTargetNode.inputPorts.get(1).docked = readTargetNode;
        chainGraph.nodes.add(storeTargetNode);
        chainGraph.nodes.add(readTargetNode);

        chainGraph.changed = true;
    }

    public PostChainInterface get() {
        if (stored == null || chainGraph.changed) {
            try {
                long start = System.nanoTime();
                stored = (PostChainInterface) chainGraph.compile();
                long micros = (System.nanoTime()-start) / 1000;
                SouperSecretSettingsClient.log("Compiled post chain in "+(micros / 1000)+"."+String.format("%3d",(micros % 1000)).replace(' ', '0')+"ms");
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
