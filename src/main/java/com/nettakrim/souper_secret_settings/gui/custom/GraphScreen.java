package com.nettakrim.souper_secret_settings.gui.custom;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.Wire;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;

public class GraphScreen extends Screen {
    private final Screen parent;

    private final Graph graph;

    private Node selectedNode;
    private final Vector2d dragPosition = new Vector2d();

    public GraphScreen(Graph graph, Screen parent) {
        super(Component.empty());
        this.graph = graph;
        this.parent = parent;
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

        for (Node node : graph.nodes) {
            if (node.isHovered(mouseButtonEvent.x(), mouseButtonEvent.y())) {
                selectedNode = node;
                dragPosition.set(selectedNode.position);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(@NotNull MouseButtonEvent mouseButtonEvent) {
        if (selectedNode != null) {
            selectedNode = null;
            return true;
        }

        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseDragged(@NotNull MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseButtonEvent, deltaX, deltaY)) {
            return true;
        }

        dragPosition.add(deltaX, deltaY);

        if (selectedNode != null) {
            selectedNode.position.set(dragPosition);
            return true;
        }

        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        for (Node node : graph.nodes) {
            node.updatePositions();
        }

        for (Wire wire : graph.wires) {
            wire.render(guiGraphics, mouseX, mouseY, delta);
        }

        for (Node node : graph.nodes) {
            node.renderNode(guiGraphics, mouseX, mouseY, delta);
            node.renderPorts(guiGraphics, mouseX, mouseY, delta, true);
        }
    }
}
