package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ReadTargetNode extends Node {
    public ReadTargetNode() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("Target", PortType.STRING).setDock(new StringNode("main"));
        addOutput("Out", PortType.TARGET);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = new TargetInputInfo((String)organisedNode.getInputData(0));
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Read Target");
    }
}