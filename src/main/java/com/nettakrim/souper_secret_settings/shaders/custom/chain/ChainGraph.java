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

public class ChainGraph extends Graph<PostChainInterface> {
    @Override
    public CreationCategory getCreationRoot() {
        return new ChainCategory();
    }

    @Override
    protected String getType() {
        return "chain";
    }

    @Override
    protected PostChainInterface compile() throws ShaderManager.CompilationException {
        long start = System.nanoTime();
        OrganisedGraph organisedGraph = organise();
        long organised = System.nanoTime();

        AtomicInteger temporaryTargets = new AtomicInteger();
        Supplier<String> uuid = () -> String.valueOf(temporaryTargets.getAndIncrement());

        List<PostChainConfig.Pass> passes = new ArrayList<>();
        Map<Identifier, PostChainConfig.InternalTarget> internalTargets = new HashMap<>();

        for (OrganisedNode organisedNode : organisedGraph.organisedNodes) {
            // store output data, this doesn't need to be done in a separate loop because organised nodes are always before their usages
            organisedNode.calculateOutputData(uuid);

            Object mainObject = organisedNode.getMainObject();
            // write target node sometimes needs to return two passes, for alpha merge
            if (mainObject instanceof List<?> objectPasses) {
                for (Object entry : objectPasses) {
                    passes.add((PostChainConfig.Pass)entry);
                }
            }

            // dependencies are handled separately from main object
            if (organisedNode.node instanceof ReadTargetNode readTargetNode) {
                Identifier id = ((TargetInputInfo)readTargetNode.outputPorts.getFirst().outputData).targetId;
                if (!id.equals(Identifier.withDefaultNamespace("main"))) {
                    internalTargets.put(id, new PostChainConfig.InternalTarget(
                            Optional.empty(),
                            Optional.empty(),
                            true,
                            0
                    ));
                }
            }

            // other node types are just needed to calculate their output data
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

        PostChainInterface postChain = (PostChainInterface)PostChain.load(
                postChainConfig,
                ClientData.minecraft.getTextureManager(),
                Set.of(Identifier.withDefaultNamespace("main")),
                Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "graph"),
                ((InternalShaderManagerInterface)ClientData.minecraft.getShaderManager()).luminance$getPostChainProjectionMatrixBuffer()
        );

        long loaded = System.nanoTime();

        SouperSecretSettingsClient.log("Compiled post chain in",getElapsedTime(start, loaded),"- Organising:",getElapsedTime(start,organised),"| Constructing:",getElapsedTime(organised,constructed),"| Loading:",getElapsedTime(constructed, loaded));
        //SouperSecretSettingsClient.log(PostChainConfig.CODEC.encodeStart(JsonOps.INSTANCE, postChainConfig).getOrThrow().toString());

        // only close on successful recompilation
        if (lastCompiled != null) {
            closeCaches(lastCompiled.luminance$getPasses(null));
            for (Identifier identifier : lastCompiled.luminance$getCustomChainNames()) {
                //noinspection DataFlowIssue
                closeCaches(lastCompiled.luminance$getPasses(identifier));
            }
            ((PostChain)lastCompiled).close();
        }

        return postChain;
    }

    private void closeCaches(List<PostPass> passes) {
        InternalGpuDeviceInterface deviceInterface =((InternalGpuDeviceInterface) RenderSystem.getDevice());
        for (PostPass postPass : passes) {
            deviceInterface.luminance$clearPipelineCache(((PostPassInterface)postPass).luminance$getPipeline());
        }
    }
}
