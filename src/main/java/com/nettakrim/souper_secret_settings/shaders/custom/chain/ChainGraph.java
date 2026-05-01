package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nettakrim.souper_secret_settings.gui.custom.ChainCategory;
import com.nettakrim.souper_secret_settings.gui.custom.CreationCategory;
import dev.dannytaylor.luminance.client.data.ClientData;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostChainInterface;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostPassInterface;
import dev.dannytaylor.luminance.client.shaders.interfaces.internal.InternalGpuDeviceInterface;
import dev.dannytaylor.luminance.client.shaders.interfaces.internal.InternalShaderManagerInterface;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ChainGraph extends Graph {
    private PostChain lastCompiled = null;

    public PostChain compile() throws ShaderManager.CompilationException {
        long start = System.nanoTime();
        OrganisedGraph organisedGraph = organise();
        long organised = System.nanoTime();

        AtomicInteger temporaryTargets = new AtomicInteger();
        Supplier<String> uuid = () -> String.valueOf(temporaryTargets.getAndIncrement());

        List<PostChainConfig.Pass> passes = new ArrayList<>();
        Map<Identifier, PostChainConfig.InternalTarget> internalTargets = new HashMap<>();

        for (OrganisedNode organisedNode : organisedGraph.organisedNodes) {
            // store output data, this doesnt need to be done in a seperate loop because organised nodes are always before their usages
            organisedNode.calculateOutputData(uuid);

            if (organisedNode.node instanceof PassNode passNode) {
                passes.add(passNode.getPass(organisedNode));
            } else if (organisedNode.node instanceof WriteTargetNode writeTargetNode) {
                // add a blit when storing a target, this will sometimes waste a pass, but often its needed
                passes.add(writeTargetNode.getPass(organisedNode));
            } else if (organisedNode.node instanceof ReadTargetNode readTargetNode) {
                // add any used persistent targets to the list
                Identifier id = Identifier.parse((String) readTargetNode.outputPorts.getFirst().outputData);
                if (!id.equals(Identifier.withDefaultNamespace("main"))) {
                    internalTargets.put(id, new PostChainConfig.InternalTarget(
                            Optional.empty(),
                            Optional.empty(),
                            true,
                            0
                    ));
                }
            }
        }

        // add all temporary targets, which take the form "0", "1", "2", etc
        for (int i = 0; i < temporaryTargets.get(); i++) {
            internalTargets.put(Identifier.parse(String.valueOf(i)), new PostChainConfig.InternalTarget(
                    Optional.empty(),
                    Optional.empty(),
                    false,
                    0
            ));
        }

        PostChainConfig postChainConfig = new PostChainConfig(internalTargets, passes);

        long constructed = System.nanoTime();

        // this ends up leaking a small amount of memory
        PostChain postChain = PostChain.load(
                postChainConfig,
                ClientData.minecraft.getTextureManager(),
                Set.of(Identifier.withDefaultNamespace("main")),
                Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "graph"),
                ((InternalShaderManagerInterface)ClientData.minecraft.getShaderManager()).luminance$getPostChainProjectionMatrixBuffer()
        );

        long loaded = System.nanoTime();

        SouperSecretSettingsClient.log("Compiled post chain in",getElapsedTime(start, loaded),"- Organising:",getElapsedTime(start,organised),"| Constructing:",getElapsedTime(organised,constructed),"| Loading:",getElapsedTime(constructed, loaded));

        if (lastCompiled != null) {
            closeCaches((PostChainInterface)lastCompiled);
            lastCompiled.close();
        }
        lastCompiled = postChain;

        changed = false;
        return postChain;
    }

    private void closeCaches(PostChainInterface postChainInterface) {
        closeCaches(postChainInterface.luminance$getPasses(null));
        for (Identifier identifier : postChainInterface.luminance$getCustomChainNames()) {
            //noinspection DataFlowIssue
            closeCaches(postChainInterface.luminance$getPasses(identifier));
        }
    }

    private void closeCaches(List<PostPass> passes) {
        InternalGpuDeviceInterface deviceInterface =((InternalGpuDeviceInterface) RenderSystem.getDevice());
        for (PostPass postPass : passes) {
            deviceInterface.luminance$clearPipelineCache(((PostPassInterface)postPass).luminance$getPipeline());
        }
    }

    private String getElapsedTime(long start, long end) {
        long micros = (end-start)/1000;
        return (micros / 1000)+"."+String.format("%3d",(micros % 1000)).replace(' ', '0')+"ms";
    }

    @Override
    public CreationCategory getCreationRoot() {
        return new ChainCategory();
    }
}
