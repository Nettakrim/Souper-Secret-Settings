package com.nettakrim.souper_secret_settings.shaders.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import org.jetbrains.annotations.NotNull;

public class InputPort extends Port {
    public Node docked;

    public InputPort(Node node, String name, PortType portType) {
        super(node, name, portType);
    }

    @Override
    public void renderPort(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderPort(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.textRenderer().accept(TextAlignment.LEFT, positionCache.x + Port.textMargin, positionCache.y - verticalOffset, text);

        if (docked != null) {
            docked.renderPorts(guiGraphics, mouseX, mouseY, delta, false);
        }
    }

    public void renderDockedNode(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (docked != null) {
            docked.renderNode(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public int getHeight() {
        int height = super.getHeight();
        if (docked != null) {
            docked.position.set(positionCache);
            docked.position.y += height;
            docked.updatePositions();
            height += docked.height;
        }
        return height;
    }

    @Override
    public Port hoveredPort(float mouseX, float mouseY) {
        Port self = super.hoveredPort(mouseX, mouseY);
        if (self != null) {
            return self;
        }
        if (docked != null) {
            return docked.hoveredPort(mouseX, mouseY);
        }
        return null;
    }
}
