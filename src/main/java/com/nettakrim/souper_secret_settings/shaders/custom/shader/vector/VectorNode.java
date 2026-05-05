package com.nettakrim.souper_secret_settings.shaders.custom.shader.vector;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.ValueNode;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class VectorNode extends ValueNode {
    private final PortType portType;
    private final List<Float> values;

    public VectorNode(PortType portType) {
        this.portType = portType;

        int count = this.portType.ordinal()-PortType.VECN.ordinal();
        values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(0f);
        }

        initialisePorts();
    }

    @Override
    protected PortType getType() {
        return portType;
    }

    @Override
    protected List<String> getValues() {
        return values.stream().map(Objects::toString).toList();
    }

    @Override
    protected void onSetValue(int index, String value) {
        try {
            values.set(index, Float.parseFloat(value));
        } catch (Exception ignored) {}
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(portType.glType).append('(');
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                stringBuilder.append(',');
            }
            stringBuilder.append(values.get(i));
        }
        stringBuilder.append(')');

        outputPorts.getFirst().outputData = stringBuilder.toString();
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Vector "+(this.portType.ordinal()-PortType.VECN.ordinal()));
    }
}
