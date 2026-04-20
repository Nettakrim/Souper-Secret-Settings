package com.nettakrim.souper_secret_settings.shaders.custom;

public class Port<T extends PortType> {
    public final Node node;
    private final String name;

    public Port(Node node, String name) {
        this.node = node;
        this.name = name;
    }
}
