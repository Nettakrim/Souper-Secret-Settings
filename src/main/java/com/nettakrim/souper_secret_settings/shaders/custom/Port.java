package com.nettakrim.souper_secret_settings.shaders.custom;

import org.joml.Vector2i;

public abstract class Port {
    public final Node node;
    public final String name;
    public final PortType portType;

    // automatically updated when a node is rendered
    public final Vector2i positionCache = new Vector2i();

    public Port(Node node, String name, PortType portType) {
        this.node = node;
        this.name = name;
        this.portType = portType;
    }
}
