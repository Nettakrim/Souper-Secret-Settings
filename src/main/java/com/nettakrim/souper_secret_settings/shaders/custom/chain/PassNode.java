package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.mclegoman.luminance.client.shaders.interfaces.PostPassInterface;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PassNode extends Node {
    private final PostPassInterface postPassInterface;

    public PassNode(PostPassInterface postPassInterface) {
        super();
        this.postPassInterface = postPassInterface;
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        for (String input : postPassInterface.luminance$getPipeline().getSamplers()) {
            addInput(input, PortType.TARGET);
        }
        addOutput("output", PortType.TARGET);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    public PostChainConfig.Pass getPass(Graph.OrganisedNode organisedNode) {
        List<PostChainConfig.Input> inputs = new ArrayList<>();

        // TODO: to support texture sampler, this will need to be changed somewhat, eg by storing instances of PostChainConfig.Input in the output data (minus the sampler name)
        for (int i = 0; i < inputPorts.size(); i++) {
            inputs.add(new PostChainConfig.TargetInput(
                    inputPorts.get(i).name,
                    Identifier.parse((String)organisedNode.inputSources[i].getPort().outputData),
                    false,
                    false
            ));
        }

        //TODO: uniforms, shaders
        return new PostChainConfig.Pass(
                Identifier.parse("vertex"),
                Identifier.parse("fragment"),
                inputs,
                Identifier.parse((String)outputPorts.getFirst().outputData),
                Map.of()
        );
    }
}
