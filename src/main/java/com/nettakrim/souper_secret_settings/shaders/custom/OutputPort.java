package com.nettakrim.souper_secret_settings.shaders.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Stack;

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
        if (inSameExpression(inputPort.node)) {
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

    public boolean inSameExpression(Node trial) {
        // trivial check
        if (node == trial) {
            return true;
        }

        // get top most docked node
        Node top = node;
        if (docker != null) {
            while (top.outputPorts.size() == 1 && top.outputPorts.getFirst().docker != null) {
                top = top.outputPorts.getFirst().docker;
            }
        }

        // then check all children
        Stack<Node> check = new Stack<>();
        check.push(top);

        while (!check.isEmpty()) {
            Node candidate = check.pop();
            if (candidate == trial) {
                return true;
            }

            for (InputPort inputPort : candidate.inputPorts) {
                if (inputPort.docked != null) {
                    check.push(inputPort.docked);
                }
            }
        }

        return false;
    }
}
