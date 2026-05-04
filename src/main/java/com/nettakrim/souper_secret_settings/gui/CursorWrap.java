package com.nettakrim.souper_secret_settings.gui;

import dev.dannytaylor.luminance.client.data.ClientData;
import com.mojang.blaze3d.platform.Window;
import com.nettakrim.souper_secret_settings.gui.custom.GraphScreen;
import com.nettakrim.souper_secret_settings.mixin.WindowAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class CursorWrap {
    static Vector2d offset = new Vector2d(0,0);

    public static double applyWrap(@NotNull MouseButtonEvent click, double deltaX, double deltaY, AbstractWidget widget) {
        return applyWrap(click, deltaX, deltaY, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
    }

    // wrap cursor around edge of widget
    public static double applyWrap(@NotNull MouseButtonEvent click, double deltaX, double deltaY, int x, int y, int w, int h) {
        deltaX -= offset.x;
        deltaY -= offset.y;

        Window window = ClientData.minecraft.getWindow();
        int scale = window.getGuiScale();

        resetOffset();
        if (deltaX < 0 && click.x() < x + 1) {
            offset.x = w - 2;
        }
        else if (deltaX > 0 && click.x() > x + w - 1) {
            offset.x = 2 - w;
        }

        if (deltaY < 0 && click.y() < y + 1) {
            offset.y = h - 2;
        }
        else if (deltaY > 0 && click.y() > y + h - 1) {
            offset.y = 2 - h;
        }

        double xPos = click.x() + offset.x;
        double yPos = click.y() + offset.y;

        // correct mouse position for panning
        GraphScreen graphScreen = GraphScreen.getInstance();
        if (graphScreen != null) {
            Vector2f vector2f = graphScreen.panning.getInverseMousePos((float)xPos, (float)yPos);
            xPos = vector2f.x;
            yPos = vector2f.y;

            // undo the scaling of delta, so that it's always a constant amount, instead of going faster when zoomed out
            deltaX /= graphScreen.panning.getCurrentZoom();
        }

        //noinspection DataFlowIssue
        GLFW.glfwSetCursorPos(((WindowAccessor)(Object)window).getHandle(), xPos * scale + 0.5, yPos * scale + 0.5);

        // modify deltaX, since setting the cursor pos will add that offset to the next frames input
        return deltaX;
    }

    public static void resetOffset() {
        offset.x = 0;
        offset.y = 0;
    }
}
