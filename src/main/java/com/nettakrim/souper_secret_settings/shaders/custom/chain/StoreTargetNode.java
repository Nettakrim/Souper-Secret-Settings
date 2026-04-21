package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;

public class StoreTargetNode extends Node {
    StoreTargetNode() {
        super();
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("target", new PortType.StringPort());
        addInput("input", new PortType.TargetPort());
    }

    @Override
    public boolean isEnd() {
        return true;
    }
}
