package com.nettakrim.souper_secret_settings.shaders.custom;

public class OutputPort<T extends PortType> extends Port<T> {
    public OutputPort(Node node, String name, T portType) {
        super(node, name, portType);
    }
}
