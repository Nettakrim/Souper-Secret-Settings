package com.nettakrim.souper_secret_settings.shaders.custom;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class InputPort extends Port {
    public Node docked;

    public InputPort(Node node, String name, PortType portType) {
        super(node, name, portType);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        if (docked != null) {
            docked.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public int getHeight() {
        int height = super.getHeight();
        if (docked != null) {
            docked.position.set(positionCache);
            docked.updatePositions();
            height += docked.height;
        }
        return height;
    }
}
