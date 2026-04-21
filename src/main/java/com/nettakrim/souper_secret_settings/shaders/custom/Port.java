package com.nettakrim.souper_secret_settings.shaders.custom;

public abstract class Port<T extends PortType> {
    public final Node node;
    public final String name;
    public final T portType;

    public Port(Node node, String name, T portType) {
        this.node = node;
        this.name = name;
        this.portType = portType;
    }
}
