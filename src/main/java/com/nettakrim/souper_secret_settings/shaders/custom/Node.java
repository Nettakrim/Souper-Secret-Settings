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

    protected void addInput(String name, PortType type) {
        inputPorts.add(new InputPort<>(this, name, type));
    }

    protected void addOutput(String name, PortType type) {
        outputPorts.add(new OutputPort<>(this, name, type));
    }

    public boolean isEnd() {
        return false;
    }
}
