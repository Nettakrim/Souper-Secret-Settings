package com.nettakrim.souper_secret_settings.shaders.custom;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    public final List<InputPort<?>> inputPorts;
    public final List<OutputPort<?>> outputPorts;

    public Node() {
        inputPorts = new ArrayList<>();
        outputPorts = new ArrayList<>();
    }

    protected abstract void initialisePorts();

    protected <T extends PortType> InputPort<T> addInput(String name, T type) {
        InputPort<T> port = new InputPort<>(this, name, type);
        inputPorts.add(port);
        return port;
    }

    protected <T extends PortType> OutputPort<T> addOutput(String name, T type) {
        OutputPort<T> port = new OutputPort<>(this, name, type);
        outputPorts.add(port);
        return port;
    }

    public boolean isEnd() {
        return false;
    }
}
