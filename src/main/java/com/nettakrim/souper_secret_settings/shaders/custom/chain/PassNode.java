package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.shaders.ChainData;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostPassInterface;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.InputPort;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PassNode extends Node {
    private final Identifier vertexShader;
    private final Identifier fragmentShader;
    private final String name;
    private final List<String> samplers;
    private final List<String> blocks;

    public PassNode(PostPassInterface postPassInterface) {
        vertexShader = postPassInterface.luminance$getPipeline().getVertexShader();
        fragmentShader = postPassInterface.luminance$getPipeline().getFragmentShader();
        this.name = ChainData.getName(postPassInterface);

        samplers = new ArrayList<>(postPassInterface.luminance$inputs().size());
        for (PostPass.Input input : postPassInterface.luminance$inputs()) {
            samplers.add(input.samplerName());
        }

        blocks = new ArrayList<>();
        blocks.addAll(postPassInterface.luminance$getUniformBlocks().keySet());

        initialisePorts();

        for (int i = 0; i < blocks.size(); i++) {
            InputPort inputPort = inputPorts.get(i + samplers.size());
            inputPort.docked = new UniformBlockNode(postPassInterface.luminance$getUniformBlocks().get(inputPort.name));
        }
    }

    @Override
    protected void initialisePorts() {
        for (String input : samplers) {
            addInput(input, PortType.TARGET);
        }

        for (String block : blocks) {
            addInput(block, PortType.BLOCK);
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
        for (int i = 0; i < samplers.size(); i++) {
            inputs.add(new PostChainConfig.TargetInput(
                    inputPorts.get(i).name,
                    Identifier.parse((String)organisedNode.inputSources[i].getPort().outputData),
                    false,
                    false
            ));
        }

        Map<String, List<UniformValue>> uniforms = new HashMap<>();

        for (int i = 0; i < blocks.size(); i++) {
            //noinspection unchecked
            uniforms.put(inputPorts.get(i + samplers.size()).name, (List<UniformValue>)organisedNode.inputSources[i + samplers.size()].getPort().outputData);
        }

        return new PostChainConfig.Pass(
                vertexShader,
                fragmentShader,
                inputs,
                Identifier.parse((String)outputPorts.getFirst().outputData),
                uniforms
        );
    }

    @Override
    protected Component getTitle() {
        return Component.literal(name);
    }
}
