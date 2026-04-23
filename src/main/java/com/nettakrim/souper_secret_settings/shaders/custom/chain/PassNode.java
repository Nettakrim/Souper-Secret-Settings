package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.mclegoman.luminance.client.shaders.UniformBlock;
import com.mclegoman.luminance.client.shaders.interfaces.PostPassInterface;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PassNode extends Node {
    private final PostPassInterface postPassInterface;

    public PassNode(PostPassInterface postPassInterface) {
        this.postPassInterface = postPassInterface;
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        for (PostPass.Input input : postPassInterface.luminance$inputs()) {
            addInput(input.samplerName(), PortType.TARGET);
        }

        for (Map.Entry<String, UniformBlock> blockEntry : postPassInterface.luminance$getUniformBlocks().entrySet()) {
            addInput(blockEntry.getKey(), PortType.BLOCK).docked = new UniformBlockNode(blockEntry.getValue());
        }

        addOutput("output", PortType.TARGET);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    public PostChainConfig.Pass getPass(Graph.OrganisedNode organisedNode) {
        List<PostChainConfig.Input> inputs = new ArrayList<>();

        int samplers = postPassInterface.luminance$getPipeline().getSamplers().size();
        // TODO: to support texture sampler, this will need to be changed somewhat, eg by storing instances of PostChainConfig.Input in the output data (minus the sampler name)
        for (int i = 0; i < samplers; i++) {
            inputs.add(new PostChainConfig.TargetInput(
                    inputPorts.get(i).name,
                    Identifier.parse((String)organisedNode.inputSources[i].getPort().outputData),
                    false,
                    false
            ));
        }

        Map<String, List<UniformValue>> uniforms = new HashMap<>();

        for (int i = 0; i < postPassInterface.luminance$getUniformBlocks().entrySet().size(); i++) {
            //noinspection unchecked
            uniforms.put(inputPorts.get(i + samplers).name, (List<UniformValue>)organisedNode.inputSources[i + samplers].getPort().outputData);
        }

        return new PostChainConfig.Pass(
                postPassInterface.luminance$getPipeline().getVertexShader(),
                postPassInterface.luminance$getPipeline().getFragmentShader(),
                inputs,
                Identifier.parse((String)outputPorts.getFirst().outputData),
                uniforms
        );
    }
}
