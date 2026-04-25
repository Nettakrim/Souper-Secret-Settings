package com.nettakrim.souper_secret_settings.gui.custom;

import com.mclegoman.luminance.client.shaders.Uniforms;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public class Panning {
    private final float scrollsPerLayer = 4f;
    private final float zoomSpeed = 8f;
    private final float minZoom = -1f;
    private final float maxZoom = 2f;

    private float zoom;
    private float currentZoom;

    private boolean panning;
    private final Vector2f origin;
    private final Vector2f position;

    public Panning()
    {
        zoom = 0;
        currentZoom = (float) Math.pow(2, zoom);
        origin = new Vector2f();
        position = new Vector2f();
    }

    public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent) {
        if (mouseButtonEvent.button() == 2) {
            origin.set(getScaledMousePos((float)mouseButtonEvent.x(), (float)mouseButtonEvent.y()));
            panning = true;
            return true;
        }

        return false;
    }

    public boolean mouseReleased(@NotNull MouseButtonEvent mouseButtonEvent) {
        if (mouseButtonEvent.button() == 2) {
            panning = false;
            return true;
        }
        return false;
    }

    public boolean mouseDragged(@NotNull MouseButtonEvent mouseButtonEvent) {
        if (panning) {
            Vector2f mousePos = getScaledMousePos((float)mouseButtonEvent.x(), (float)mouseButtonEvent.y());
            mousePos.sub(position);
            position.set(origin);
            position.sub(mousePos);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(float scroll) {
        if (scroll != 0)
        {
            zoom = Mth.clamp(zoom - scroll / scrollsPerLayer, minZoom, maxZoom);
            return true;
        }
        return false;
    }

    public void update(double mouseX, double mouseY)
    {
        float targetZoom = (float)Math.pow(2, zoom);
        float newZoom = Mth.lerp(Uniforms.shaderTime.getExpDeltaTime(zoomSpeed), currentZoom, targetZoom);

        if (newZoom != currentZoom)
        {
            // zoom around cursor
            Vector2f scaleAround = getScaledMousePos((float)mouseX, (float)mouseY);
            position.sub(scaleAround);

            float scaleChange = newZoom / currentZoom;
            position.mul(scaleChange);
            position.add(scaleAround);
            currentZoom = newZoom;
        }
    }

    public Vector2f getScaledMousePos(float x, float y)
    {
        Vector2f mousePosition = new Vector2f(x, y);
        mousePosition.mul(currentZoom);
        mousePosition.add(position);
        return mousePosition;
    }

    public void applyMatrix(Matrix3x2f matrix3x2f) {
        matrix3x2f.scale(1f/currentZoom);
        matrix3x2f.translate(-position.x, -position.y);
    }

    public float getCurrentZoom() {
        return currentZoom;
    }
}
