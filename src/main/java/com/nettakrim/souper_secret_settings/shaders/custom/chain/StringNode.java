package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.ValueNode;

public class StringNode extends ValueNode {
    @Override
    protected PortType getType() {
        return new PortType.StringPort();
    }
}
