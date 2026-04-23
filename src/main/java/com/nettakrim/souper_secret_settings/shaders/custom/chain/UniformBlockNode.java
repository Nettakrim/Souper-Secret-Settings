package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.mclegoman.luminance.client.shaders.UniformBlock;
import com.mclegoman.luminance.client.shaders.UniformInstance;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import net.minecraft.client.renderer.UniformValue;
import org.joml.Vector4f;

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
            // TODO: port types
            addInput(uniformInstance.name, PortType.STRING).docked = new StringNode(null);
        }
        addOutput("block", PortType.BLOCK);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        List<UniformValue> list = new ArrayList<>();

        for (Graph.Source source : organisedNode.inputSources) {
            //source.getPort().outputData
            list.add(new UniformValue.Vec4Uniform(new Vector4f(1f,1f,1f, 1f)));
        }

        outputPorts.getFirst().outputData = list;
    }
}
