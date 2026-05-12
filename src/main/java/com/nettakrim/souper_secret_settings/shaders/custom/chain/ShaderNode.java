package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.gui.custom.GraphScreen;
import com.nettakrim.souper_secret_settings.shaders.custom.*;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.FragColorNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.SampleNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.ShaderGraph;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.TextureNode;
import dev.dannytaylor.luminance.client.data.ClientData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.*;
import java.util.function.Supplier;

public class ShaderNode extends Node {
    private final ShaderGraph shaderGraph;
    private long lastVersion = 0;

    public ShaderNode() {
        shaderGraph = new ShaderGraph();

        TextureNode textureNode = new TextureNode();
        SampleNode sampleNode = new SampleNode();
        FragColorNode fragColorNode = new FragColorNode();
        shaderGraph.nodes.add(textureNode);
        shaderGraph.nodes.add(sampleNode);
        shaderGraph.nodes.add(fragColorNode);

        Wire a2b = new Wire();
        a2b.source = textureNode.outputPorts.getFirst();
        a2b.destination = sampleNode.inputPorts.getFirst();
        shaderGraph.addWire(a2b);

        Wire b2c = new Wire();
        b2c.source = sampleNode.outputPorts.getFirst();
        b2c.destination = fragColorNode.inputPorts.getFirst();
        shaderGraph.addWire(b2c);

        textureNode.position = new Vector2i(-170, -40);
        sampleNode.position = new Vector2i(-50, -40);
        fragColorNode.position = new Vector2i(70, -40);

        shaderGraph.makeChange();

        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("In", PortType.TARGET);
        addOutput("Out", PortType.TARGET);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = new TargetInputInfo(uuid.get());
    }

    @Override
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) {
        ArrayList<PostChainConfig.Input> inputs = new ArrayList<>();

        for (int i = 0; i < inputPorts.size(); i++) {
            inputs.add(((InputInfo)organisedNode.getInputData(i)).getInput(inputPorts.get(i).name));
        }

        return List.of(new PostChainConfig.Pass(
                Identifier.parse("core/screenquad"),
                shaderGraph.getOrCompile(),
                inputs,
                ((TargetInputInfo)outputPorts.getFirst().outputData).targetId,
                Map.of()
        ));
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

    @Override
    public void updatePositions(HashMap<InputPort, Wire> wires, int depth) {
        long currentVersion = shaderGraph.getVersion();
        if (currentVersion != lastVersion) {
            updateInputPorts(wires);
            lastVersion = currentVersion;
        }

        super.updatePositions(wires, depth);
    }

    private void updateInputPorts(HashMap<InputPort, Wire> wires) {
        List<String> inputNames = shaderGraph.getInputNames();

        for (Iterator<InputPort> it = inputPorts.iterator(); it.hasNext(); ) {
            InputPort inputPort = it.next();

            if (!inputNames.contains(inputPort.name)) {
                wires.remove(inputPort);
                if (inputPort.docked != null) {
                    inputPort.docked.detachWires(wires);
                }
                it.remove();
            }
        }

        for (String inputName : inputNames) {
            boolean isNew = true;
            for (InputPort inputPort : inputPorts) {
                if (inputPort.name.equals(inputName)) {
                    isNew = false;
                    break;
                }
            }

            if (isNew) {
                addInput(inputName, PortType.TARGET);
            }
        }

        // sort alphabetically
        inputPorts.sort(Comparator.comparing(port -> port.name));
    }
}
