package com.nettakrim.souper_secret_settings.shaders.custom;

public abstract class Port {
    public final Node node;
    public final String name;
    public final PortType portType;

    public Port(Node node, String name, PortType portType) {
        this.node = node;
        this.name = name;
        this.portType = portType;
    }
}
