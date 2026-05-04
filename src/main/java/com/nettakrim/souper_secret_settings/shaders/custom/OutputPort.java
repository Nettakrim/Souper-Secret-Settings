package com.nettakrim.souper_secret_settings.shaders.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class OutputPort extends Port {
    public Node docker;
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

    public String getGlVariableDeclaration() {
        return portType.glType + " " + outputData;
    }

    @Override
    public boolean setPortType(PortType portType, HashMap<InputPort, Wire> wires) {
        if (super.setPortType(portType, wires)) {
            // propagate changes
            if (docker != null) {
                docker.updateConnections(wires);
            } else {
                for (Wire wire : wires.values()) {
                    if (wire.source == this) {
                        wire.destination.node.updateConnections(wires);
                    }
                }
            }
            return true;
        }
        return false;
    }
}
