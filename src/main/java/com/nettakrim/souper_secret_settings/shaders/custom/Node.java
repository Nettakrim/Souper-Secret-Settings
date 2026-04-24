package com.nettakrim.souper_secret_settings.shaders.custom;

import com.nettakrim.souper_secret_settings.gui.SoupGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class Node {
    public final List<InputPort> inputPorts = new ArrayList<>();
    public final List<OutputPort> outputPorts = new ArrayList<>();

    public Vector2i position = new Vector2i();

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SoupGui.BUTTON_TEXTURES.get(true, false), position.x, position.y, 100, 100, -1);

        for (int i = 0; i < inputPorts.size(); i++) {
            Vector2i portPos = inputPorts.get(i).positionCache;
            portPos.set(position);
            portPos.y += i * 10;
        }

        for (int i = 0; i < outputPorts.size(); i++) {
            Vector2i portPos = outputPorts.get(i).positionCache;
            portPos.set(position);
            portPos.x += 100;
            portPos.y += i * 10;
        }
    }

    protected abstract void initialisePorts();

    public abstract void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid);

    protected InputPort addInput(String name, PortType type) {
        InputPort port = new InputPort(this, name, type);
        inputPorts.add(port);
        return port;
    }

    protected void addOutput(String name, PortType type) {
        OutputPort port = new OutputPort(this, name, type);
        outputPorts.add(port);
    }

    public boolean isEnd() {
        return false;
    }
}
