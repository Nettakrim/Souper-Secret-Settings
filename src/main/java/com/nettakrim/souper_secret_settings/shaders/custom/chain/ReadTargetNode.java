package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;

import java.util.function.Supplier;

public class ReadTargetNode extends Node {
    ReadTargetNode() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("target", PortType.STRING).docked = new StringNode("minecraft:main");
        addOutput("output", PortType.TARGET);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = organisedNode.inputSources[0].getPort().outputData;
    }
}