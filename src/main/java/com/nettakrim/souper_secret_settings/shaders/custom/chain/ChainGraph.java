package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.gui.custom.ChainCategory;
import com.nettakrim.souper_secret_settings.gui.custom.CreationCategory;
import dev.dannytaylor.luminance.client.data.ClientData;
import dev.dannytaylor.luminance.client.shaders.CustomPassData;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostChainInterface;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostPassInterface;
import dev.dannytaylor.luminance.mixin.client.shaders.ShaderManagerAccessor;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ChainGraph extends Graph {
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

        PostChain postChain = PostChain.load(
                postChainConfig,
                ClientData.minecraft.getTextureManager(),
                Set.of(Identifier.withDefaultNamespace("main")),
                Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "graph"),
                ((ShaderManagerAccessor)ClientData.minecraft.getShaderManager()).getPostChainProjectionMatrixBuffer()
        );

        PostChainInterface postChainInterface = (PostChainInterface)postChain;
        postChainInterface.luminance$getPasses(null).forEach(postPass -> ((PostPassInterface)postPass).luminance$putCustomData(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "compiled"), new CustomPassData() {
            @Override
            public CustomPassData copy() {
                return this;
            }
        }));

        long loaded = System.nanoTime();

        SouperSecretSettingsClient.log("Compiled post chain in",getElapsedTime(start, loaded),"- Organising:",getElapsedTime(start,organised),"| Constructing:",getElapsedTime(organised,constructed),"| Loading:",getElapsedTime(constructed, loaded));

        changed = false;
        return postChain;
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
