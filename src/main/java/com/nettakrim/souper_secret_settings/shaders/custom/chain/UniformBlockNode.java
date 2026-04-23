package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.mclegoman.luminance.client.shaders.UniformBlock;
import com.mclegoman.luminance.client.shaders.UniformInstance;
import com.mclegoman.luminance.client.shaders.interfaces.internal.InternalUniformValueInterface;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.InputPort;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import net.minecraft.client.renderer.UniformValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UniformBlockNode extends Node {
    private final UniformBlock block;

    public UniformBlockNode(UniformBlock block) {
        this.block = block;
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        for (UniformInstance uniformInstance : block.uniforms) {
            InputPort inputPort = addInput(uniformInstance.name, PortType.UNIFORM_VALUE);
            //if (uniformInstance.override == null) {
                inputPort.docked = new UniformValueNode(uniformInstance.defaultValue);
            //} else {
            //    inputPort.docked = new UniformOverrideNode()
            //}
        }
        addOutput("block", PortType.BLOCK);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        List<UniformValue> list = new ArrayList<>();

        for (int i = 0; i < organisedNode.inputSources.length; i++) {
            Graph.Source source = organisedNode.inputSources[i];
            UniformValue uniformValue = (UniformValue)source.getPort().outputData;
            // this wont work properly if a uniform value is reused, but thats fine for now
            ((InternalUniformValueInterface)uniformValue).luminance$setName(block.uniforms.get(i).name);
            list.add((UniformValue)source.getPort().outputData);
        }

        outputPorts.getFirst().outputData = list;
    }
}
