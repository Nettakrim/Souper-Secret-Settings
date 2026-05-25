package com.nettakrim.souper_secret_settings.gui.custom;

import dev.dannytaylor.luminance.client.data.ClientData;
import dev.dannytaylor.luminance.client.shaders.Uniforms;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public class Panning {
    private static final float scrollsPerLayer = 4f;
    private static final float zoomSpeed = 7f;
    private static final float minZoom = -1f;
    private static final float maxZoom = 2f;

    private float zoom;
    private float currentZoom;

    private boolean panning;
    private final Vector2f origin;
    private final Vector2f position;

    private long changedZoomAt;

    private Vector2f offset;

    public Panning()
    {
        zoom = 0;
        currentZoom = 1;
        origin = new Vector2f();
        position = new Vector2f();
    }

    public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent) {
        if (mouseButtonEvent.button() == 2) {
            origin.set((float)mouseButtonEvent.x(), (float)mouseButtonEvent.y());
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
            Vector2f mousePos = new Vector2f((float)mouseButtonEvent.x(), (float)mouseButtonEvent.y());
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

            changedZoomAt = System.currentTimeMillis() + 1000;
        }
    }

    public Vector2f getScaledMousePos(float x, float y)
    {
        Vector2f mousePosition = new Vector2f(x, y);
        mousePosition.sub(offset);
        mousePosition.mul(currentZoom);
        mousePosition.add(position);
        return mousePosition;
    }

    public Vector2f getInverseMousePos(float x, float y) {
        return new Vector2f((x - position.x)/currentZoom + offset.x, (y - position.y)/currentZoom + offset.y);
    }

    public void applyMatrix(Matrix3x2f matrix3x2f) {
        float factor = 1f / currentZoom;
        matrix3x2f.translate(offset);
        matrix3x2f.scale(factor);

        // round camera panning to the nearest pixel, to stop weird aliasing at certain alignments or while panning
        factor *= ClientData.minecraft.getWindow().getGuiScale();
        matrix3x2f.translate(Math.round(-position.x * factor) / factor, Math.round(-position.y * factor) / factor);
    }

    public float getCurrentZoom() {
        return currentZoom;
    }

    public long changedZoom() {
        return System.currentTimeMillis() - changedZoomAt;
    }

    public void setSize(int width, int height) {
        offset = new Vector2f((float)(width/2), (float)(height/2));
    }
}
