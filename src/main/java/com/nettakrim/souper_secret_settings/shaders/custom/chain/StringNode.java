package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.ValueNode;

import java.util.function.Supplier;

public class StringNode extends ValueNode {
    public String value;

    public StringNode(String value) {
        this.value = value;
        initialisePorts();
    }

    @Override
    protected PortType getType() {
        return PortType.STRING;
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = value;
    }
}
