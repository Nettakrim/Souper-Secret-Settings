package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import dev.dannytaylor.luminance.client.data.ClientData;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostChainInterface;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostPassInterface;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.nettakrim.souper_secret_settings.shaders.custom.Wire;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.List;
import java.util.Set;

public class CustomPostChain implements PostChainInterface {
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

        chainGraph.makeChange(true);
    }

    @Override
    public @Nullable List<PostPass> luminance$getPasses(@Nullable Identifier chain) {
        return chainGraph.getOrCompile().luminance$getPasses(chain);
    }

    @Override
    public void luminance$render(FrameGraphBuilder frameGraphBuilder, int width, int height, PostChain.TargetBundle targetBundle, @Nullable Identifier chain) {
        chainGraph.getOrCompile().luminance$render(frameGraphBuilder, width, height, targetBundle, chain);
    }

    @Override
    public Set<Identifier> luminance$getCustomChainNames() {
        return chainGraph.getOrCompile().luminance$getCustomChainNames();
    }

    @Override
    public boolean luminance$usesDepth() {
        return chainGraph.getOrCompile().luminance$usesDepth();
    }

    @Override
    public boolean luminance$usesImprovedTransparency() {
        return chainGraph.getOrCompile().luminance$usesImprovedTransparency();
    }

    @Override
    public boolean luminance$usesPersistentBuffers() {
        return chainGraph.getOrCompile().luminance$usesPersistentBuffers();
    }

    @Override
    public void luminance$setPersistentBufferSource(@Nullable Identifier source) {
        chainGraph.getOrCompile().luminance$setPersistentBufferSource(source);
    }
}
