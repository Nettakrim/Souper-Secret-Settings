package com.nettakrim.souper_secret_settings.shaders.custom.shader;

import com.nettakrim.souper_secret_settings.gui.DraggableEditBoxWidget;
import com.nettakrim.souper_secret_settings.gui.custom.GraphScreen;
import com.nettakrim.souper_secret_settings.shaders.custom.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Supplier;

public class TextureNode extends Node {
    public String name;
    private DraggableEditBoxWidget widget;

    public TextureNode() {
        this.name = "In";
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addOutput("", PortType.TARGET);
        addOutput("Size", PortType.VEC2);
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        outputPorts.getFirst().outputData = name+"Sampler";
        outputPorts.get(1).outputData = name+"Size";
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Texture");
    }

    @Override
    public void updatePositions(HashMap<InputPort, Wire> wires, int depth) {
        super.updatePositions(wires, depth);

        if (widget == null) {
            widget = new DraggableEditBoxWidget(1, 1, ValueNode.widgetHeight, Component.empty());
            widget.setValue(name);
            widget.setResponder(value -> name = value);

            GraphScreen instance = GraphScreen.getInstance();
            if (instance != null) {
                instance.addActualWidget(widget);
            }
        }

        widget.setPosition(position.x + 1, position.y + 12);
        widget.setWidth(width - 2);
    }

    @Override
    public void renderNode(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderNode(guiGraphics, mouseX, mouseY, delta);
        widget.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public void hideDock(HashMap<InputPort, Wire> wires) {
        super.hideDock(wires);
        removeWidget();
    }

    @Override
    public void clearUICaches() {
        super.clearUICaches();
        removeWidget();
    }

    private void removeWidget() {
        GraphScreen instance = GraphScreen.getInstance();
        if (instance != null) {
            instance.removeActualWidget(widget);
        }

        widget = null;
    }
}
