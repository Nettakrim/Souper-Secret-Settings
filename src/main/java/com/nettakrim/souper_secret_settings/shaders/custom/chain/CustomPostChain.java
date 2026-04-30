package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import dev.dannytaylor.luminance.client.data.ClientData;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostChainInterface;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostPassInterface;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.custom.Wire;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.List;
import java.util.Set;

public class CustomPostChain implements PostChainInterface {
    private PostChainInterface stored;
    public final ChainGraph chainGraph;

    public CustomPostChain() {
        chainGraph = new ChainGraph();

        PostChain reference = ClientData.minecraft.getShaderManager().getPostChain(Identifier.withDefaultNamespace("invert"), LevelTargetBundle.SORTING_TARGETS);
        assert reference != null;

        ReadTargetNode readTargetNode = new ReadTargetNode();
        PassNode passNode = new PassNode((PostPassInterface)((PostChainInterface)reference).luminance$getPasses(null).getFirst());
        WriteTargetNode writeTargetNode = new WriteTargetNode();
        chainGraph.nodes.add(readTargetNode);
        chainGraph.nodes.add(passNode);
        chainGraph.nodes.add(writeTargetNode);

        Wire a2b = new Wire();
        a2b.source = readTargetNode.outputPorts.getFirst();
        a2b.destination = passNode.inputPorts.getFirst();
        chainGraph.addWire(a2b);

        Wire b2c = new Wire();
        b2c.source = passNode.outputPorts.getFirst();
        b2c.destination = writeTargetNode.inputPorts.getFirst();
        chainGraph.addWire(b2c);

        readTargetNode.position = new Vector2i(-170, -40);
        passNode.position = new Vector2i(-50, -40);
        writeTargetNode.position = new Vector2i(70, -40);

        chainGraph.changed = true;
    }

    public PostChainInterface get() {
        if (stored == null || chainGraph.changed) {
            try {
                stored = (PostChainInterface) chainGraph.compile();
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
