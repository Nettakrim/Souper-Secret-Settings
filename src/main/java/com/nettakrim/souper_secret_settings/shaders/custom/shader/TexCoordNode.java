package com.nettakrim.souper_secret_settings.shaders.custom.shader;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class TexCoordNode extends Node {
    public TexCoordNode() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addOutput("Position", PortType.VEC2);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = "texCoord";
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Texture Coordinate");
    }
}
