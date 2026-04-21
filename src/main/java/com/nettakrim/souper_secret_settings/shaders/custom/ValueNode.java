package com.nettakrim.souper_secret_settings.shaders.custom;

public abstract class ValueNode extends Node {
    @Override
    protected void initialisePorts() {
        addOutput("value", getType());
    }

    protected abstract PortType getType();
}
