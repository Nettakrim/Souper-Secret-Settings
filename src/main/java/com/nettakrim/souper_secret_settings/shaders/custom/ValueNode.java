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

    protected abstract List<String> getValues();

    protected abstract void onSetValue(int index, String value);

    private void setValue(int index, String value) {
        onSetValue(index, value);

        GraphScreen instance = GraphScreen.getInstance();
        if (instance != null) {
            instance.valueChanged();
        }
    }

    @Override
    public void updatePositions(HashMap<InputPort, Wire> wires) {
        super.updatePositions(wires);

        if (widgets.isEmpty()) {
            GraphScreen instance = GraphScreen.getInstance();

            int i = 0;
            for (String value : getValues()) {
                DraggableEditBoxWidget widget = new DraggableEditBoxWidget(1, width - 2, widgetHeight, Component.empty());
                widget.setValue(value);

                int finalI = i++;
                widget.setResponder((s) -> setValue(finalI, s));

                widgets.add(widget);
                if (instance != null) {
                    instance.addActualWidget(widget);
                }
            }
        }

        // reduce base height padding
        height -= 3;

        for (DraggableEditBoxWidget widget : widgets) {
            widget.setPosition(position.x + 1, position.y + height - footerHeight);
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
    public void clearUICaches() {
        super.clearUICaches();
        widgets.clear();
    }
}
