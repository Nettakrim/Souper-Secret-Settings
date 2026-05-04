package com.nettakrim.souper_secret_settings.shaders.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.HashMap;

public abstract class Port {
    public final Node node;
    public final String name;
    public PortType portType;
    protected final Component text;

    public static final int baseHeight = 10;
    public static final int verticalOffset = 3;
    public static final int textMargin = 3;

    // automatically updated when a node is rendered
    public final Vector2i positionCache = new Vector2i();

    public Port(Node node, String name, PortType portType) {
        this.node = node;
        this.name = name;
        this.portType = portType;
        this.text = Component.literal(name).setStyle(Style.EMPTY.withColor(0xFFCCCCCC));
    }

    public int updateHeight(HashMap<InputPort, Wire> wires) {
        return baseHeight;
    }

    public void renderPort(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.fill(positionCache.x-1, positionCache.y-1, positionCache.x+2, positionCache.y+2, portType.color);
    }

    public Port hoveredPort(float mouseX, float mouseY, HashMap<InputPort, Wire> wires) {
        float dx = positionCache.x - mouseX;
        float dy = positionCache.y - mouseY;
        if (dx * dx + dy * dy < 25) {
            return this;
        }
        return null;
    }

    public boolean setPortType(PortType portType, HashMap<InputPort, Wire> wires) {
        if (this.portType != portType) {
            this.portType = portType;
            return true;
        }
        return false;
    }
}
