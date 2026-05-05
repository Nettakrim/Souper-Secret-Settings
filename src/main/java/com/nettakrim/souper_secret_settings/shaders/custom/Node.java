package com.nettakrim.souper_secret_settings.shaders.custom;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.SoupButtonWidget;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.gui.custom.GraphScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public abstract class Node {
    public final List<InputPort> inputPorts = new ArrayList<>();
    public final List<OutputPort> outputPorts = new ArrayList<>();

    public Vector2i position = new Vector2i();
    public int height;
    public final int width = 100;

    protected static final int baseHeight = 15;
    protected static final int footerHeight = 2;

    public boolean includedInLastCompile = false;
    public boolean selected;
    public boolean hovered;

    protected SoupButtonWidget settingsButton = null;

    protected abstract void initialisePorts();

    public abstract void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid);

    // main entry into the compiled graph for a given node
    // any dependencies should be handled separately
    protected @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) throws GraphCompilationException {
        return null;
    }

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

    protected abstract @NotNull Component getTitle();

    public void updatePositions(HashMap<InputPort, Wire> wires) {
        height = baseHeight;

        for (InputPort inputPort : inputPorts) {
            Vector2i portPos = inputPort.positionCache;
            portPos.set(position);

            portPos.y += height;
            height += inputPort.updateHeight(wires);
            portPos.y += Port.verticalOffset;
        }

        int outputHeight = baseHeight;
        for (OutputPort outputPort : outputPorts) {
            if (outputPort.docker != null) {
                break;
            }

            Vector2i portPos = outputPort.positionCache;
            portPos.set(position);
            portPos.x += width;

            portPos.y += outputHeight;
            outputHeight += outputPort.updateHeight(wires);
            portPos.y += Port.verticalOffset;
        }

        // rarely, outputs will be larger than inputs
        if (outputHeight > height) {
            height = outputHeight;
        }

        // footer offset
        height += footerHeight;

        if (hasSettings()) {
            if (settingsButton == null) {
                settingsButton = new SoupButtonWidget(SouperSecretSettingsClient.translate("gui.config"), this::openSettings, 0, 0, 12, 12);
                assert GraphScreen.getInstance() != null;
                GraphScreen.getInstance().addActualWidget(settingsButton);
            }
            settingsButton.setPosition(position.x + width - settingsButton.getWidth() - 1, position.y + 1);

        }
    }

    public void renderNode(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SoupGui.BUTTON_TEXTURES.get(includedInLastCompile, selected || hovered), position.x, position.y, width, height, -1);

        guiGraphics.textRenderer().accept(position.x + 3, position.y + 3, getTitle());

        for (InputPort inputPort : inputPorts) {
            inputPort.renderDockedNode(guiGraphics, mouseX, mouseY, delta);
        }

        if (settingsButton != null) {
            settingsButton.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    public void renderPorts(@NotNull GuiGraphics guiGraphics, boolean includeOutput) {
        for (InputPort inputPort : inputPorts) {
            inputPort.renderPort(guiGraphics);
        }

        if (includeOutput) {
            for (OutputPort outputPort : outputPorts) {
                outputPort.renderPort(guiGraphics);
            }
        }
    }

    public Node grabNode(float mouseX, float mouseY, HashMap<InputPort, Wire> wires, InputPort source) {
        if (outside(mouseX, mouseY)) {
            return null;
        }

        for (InputPort inputPort : inputPorts) {
            Node node = inputPort.grabNode(mouseX, mouseY, wires);
            if (node != null) {
                return node;
            }
        }

        if (source != null) {
            // undock node
            source.setDock(null);
            Wire wire = new Wire();
            wire.source = outputPorts.getFirst();
            wire.destination = source;
            wires.put(source, wire);
        }

        return this;
    }

    public boolean drop(float mouseX, float mouseY, Node node, HashMap<InputPort, Wire> wires) {
        if (outside(mouseX, mouseY)) {
            return false;
        }

        for (InputPort inputPort : inputPorts) {
            if (inputPort.docked != null && !wires.containsKey(inputPort)) {
                if (inputPort.docked.drop(mouseX, mouseY, node, wires)) {
                    return true;
                }
            }

            if (node.outputPorts.getFirst().canConnectTo(inputPort) && mouseY >= inputPort.positionCache.y - 3 && mouseY <= inputPort.positionCache.y + 3) {
                inputPort.setDock(node);
                // remove any nodes connected to the now docked output, or any now looping nodes
                Graph.removeWiresIf(wires, wire -> (wire.source.node == node || wire.source.inSameExpression(wire.destination.node)));
                node.updateConnections(wires);
                updateConnections(wires);
                return true;
            }
        }

        return false;
    }

    private boolean outside(float mouseX, float mouseY) {
        return mouseX < position.x || mouseY < position.y || mouseX > position.x + width || mouseY > position.y + height;
    }

    public boolean inBounds(float x1, float y1, float x2, float y2) {
        return position.x < Math.max(x1, x2) && position.x + width > Math.min(x1, x2) && position.y < Math.max(y1, y2) && position.y + height > Math.min(y1, y2);
    }

    public void hideDock(HashMap<InputPort, Wire> wires) {
        for (InputPort inputPort : inputPorts) {
            inputPort.hideDock(wires);
        }
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

    public Node getHoveredNode(float mouseX, float mouseY) {
        if (outside(mouseX, mouseY)) {
            return null;
        }

        for (InputPort inputPort : inputPorts) {
            if (inputPort.docked != null) {
                Node node = inputPort.docked.getHoveredNode(mouseX, mouseY);
                if (node != null) {
                    return node;
                }
            }
        }

        return this;
    }

    public void detachWires(HashMap<InputPort, Wire> wires) {
        for (InputPort inputPort : inputPorts) {
            wires.remove(inputPort);
            if (inputPort.docked != null) {
                inputPort.docked.detachWires(wires);
            }
        }

        Graph.removeWiresIf(wires, wire -> wire.source.node == this);
    }

    public void clearUICaches() {
        for (InputPort inputPort : inputPorts) {
            if (inputPort.docked != null) {
                inputPort.docked.clearUICaches();
            }
        }

        if (settingsButton != null) {
            assert GraphScreen.getInstance() != null;
            GraphScreen.getInstance().removeActualWidget(settingsButton);
            settingsButton = null;
        }
    }

    public void clearCompileCaches() {
        includedInLastCompile = false;
        for (InputPort inputPort : inputPorts) {
            if (inputPort.docked != null) {
                inputPort.docked.clearCompileCaches();
            }
        }
    }

    protected boolean hasSettings() {
        return false;
    }

    protected void openSettings(Button button) {

    }

    public void updateConnections(HashMap<InputPort, Wire> wires) {

    }

    protected PortType setInputsToWidestType(HashMap<InputPort, Wire> wires) {
        // set all port types to be the widest input type
        // this currently relies on the ordinals of the types, so it is fragile
        PortType dynamicType = PortType.VECN;

        for (InputPort inputPort : inputPorts) {
            PortType inputType = null;
            Wire wire = wires.get(inputPort);

            if (wire != null) {
                inputType = wire.source.portType;
            } else if (inputPort.docked != null) {
                inputType = inputPort.docked.outputPorts.getFirst().portType;
            }

            if (inputType != null && inputType.ordinal() > dynamicType.ordinal()) {
                dynamicType = inputType;
            }
        }

        for (InputPort inputPort : inputPorts) {
            inputPort.setPortType(dynamicType, wires);
        }

        return dynamicType;
    }
}
