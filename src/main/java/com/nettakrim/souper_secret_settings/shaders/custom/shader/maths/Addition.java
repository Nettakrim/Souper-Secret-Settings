package com.nettakrim.souper_secret_settings.shaders.custom.shader.maths;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Addition extends Node {
    public Addition() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("A", PortType.VEC4);
        addInput("B", PortType.VEC4);
        addOutput("Out", PortType.VEC4);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) {
        return "vec4 "+outputPorts.getFirst().outputData + " = "+organisedNode.getVectorInput(0, PortType.VEC4)+" + "+organisedNode.getVectorInput(1, PortType.VEC4);
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Add");
    }
}
