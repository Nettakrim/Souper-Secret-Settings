package com.nettakrim.souper_secret_settings.gui.custom;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.Wire;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        for (Node node : graph.nodes) {
            node.render(guiGraphics, mouseX, mouseY, delta);
        }

        for (Wire wire : graph.wires) {
            wire.render(guiGraphics, mouseX, mouseY, delta);
        }
    }
}
