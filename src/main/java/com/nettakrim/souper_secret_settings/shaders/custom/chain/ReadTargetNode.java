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
        addInput("target", new PortType.StringPort());
        addOutput("output", new PortType.TargetPort());
    }
}