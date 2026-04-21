package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.mclegoman.luminance.client.shaders.interfaces.PostPassInterface;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;

public class PassNode extends Node {
    private final PostPassInterface postPassInterface;

    public PassNode(PostPassInterface postPassInterface) {
        super();
        this.postPassInterface = postPassInterface;
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        for (String input : postPassInterface.luminance$getPipeline().getSamplers()) {
            addInput(input, PortType.TARGET);
        }
        addOutput("output", PortType.TARGET);
    }
}
