package com.nettakrim.souper_secret_settings.shaders.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public abstract class Port {
    public final Node node;
    public final String name;
    public final PortType portType;
    protected final Component text;

    public static final int baseHeight = 10;
    public static final int verticalOffset = 3;

    // automatically updated when a node is rendered
    public final Vector2i positionCache = new Vector2i();

    public Port(Node node, String name, PortType portType) {
        this.node = node;
        this.name = name;
        this.portType = portType;
        this.text = Component.literal(name);
    }

    public int getHeight() {
        return baseHeight;
    }

    public void renderPort(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.fill(positionCache.x-1, positionCache.y-1 + verticalOffset, positionCache.x+2, positionCache.y+2 + verticalOffset, portType.color);
        guiGraphics.textRenderer().accept(positionCache.x, positionCache.y, text);
    }
}
