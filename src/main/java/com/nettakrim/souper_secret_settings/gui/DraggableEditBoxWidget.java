package com.nettakrim.souper_secret_settings.gui;

import dev.dannytaylor.luminance.client.data.ClientData;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DraggableEditBoxWidget extends EditBox implements ListChild {
    public boolean disableDrag = false;
    public Float dragValue = null;

    public DraggableEditBoxWidget(int x, int width, int height, Component message) {
        super(ClientData.minecraft.font, x, 0, width, height, message);
    }

    @Override
    protected void onDrag(@NotNull MouseButtonEvent click, double deltaX, double deltaY) {
        if (disableDrag) {
            return;
        }

        try {
            deltaX = CursorWrap.applyWrap(click, deltaX, deltaY, this);

            float f;
            if (dragValue == null) {
                f = Float.parseFloat(getValue());
            } else {
                f = dragValue;
                dragValue = null;
            }
            float prev = f;
            f += (float)(deltaX/50.0 * Math.max(Math.abs(f), 0.5f));
            if (!Float.isFinite(f)) {
                f = prev < 0 ? -Float.MAX_VALUE : Float.MAX_VALUE;
            }

            // always hit zero if swapping sign
            if (f != 0 && prev != 0 && f > 0 != prev > 0) {
                f = 0;
            }

            setValue(String.valueOf(f));
        } catch (Exception ignored) {}
    }

    @Override
    public boolean mouseReleased(@NotNull MouseButtonEvent mouseButtonEvent) {
        CursorWrap.resetOffset();
        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public int getCollapseHeight() {
        return getHeight();
    }

    @Override
    public void onRemove() {

    }

    @Override
    public void setValue(@NotNull String text) {
        super.setValue(text);
        this.moveCursorToStart(false);
    }
}
