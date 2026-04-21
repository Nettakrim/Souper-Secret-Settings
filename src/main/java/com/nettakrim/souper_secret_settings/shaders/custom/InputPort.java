package com.nettakrim.souper_secret_settings.shaders.custom;

public class InputPort<T extends PortType> extends Port<T> {
    public Node docked;

    public InputPort(Node node, String name, T portType) {
        super(node, name, portType);
    }
}
