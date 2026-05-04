package com.nettakrim.souper_secret_settings.shaders.custom.shader;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SampleNode extends Node {
    public SampleNode() {
        initialisePorts();
        inputPorts.get(1).setDock(new TexCoordNode());
    }

    @Override
    protected void initialisePorts() {
        addInput("Texture", PortType.TARGET);
        addInput("Position", PortType.VEC2);
        addOutput("Color", PortType.VEC4);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) {
        return "vec4 "+outputPorts.getFirst().outputData + " = texture("+organisedNode.getInputData(0)+","+organisedNode.getVectorInput(1, PortType.VEC2)+")";
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Sample");
    }
}
