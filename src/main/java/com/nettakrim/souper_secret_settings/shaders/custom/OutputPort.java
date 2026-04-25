package com.nettakrim.souper_secret_settings.shaders.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import org.jetbrains.annotations.NotNull;

public class OutputPort extends Port {
    public Object outputData;

    public OutputPort(Node node, String name, PortType portType) {
        super(node, name, portType);
    }

    @Override
    public void renderPort(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderPort(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.textRenderer().accept(TextAlignment.RIGHT, positionCache.x - Port.textMargin, positionCache.y - verticalOffset, text);
    }
}
