package com.nettakrim.souper_secret_settings.shaders.custom;

import com.nettakrim.souper_secret_settings.gui.DraggableEditBoxWidget;
import com.nettakrim.souper_secret_settings.gui.custom.GraphScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class ValueNode extends Node {
    private final List<AbstractWidget> widgets = new ArrayList<>();

    public static final int widgetHeight = 10;

    @Override
    protected void initialisePorts() {
        addOutput("", getType());
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
    public void updatePositions(HashMap<InputPort, Wire> wires, int depth) {
        super.updatePositions(wires, depth);

        if (widgets.isEmpty()) {
            GraphScreen instance = GraphScreen.getInstance();

            int i = 0;
            for (String value : getValues()) {
                EditBox widget = createWidget(value);

                int finalI = i++;
                widget.setResponder((s) -> setValue(finalI, s));

                widgets.add(widget);
                if (instance != null) {
                    instance.addActualWidget(widget);
                }
            }
        }

        height = baseHeight;

        for (AbstractWidget widget : widgets) {
            widget.setPosition(position.x + 1, position.y + height - footerHeight);
            widget.setWidth(width - 2);
            height += widgetHeight;
        }
    }

    @Override
    public void renderNode(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderNode(guiGraphics, mouseX, mouseY, delta);

        for (AbstractWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public void hideDock(HashMap<InputPort, Wire> wires) {
        super.hideDock(wires);
        removeWidgets();
    }

    @Override
    public void clearUICaches() {
        super.clearUICaches();
        removeWidgets();
    }

    private void removeWidgets() {
        GraphScreen instance = GraphScreen.getInstance();
        if (instance != null) {
            for (AbstractWidget widget : widgets) {
                instance.removeActualWidget(widget);
            }
        }

        widgets.clear();
    }

    protected EditBox createWidget(String value) {
        DraggableEditBoxWidget widget = new DraggableEditBoxWidget(1, 1, widgetHeight, Component.empty());
        widget.setValue(value);
        return widget;
    }
}
