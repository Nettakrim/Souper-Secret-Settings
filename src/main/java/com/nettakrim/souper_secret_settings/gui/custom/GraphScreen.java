package com.nettakrim.souper_secret_settings.gui.custom;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.Wire;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class GraphScreen extends Screen {
    private final Screen parent;

    private final Graph graph;

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
    public boolean mouseDragged(@NotNull MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseButtonEvent, deltaX, deltaY)) {
            return true;
        }

        for (Node node : graph.nodes) {
            if (node.isHovered(mouseButtonEvent.x(), mouseButtonEvent.y())) {
                node.position.add(Mth.sign(deltaX), Mth.sign(deltaY));
                return true;
            }
        }

        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        for (Node node : graph.nodes) {
            node.updatePositions();
            node.render(guiGraphics, mouseX, mouseY, delta);
        }

        for (Wire wire : graph.wires) {
            wire.render(guiGraphics, mouseX, mouseY, delta);
        }
    }
}
