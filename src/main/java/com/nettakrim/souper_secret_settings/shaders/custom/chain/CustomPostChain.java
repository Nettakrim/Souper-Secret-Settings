package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.client.shaders.interfaces.PostChainInterface;
import com.mclegoman.luminance.client.shaders.interfaces.PostPassInterface;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.custom.Wire;
import net.minecraft.client.renderer.LevelTargetBundle;
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

        PostChain reference = ClientData.minecraft.getShaderManager().getPostChain(Identifier.withDefaultNamespace("invert"), LevelTargetBundle.SORTING_TARGETS);
        assert reference != null;

        ReadTargetNode readTargetNode = new ReadTargetNode();
        PassNode passNode = new PassNode((PostPassInterface)((PostChainInterface)reference).luminance$getPasses(null).getFirst());
        StoreTargetNode storeTargetNode = new StoreTargetNode();
        chainGraph.nodes.add(readTargetNode);
        chainGraph.nodes.add(passNode);
        chainGraph.nodes.add(storeTargetNode);

        Wire a2b = new Wire();
        a2b.source = readTargetNode.outputPorts.getFirst();
        a2b.destination = passNode.inputPorts.getFirst();
        chainGraph.wires.add(a2b);

        Wire b2c = new Wire();
        b2c.source = passNode.outputPorts.getFirst();
        b2c.destination = storeTargetNode.inputPorts.get(1);
        chainGraph.wires.add(b2c);

        chainGraph.changed = true;
    }

    public PostChainInterface get() {
        if (stored == null || chainGraph.changed) {
            try {
                long start = System.nanoTime();
                stored = (PostChainInterface) chainGraph.compile();
                long micros = (System.nanoTime()-start) / 1000;
                SouperSecretSettingsClient.log("Compiled post chain in",(micros / 1000)+"."+String.format("%3d",(micros % 1000)).replace(' ', '0'),"ms");
            } catch (ShaderManager.CompilationException compilationException) {
                SouperSecretSettingsClient.log("Failed to compile:",compilationException.getMessage());
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
