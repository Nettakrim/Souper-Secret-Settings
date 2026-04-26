package com.nettakrim.souper_secret_settings.gui.custom;

import com.nettakrim.souper_secret_settings.shaders.custom.*;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2f;

public class GraphScreen extends Screen {
    private final Graph graph;
    private final Screen parent;
    private final Panning panning;

    private Node selectedNode;
    private final Vector2d dragPosition = new Vector2d();

    private Wire drawingWire;
    private Port drawingEnd;

    public GraphScreen(Graph graph, Screen parent) {
        super(Component.empty());
        this.graph = graph;
        this.parent = parent;
        this.panning = new Panning();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        if (super.mouseClicked(mouseButtonEvent, doubleClick)) {
            return true;
        }

        if (mouseButtonEvent.button() == 0) {
            Vector2f scaledPos = panning.getScaledMousePos((float) mouseButtonEvent.x(), (float) mouseButtonEvent.y());

            Port port = getHoveredPort(scaledPos.x, scaledPos.y);
            if (port != null) {
                if (port instanceof InputPort inputPort) {
                    drawingWire = graph.wires.remove(inputPort);
                    if (drawingWire == null) {
                        drawingWire = new Wire();
                        drawingWire.destination = inputPort;
                        drawingEnd = drawingWire.source = new OutputPort(null, "", port.portType);
                    } else {
                        drawingEnd = drawingWire.destination = new InputPort(null, "", drawingWire.source.portType);
                    }
                }
                else if (port instanceof OutputPort outputPort) {
                    drawingWire = new Wire();
                    drawingWire.source = outputPort;
                    drawingEnd = drawingWire.destination = new InputPort(null, "", port.portType);
                }

                snapDrawing(scaledPos);
                return true;
            }

            for (Node node : graph.nodes) {
                if (node.isHovered(scaledPos.x, scaledPos.y)) {
                    selectedNode = node;
                    dragPosition.set(selectedNode.position);
                    return true;
                }
            }
        }

        return panning.mouseClicked(mouseButtonEvent);
    }

    @Override
    public boolean mouseReleased(@NotNull MouseButtonEvent mouseButtonEvent) {
        if (drawingWire != null) {
            if (drawingWire.destination != drawingEnd && drawingWire.source != drawingEnd) {
                graph.addWire(drawingWire);
            }
            drawingEnd = null;
            drawingWire = null;
            return true;
        }

        if (selectedNode != null) {
            selectedNode = null;
            return true;
        }

        if (panning.mouseReleased(mouseButtonEvent)) {
            return true;
        }

        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseDragged(@NotNull MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseButtonEvent, deltaX, deltaY)) {
            return true;
        }

        Vector2f scaledPos = panning.getScaledMousePos((float) mouseButtonEvent.x(), (float) mouseButtonEvent.y());
        if (drawingWire != null) {
            snapDrawing(scaledPos);
        }

        float scale = panning.getCurrentZoom();
        dragPosition.add(deltaX * scale, deltaY * scale);

        if (selectedNode != null) {
            selectedNode.position.set(dragPosition);
            return true;
        }

        return panning.mouseDragged(mouseButtonEvent);
    }

    private void snapDrawing(Vector2f scaledPos) {
        Port port = getHoveredPort(scaledPos.x, scaledPos.y);
        drawingEnd.positionCache.set((int)scaledPos.x, (int)scaledPos.y);

        if (drawingEnd instanceof InputPort) {
            drawingWire.destination = port instanceof InputPort inputPort ? inputPort : (InputPort)drawingEnd;
        } else {
            drawingWire.source = port instanceof OutputPort outputPort ? outputPort : (OutputPort)drawingEnd;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        return panning.mouseScrolled((float)verticalAmount);
    }

    private Port getHoveredPort(float mouseX, float mouseY) {
        for (Node node : graph.nodes) {
            Port port = node.hoveredPort(mouseX, mouseY, graph.wires);
            if (port != null) {
                return port;
            }
        }
        return null;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        panning.update(mouseX, mouseY);
        long change = panning.changedZoom();
        if (change < 0) {
            String zoom = String.valueOf(1f/panning.getCurrentZoom());
            zoom = zoom.substring(0, Math.min(5, zoom.length()))+"x";
            ActiveTextCollector textCollector = guiGraphics.textRenderer();
            textCollector.accept(TextAlignment.LEFT, 1, height - 9, textCollector.defaultParameters().withOpacity(Math.min(-change,256)/256f), Component.literal(zoom));
        }
        panning.applyMatrix(guiGraphics.pose());

        Vector2f scaledPos = panning.getScaledMousePos(mouseX, mouseY);
        int x = Math.round(scaledPos.x);
        int y = Math.round(scaledPos.y);

        for (Node node : graph.nodes) {
            node.updatePositions(graph.wires);
        }

        for (Wire wire : graph.wires.values()) {
            wire.render(guiGraphics, x, y, delta);
        }

        for (Node node : graph.nodes) {
            node.renderNode(guiGraphics, x, y, delta);
            node.renderPorts(guiGraphics, x, y, delta, true);
        }

        if (drawingWire != null) {
            drawingWire.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Override
    protected void renderBlurredBackground(@NotNull GuiGraphics context) {}

    @Override
    protected void renderMenuBackground(@NotNull GuiGraphics context, int x, int y, int width, int height) {}

    @Override
    public boolean isPauseScreen() {return false;}
}
