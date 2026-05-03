package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.gui.custom.GraphScreen;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.ShaderGraph;
import dev.dannytaylor.luminance.client.data.ClientData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ShaderNode extends Node {
    private final ShaderGraph shaderGraph;

    public ShaderNode() {
        shaderGraph = new ShaderGraph();
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("In", PortType.TARGET);
        addOutput("Out", PortType.TARGET);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = uuid.get();
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) {
        return new PostChainConfig.Pass(
                Identifier.parse("core/screenquad"),
                shaderGraph.getOrCompile(),
                List.of(new PostChainConfig.TargetInput("In", Identifier.parse((String)organisedNode.getInputData(0)), false, false)),
                Identifier.parse((String)outputPorts.getFirst().outputData),
                Map.of()
        );
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Shader");
    }

    @Override
    protected boolean hasSettings() {
        return true;
    }

    @Override
    protected void openSettings(Button button) {
        ClientData.minecraft.setScreen(new GraphScreen(shaderGraph, ClientData.minecraft.screen));
    }
}
