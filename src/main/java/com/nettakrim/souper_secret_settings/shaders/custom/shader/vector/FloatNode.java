package com.nettakrim.souper_secret_settings.shaders.custom.shader.vector;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.ValueNode;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class FloatNode extends ValueNode {
    private float value;

    public FloatNode() {
        value = 0f;
        initialisePorts();
    }

    @Override
    protected PortType getType() {
        return PortType.VEC1;
    }

    @Override
    protected List<String> getValues() {
        return List.of(String.valueOf(value));
    }

    @Override
    protected void onSetValue(int index, String value) {
        try {
            this.value = Float.parseFloat(value);
        } catch (Exception ignored) {}
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = String.valueOf(value);
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Float");
    }
}
