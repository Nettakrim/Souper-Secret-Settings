package com.nettakrim.souper_secret_settings.shaders.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class Node {
    public final List<InputPort> inputPorts = new ArrayList<>();
    public final List<OutputPort> outputPorts = new ArrayList<>();

    protected abstract void initialisePorts();

    public abstract void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid);

    protected InputPort addInput(String name, PortType type) {
        InputPort port = new InputPort(this, name, type);
        inputPorts.add(port);
        return port;
    }

    protected void addOutput(String name, PortType type) {
        OutputPort port = new OutputPort(this, name, type);
        outputPorts.add(port);
    }

    public boolean isEnd() {
        return false;
    }
}
