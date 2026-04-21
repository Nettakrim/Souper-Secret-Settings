package com.nettakrim.souper_secret_settings.shaders.custom;

public class InputPort extends Port {
    public Node docked;

    public InputPort(Node node, String name, PortType portType) {
        super(node, name, portType);
    }
}
