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
    public int height;

    public static final int width = 100;

    public void updatePositions() {
        height = 10;

        for (InputPort inputPort : inputPorts) {
            Vector2i portPos = inputPort.positionCache;
            portPos.set(position);
            portPos.y += height;

            height += inputPort.getHeight();
        }

        for (OutputPort outputPort : outputPorts) {
            Vector2i portPos = outputPort.positionCache;
            portPos.set(position);
            portPos.x += width;
            portPos.y += outputPort.getHeight();
        }
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SoupGui.BUTTON_TEXTURES.get(true, false), position.x, position.y, width, height, -1);

        for (InputPort inputPort : inputPorts) {
            inputPort.render(guiGraphics, mouseX, mouseY, delta);
        }

        for (OutputPort outputPort : outputPorts) {
            outputPort.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= position.x && mouseY >= position.y && mouseX <= position.x + width && mouseY <= position.y + height;
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
