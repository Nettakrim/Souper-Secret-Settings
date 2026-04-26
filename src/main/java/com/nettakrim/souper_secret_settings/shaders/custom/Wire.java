package com.nettakrim.souper_secret_settings.shaders.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

public class Wire {
    public OutputPort source;
    public InputPort destination;

    private final Matrix3x2fStack matrixCache = new Matrix3x2fStack();

    public void updatePosition() {
        int offsetX = destination.positionCache.x - source.positionCache.x;
        int offsetY = destination.positionCache.y - source.positionCache.y;

        matrixCache.identity();
        matrixCache.translate(source.positionCache.x + 0.5f, source.positionCache.y + 0.5f);
        matrixCache.rotate((float) Math.atan2(-offsetX, offsetY));
        matrixCache.translate(-0.5f, -0.5f);
        matrixCache.scale(1f, Mth.sqrt(offsetX * offsetX + offsetY * offsetY));
    }

    public void render(@NotNull GuiGraphics guiGraphics, boolean border) {
        Matrix3x2fStack matrixStack = guiGraphics.pose();
        matrixStack.pushMatrix();
        matrixStack.mul(matrixCache);

        if (border) {
            guiGraphics.fill(-1, 0, 0, 1, -16777216);
            guiGraphics.fill(1, 0, 2, 1, -16777216);
        } else {
            guiGraphics.fillGradient(0, 0, 1, 1, source.portType.color, destination.portType.color);
        }

        matrixStack.popMatrix();
    }
}
