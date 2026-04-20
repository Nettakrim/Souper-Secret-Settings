package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.Port;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;

public class StoreTargetNode extends Node {
    StoreTargetNode() {
        super();
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        inputPorts.add(new Port<PortType.StringPort>(this, "target"));
        inputPorts.add(new Port<PortType.TargetPort>(this, "input"));
    }

    @Override
    public boolean isEnd() {
        return true;
    }
}
