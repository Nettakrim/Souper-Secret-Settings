package com.nettakrim.souper_secret_settings.shaders.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

public class Wire {
    public OutputPort source;
    public InputPort destination;

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderLine(guiGraphics, source.positionCache.x, source.positionCache.y, source.portType.color, destination.positionCache.x, destination.positionCache.y, destination.portType.color);
    }

    private void renderLine(GuiGraphics guiGraphics, int startX, int startY, int startColor, int endX, int endY, int endColor) {
        int offsetX = endX - startX;
        int offsetY = endY - startY;

        Matrix3x2fStack matrixStack = guiGraphics.pose();
        matrixStack.pushMatrix();

        matrixStack.translate(startX + 0.5f, startY + 0.5f);
        matrixStack.rotate((float) Math.atan2(offsetY, offsetX));
        matrixStack.translate(-0.5f, -0.5f);
        int distance = Mth.floor(Mth.sqrt(offsetX * offsetX + offsetY * offsetY));

        guiGraphics.fill(0, -1, distance + 1, 0, -16777216);
        guiGraphics.fill(0, 1, distance + 1, 2, -16777216);
        guiGraphics.fillGradient(0, 0, distance + 1, 1, startColor, endColor);

        matrixStack.popMatrix();
    }
}
