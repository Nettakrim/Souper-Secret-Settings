package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;

public class ReadTargetNode extends Node {
    ReadTargetNode() {
        super();
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("target", PortType.STRING).docked = new StringNode();
        addOutput("output", PortType.TARGET);
    }
}