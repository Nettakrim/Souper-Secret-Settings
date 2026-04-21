package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.ValueNode;

public class StringNode extends ValueNode {
    public String value;

    public StringNode(String value) {
        super();
        this.value = value;
        initialisePorts();
    }

    @Override
    protected PortType getType() {
        return PortType.STRING;
    }
}
