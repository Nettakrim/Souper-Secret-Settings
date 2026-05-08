package com.nettakrim.souper_secret_settings.shaders.custom.shader;

import com.nettakrim.souper_secret_settings.shaders.custom.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Supplier;

public class FragColorNode extends Node {
    public FragColorNode() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("Color", PortType.VEC4);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {

    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) throws GraphCompilationException {
        return "fragColor = "+organisedNode.getVectorInput(0, PortType.VEC4);
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Output");
    }

    @Override
    public RootType rootType(HashMap<InputPort, Wire> wires) {
        return RootType.MAIN;
    }
}
