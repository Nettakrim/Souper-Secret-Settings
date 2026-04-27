package com.nettakrim.souper_secret_settings.shaders.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class InputPort extends Port {
    public Node docked;
    private boolean dockEnabled;

    public InputPort(Node node, String name, PortType portType) {
        super(node, name, portType);
    }

    @Override
    public void renderPort(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderPort(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.textRenderer().accept(TextAlignment.LEFT, positionCache.x + Port.textMargin, positionCache.y - verticalOffset, text);

        if (dockEnabled) {
            docked.renderPorts(guiGraphics, mouseX, mouseY, delta, false);
        }
    }

    public void renderDockedNode(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (dockEnabled) {
            docked.renderNode(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public int updateHeight(HashMap<InputPort, Wire> wires) {
        int height = baseHeight;
        if (docked != null && !wires.containsKey(this)) {
            docked.position.set(positionCache);
            docked.position.y += height;
            docked.updatePositions(wires);
            height += docked.height;
            dockEnabled = true;
        } else {
            dockEnabled = false;
        }
        return height;
    }

    public Node grabNode(float mouseX, float mouseY, HashMap<InputPort, Wire> wires) {
        if (docked != null && !wires.containsKey(this)) {
            return docked.grabNode(mouseX, mouseY, wires, this);
        }
        return null;
    }

    @Override
    public Port hoveredPort(float mouseX, float mouseY, HashMap<InputPort, Wire> wires) {
        Port self = super.hoveredPort(mouseX, mouseY, wires);
        if (self != null) {
            return self;
        }
        if (docked != null && !wires.containsKey(this)) {
            return docked.hoveredPort(mouseX, mouseY, wires);
        }
        return null;
    }

    public void onDock(HashMap<InputPort, Wire> wires) {
        // recursively remove any wires attached to docked nodes
        wires.remove(this);
        if (docked != null) {
            docked.onDock(wires);
        }
    }
}
