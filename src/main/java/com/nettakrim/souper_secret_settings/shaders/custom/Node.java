package com.nettakrim.souper_secret_settings.shaders.custom;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    public final List<Port<?>> inputPorts;
    public final List<Port<?>> outputPorts;

    public Node() {
        inputPorts = new ArrayList<>();
        outputPorts = new ArrayList<>();
    }

    protected abstract void initialisePorts();

    public boolean isEnd() {
        return false;
    }
}
