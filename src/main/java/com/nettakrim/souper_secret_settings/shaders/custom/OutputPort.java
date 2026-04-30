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
    public void renderPort(@NotNull GuiGraphics guiGraphics) {
        super.renderPort(guiGraphics);
        guiGraphics.textRenderer().accept(TextAlignment.RIGHT, positionCache.x - Port.textMargin, positionCache.y - verticalOffset, text);
    }

    public boolean canConnectTo(InputPort inputPort) {
        // dont allow self connections
        if (node == inputPort.node) {
            return false;
        }

        return portType.canConnect.test(portType, inputPort.portType);
    }
}
