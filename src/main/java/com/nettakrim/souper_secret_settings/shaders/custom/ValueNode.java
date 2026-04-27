package com.nettakrim.souper_secret_settings.shaders.custom;

import com.nettakrim.souper_secret_settings.gui.DraggableEditBoxWidget;
import com.nettakrim.souper_secret_settings.gui.custom.GraphScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class ValueNode extends Node {
    private final List<DraggableEditBoxWidget> widgets = new ArrayList<>();

    private static final int widgetHeight = 10;

    @Override
    protected void initialisePorts() {
        addOutput("value", getType());
    }

    protected abstract PortType getType();

    @Override
    public void updatePositions(HashMap<InputPort, Wire> wires) {
        super.updatePositions(wires);

        if (widgets.isEmpty()) {
            DraggableEditBoxWidget widget = new DraggableEditBoxWidget(0, width, widgetHeight, Component.empty());
            widgets.add(widget);

            GraphScreen instance = GraphScreen.getInstance();
            if (instance != null) {
                instance.addActualWidget(widget);
            }
        }

        for (DraggableEditBoxWidget widget : widgets) {
            widget.setPosition(position.x, position.y + height - footerHeight);
            height += widgetHeight;
        }
    }

    @Override
    public void renderNode(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderNode(guiGraphics, mouseX, mouseY, delta);

        for (DraggableEditBoxWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public void onDock(HashMap<InputPort, Wire> wires) {
        super.onDock(wires);

        GraphScreen instance = GraphScreen.getInstance();
        if (instance != null) {
            for (DraggableEditBoxWidget widget : widgets) {
                instance.removeActualWidget(widget);
            }
        }

        widgets.clear();
    }

    @Override
    public void clearCaches() {
        super.clearCaches();
        widgets.clear();
    }
}
