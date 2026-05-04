package com.nettakrim.souper_secret_settings.gui.custom;

import dev.dannytaylor.luminance.client.data.ClientData;
import com.nettakrim.souper_secret_settings.shaders.custom.*;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class GraphScreen extends Screen {
    private final Graph<?> graph;
    private final Screen parent;
    public final Panning panning;
    private final CreationMenu creationMenu;

    private boolean isDragSelecting;
    private final List<Node> selected = new ArrayList<>();
    private final Vector2i dragPosition = new Vector2i();

    private Wire drawingWire;
    private Port drawingEnd;

    private Node hoveredNode;

    public GraphScreen(Graph<?> graph, Screen parent) {
        super(Component.empty());
        this.graph = graph;
        this.parent = parent;
        this.panning = new Panning();
        this.creationMenu = new CreationMenu(100, this::createNode);
        creationMenu.setActive(false);
    }

    @Override
    protected void init() {
        panning.setSize(width, height);
        addRenderableWidget(creationMenu);
        for (Node node : graph.nodes) {
            node.clearUICaches();
        }
    }

    @Override
    public void onClose() {
        for (Node node : graph.nodes) {
            node.clearUICaches();
            node.hovered = false;
            node.selected = false;
        }
        minecraft.setScreen(parent);
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        mouseButtonEvent = scaleMouseButtonEvent(mouseButtonEvent);

        // it would be nice if you could pan while drag selecting, but pressing a mouse button stops mouseDragged from firing
        isDragSelecting = false;

        if (mouseButtonEvent.button() == 2) {
            panning.mouseClicked(mouseButtonEvent);
            return true;
        }

        if (super.mouseClicked(mouseButtonEvent, doubleClick)) {
            selected.clear();
            updateSelectedNodes();
            return true;
        }

        setFocused(null);
        dragPosition.set((int)Math.round(mouseButtonEvent.x()), (int)Math.round(mouseButtonEvent.y()));
        creationMenu.setActive(false);

        if (mouseButtonEvent.button() == 0) {
            creationMenu.setActive(false);

            Port port = getHoveredPort((float)mouseButtonEvent.x(), (float)mouseButtonEvent.y());
            if (port != null && selected.size() <= 1) {
                if (port instanceof InputPort inputPort) {
                    drawingWire = graph.wires.remove(inputPort);
                    if (drawingWire == null) {
                        drawingWire = new Wire();
                        drawingWire.destination = inputPort;
                        drawingEnd = drawingWire.source = new OutputPort(null, "", port.portType);
                    } else {
                        drawingWire.destination.node.updateConnections(graph.wires);
                        drawingEnd = drawingWire.destination = new InputPort(null, "", drawingWire.source.portType);
                    }
                }
                else if (port instanceof OutputPort outputPort) {
                    drawingWire = new Wire();
                    drawingWire.source = outputPort;
                    drawingEnd = drawingWire.destination = new InputPort(null, "", port.portType);
                }

                snapDrawing((float)mouseButtonEvent.x(), (float)mouseButtonEvent.y());
                selected.clear();
                updateSelectedNodes();
                return true;
            }

            Node grabbed = grab((float)mouseButtonEvent.x(), (float)mouseButtonEvent.y());
            if (grabbed != null) {
                isDragSelecting = false;
                if (!selected.contains(grabbed)) {
                    selected.clear();
                    selected.add(grabbed);
                    updateSelectedNodes();
                }

                // move nodes on top but maintain their relative order
                // this will also add a previously docked grabbed node to the list of nodes
                graph.nodes.removeAll(selected);
                graph.nodes.addAll(selected);
            } else {
                isDragSelecting = true;
                selected.clear();
                updateSelectedNodes();
            }

            return true;
        }

        if (mouseButtonEvent.button() == 1) {
            creationMenu.init(graph.getCreationRoot(), mouseButtonEvent);
            creationMenu.setActive(true);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(@NotNull MouseButtonEvent mouseButtonEvent) {
        mouseButtonEvent = scaleMouseButtonEvent(mouseButtonEvent);

        if (panning.mouseReleased(mouseButtonEvent)) {
            return true;
        }

        if (isDragSelecting && mouseButtonEvent.button() == 0) {
            isDragSelecting = false;
            return true;
        }

        if (drawingWire != null) {
            if (drawingWire.destination != drawingEnd && drawingWire.source != drawingEnd) {
                drawingWire.destination.hideDock(graph.wires);
                graph.addWire(drawingWire);
            }
            topologyChanged();
            drawingEnd = null;
            drawingWire = null;
            return true;
        }

        if (selected.size() == 1 && drop((float)mouseButtonEvent.x(), (float)mouseButtonEvent.y(), selected.getFirst())) {
            selected.getFirst().selected = false; // docked nodes wont get their selectedness cleared by updateSelectedNodes()
            selected.clear();
            updateSelectedNodes();
            return true;
        }

        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseDragged(@NotNull MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        mouseButtonEvent = scaleMouseButtonEvent(mouseButtonEvent);

        float scale = panning.getCurrentZoom();
        deltaX *= scale;
        deltaY *= scale;

        if (super.mouseDragged(mouseButtonEvent, deltaX, deltaY)) {
            return true;
        }

        if (drawingWire != null) {
            snapDrawing((float)mouseButtonEvent.x(), (float)mouseButtonEvent.y());
        }

        Vector2i current = new Vector2i((int) Math.round(mouseButtonEvent.x()), (int) Math.round(mouseButtonEvent.y()));

        if (mouseButtonEvent.button() == 0) {
            if (isDragSelecting) {
                selected.clear();
                for (Node node : graph.nodes) {
                    if (node.inBounds(dragPosition.x, dragPosition.y, current.x, current.y)) {
                        selected.add(node);
                    }
                }
                updateSelectedNodes();

                return true;
            } else if (!selected.isEmpty()) {
                current.sub(dragPosition);

                for (Node node : selected) {
                    node.position.add(current.x, current.y);
                }

                dragPosition.add(current);
                return true;
            }
        }

        if (mouseButtonEvent.button() == 1) {
            Vector2i a = new Vector2i((int)Math.round(mouseButtonEvent.x()), (int)Math.round(mouseButtonEvent.y()));
            Vector2i b = new Vector2i((int)Math.round(mouseButtonEvent.x()-deltaX), (int)Math.round(mouseButtonEvent.y()-deltaY));
            if (Graph.removeWiresIf(graph.wires, (wire) -> wire.cut(a,b))) {
                topologyChanged();
                creationMenu.setActive(false);
            }

            if (dragPosition.distanceSquared(current) > 100) {
                creationMenu.setActive(false);
            }
        }

        return panning.mouseDragged(mouseButtonEvent);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        return panning.mouseScrolled((float)verticalAmount);
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent keyEvent) {
        // delete or backspace
        if (!selected.isEmpty() && (keyEvent.key() == 259 || keyEvent.key() == 261)) {
            for (Node node : selected) {
                node.detachWires(graph.wires);
                node.clearUICaches();
            }
            graph.nodes.removeAll(selected);
            selected.clear();
            topologyChanged();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    private MouseButtonEvent scaleMouseButtonEvent(MouseButtonEvent mouseButtonEvent) {
        Vector2f scaledPos = panning.getScaledMousePos((float) mouseButtonEvent.x(), (float) mouseButtonEvent.y());
        return new MouseButtonEvent(scaledPos.x, scaledPos.y, mouseButtonEvent.buttonInfo());
    }

    private Node grab(float x, float y) {
        for (Node node : graph.nodes.reversed()) {
            Node grabbed = node.grabNode(x, y, graph.wires, null);
            if (grabbed != null) {
                // grabbed node will be removed from docks
                // it always needs to be reinserted, so that it renders on top
                return grabbed;
            }
        }

        return null;
    }

    private boolean drop(float x, float y, Node node) {
        if (node.outputPorts.size() != 1) {
            return false;
        }

        for (Node other : graph.nodes.reversed()) {
            if (other == node) {
                continue;
            }

            if (other.drop(x, y, node, graph.wires)) {
                // node was dropped into a dock
                graph.nodes.remove(node);
                topologyChanged();
                return true;
            }
        }

        return false;
    }

    private void snapDrawing(float x, float y) {
        Port port = getHoveredPort(x, y);
        drawingEnd.positionCache.set((int)x, (int)y);

        if (drawingEnd instanceof InputPort) {
            drawingWire.destination = port instanceof InputPort inputPort && drawingWire.source.canConnectTo(inputPort) ? inputPort : (InputPort)drawingEnd;
        } else {
            drawingWire.source = port instanceof OutputPort outputPort && outputPort.canConnectTo(drawingWire.destination) ? outputPort : (OutputPort)drawingEnd;
        }
    }

    private Port getHoveredPort(float mouseX, float mouseY) {
        for (Node node : graph.nodes.reversed()) {
            Port port = node.hoveredPort(mouseX, mouseY, graph.wires);
            if (port != null) {
                return port;
            }
        }
        return null;
    }

    private void createNode(Node node) {
        graph.nodes.add(node);
        node.updateConnections(graph.wires);
        creationMenu.setActive(false);
    }

    private void updateSelectedNodes() {
        for (Node node : graph.nodes) {
            node.selected = false;
        }
        for (Node node : selected) {
            node.selected = true;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();

        panning.update(mouseX, mouseY);
        panning.applyMatrix(pose);

        Vector2f scaledPos = panning.getScaledMousePos(mouseX, mouseY);

        Node currentHovered = null;
        for (Node node : graph.nodes.reversed()) {
            node.updatePositions(graph.wires);
            if (currentHovered == null && !isDragSelecting) {
                currentHovered = node.getHoveredNode(scaledPos.x, scaledPos.y);
            }
        }

        if (currentHovered != hoveredNode) {
            if (hoveredNode != null) {
                hoveredNode.hovered = false;
            }
            hoveredNode = currentHovered;
            if (hoveredNode != null) {
                hoveredNode.hovered = true;
            }
        }

        if (isDragSelecting) {
            guiGraphics.fill(dragPosition.x, dragPosition.y, Math.round(scaledPos.x), Math.round(scaledPos.y), 128 << 24);
        }

        for (Wire wire : graph.wires.values()) {
            wire.updatePosition();
            wire.render(guiGraphics, true);
        }

        for (Wire wire : graph.wires.values()) {
            wire.render(guiGraphics, false);
        }

        for (Node node : graph.nodes) {
            node.renderNode(guiGraphics, (int)scaledPos.x, (int)scaledPos.y, delta);
            node.renderPorts(guiGraphics, true);
        }

        if (drawingWire != null) {
            drawingWire.updatePosition();
            drawingWire.render(guiGraphics, true);
            drawingWire.render(guiGraphics, false);
        }

        super.render(guiGraphics, mouseX, mouseY, delta);

        pose.popMatrix();

        long change = panning.changedZoom();
        if (change < 0) {
            String zoom = String.valueOf(1f/panning.getCurrentZoom());
            zoom = zoom.substring(0, Math.min(5, zoom.length()))+"x";
            ActiveTextCollector textCollector = guiGraphics.textRenderer();
            textCollector.accept(TextAlignment.LEFT, 1, height - 9, textCollector.defaultParameters().withOpacity(Math.min(-change,256)/256f), Component.literal(zoom));
        }
    }

    @Override
    protected void renderBlurredBackground(@NotNull GuiGraphics guiGraphics) {}

    @Override
    protected void renderMenuBackground(@NotNull GuiGraphics guiGraphics, int x, int y, int width, int height) {}

    @Override
    public boolean isPauseScreen() {return false;}

    public <T extends GuiEventListener & NarratableEntry> void addActualWidget(@NotNull T guiEventListener) {
        addWidget(guiEventListener);
    }

    public void removeActualWidget(@NotNull GuiEventListener guiEventListener) {
        removeWidget(guiEventListener);
    }

    public static GraphScreen getInstance() {
        if (ClientData.minecraft.screen instanceof GraphScreen graphScreen) {
            return graphScreen;
        }
        return null;
    }

    public void topologyChanged() {
        graph.makeChange();
    }

    public void valueChanged() {
        // this could be optimised, avoiding needing to re-organise the graph
        graph.makeChange();
    }
}
