package com.nettakrim.souper_secret_settings.shaders.custom.shader;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.ValueNode;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class TextureNode extends ValueNode {
    private String name;

    public TextureNode() {
        this.name = "In";
        initialisePorts();
    }

    @Override
    protected PortType getType() {
        return PortType.TARGET;
    }

    @Override
    protected List<String> getValues() {
        return List.of(name);
    }

    @Override
    protected void onSetValue(int index, String value) {
        name = value;
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = name+"Sampler";
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Texture");
    }
}
