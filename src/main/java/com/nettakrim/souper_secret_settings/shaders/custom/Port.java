package com.nettakrim.souper_secret_settings.shaders.custom;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public abstract class Port {
    public final Node node;
    public final String name;
    public final PortType portType;

    // automatically updated when a node is rendered
    public final Vector2i positionCache = new Vector2i();

    public Port(Node node, String name, PortType portType) {
        this.node = node;
        this.name = name;
        this.portType = portType;
    }

    public int getHeight() {
        return 10;
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.fill(positionCache.x-1, positionCache.y-1, positionCache.x+1, positionCache.y+1, portType.color);
    }
}
