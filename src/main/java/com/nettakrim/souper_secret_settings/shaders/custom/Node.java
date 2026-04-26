package com.nettakrim.souper_secret_settings.shaders.custom;

import com.nettakrim.souper_secret_settings.gui.SoupGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public abstract class Node {
    public final List<InputPort> inputPorts = new ArrayList<>();
    public final List<OutputPort> outputPorts = new ArrayList<>();
    protected Component title = getTitle();

    public Vector2i position = new Vector2i();
    public int height;

    public static final int width = 100;
    private static final int baseHeight = 15;

    public void updatePositions(HashMap<InputPort, Wire> wires) {
        height = baseHeight;

        for (InputPort inputPort : inputPorts) {
            Vector2i portPos = inputPort.positionCache;
            portPos.set(position);

            portPos.y += height;
            height += inputPort.updateHeight(wires);
            portPos.y += Port.verticalOffset;
        }

        // footer offset
        height += 2;

        int outputHeight = baseHeight;
        for (OutputPort outputPort : outputPorts) {
            Vector2i portPos = outputPort.positionCache;
            portPos.set(position);
            portPos.x += width;

            portPos.y += outputHeight;
            outputHeight += outputPort.updateHeight(wires);
            portPos.y += Port.verticalOffset;
        }
    }

    public void renderNode(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SoupGui.BUTTON_TEXTURES.get(true, false), position.x, position.y, width, height, -1);

        guiGraphics.textRenderer().accept(position.x + 3, position.y + 3, title);

        for (InputPort inputPort : inputPorts) {
            inputPort.renderDockedNode(guiGraphics, mouseX, mouseY, delta);
        }
    }

    public void renderPorts(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, boolean includeOutput) {
        for (InputPort inputPort : inputPorts) {
            inputPort.renderPort(guiGraphics, mouseX, mouseY, delta);
        }

        if (includeOutput) {
            for (OutputPort outputPort : outputPorts) {
                outputPort.renderPort(guiGraphics, mouseX, mouseY, delta);
            }
        }
    }

    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX >= position.x && mouseY >= position.y && mouseX <= position.x + width && mouseY <= position.y + height;
    }

    public Port hoveredPort(float mouseX, float mouseY, HashMap<InputPort, Wire> wires) {
        for (InputPort inputPort : inputPorts) {
            Port port = inputPort.hoveredPort(mouseX, mouseY, wires);
            if (port != null) {
                return port;
            }
        }

        for (OutputPort outputPort : outputPorts) {
            Port port = outputPort.hoveredPort(mouseX, mouseY, wires);
            if (port != null) {
                return port;
            }
        }

        return null;
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

    protected abstract Component getTitle();
}
