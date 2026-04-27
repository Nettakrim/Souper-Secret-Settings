package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.data.ClientData;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class DraggableIntEditBoxWidget extends EditBox {
    protected final int min;
    protected final int max;
    protected final String defaultValue;
    protected final Consumer<Integer> onChange;

    float value;

    public DraggableIntEditBoxWidget(int x, int width, int height, Component message, int min, int max, int defaultValue, Consumer<Integer> onChange) {
        super(ClientData.minecraft.font, x, 0, width, height, message);
        setResponder(this::onChange);
        this.min = min;
        this.max = max;
        this.onChange = onChange;
        value = defaultValue;
        this.defaultValue = String.valueOf(defaultValue);
    }

    @Override
    protected void onDrag(@NotNull MouseButtonEvent click, double deltaX, double deltaY) {
        try {
            deltaX = CursorWrap.applyWrap(click, deltaX, deltaY, this);

            value += (float)(deltaX/50.0 * Math.max(Math.abs(value), 4));
            setValue(String.valueOf(Mth.clamp(Math.round(value), min, max)));
        } catch (Exception ignored) {}
    }

    @Override
    public boolean mouseReleased(@NotNull MouseButtonEvent mouseButtonEvent) {
        CursorWrap.resetOffset();
        return super.mouseReleased(mouseButtonEvent);
    }

    void onChange(String text) {
        if (defaultValue.startsWith(text)) {
            setSuggestion(defaultValue.substring(text.length()));
        } else {
            setSuggestion(null);
        }

        try {
            float f = Float.parseFloat(text);
            int i = Math.round(f);

            if (i != Math.round(value)) {
                value = f;
            }

            if (i >= min && i <= max) {
                onChange.accept(i);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent keyInput) {
        if (this.isFocused()) {
            if (keyInput.key() == 258) {
                String text = getValue();
                if (defaultValue.startsWith(text)) {
                    setValue(defaultValue);
                } else {
                    try {
                        float f = Float.parseFloat(text);
                        setValue(Integer.toString(Mth.clamp(Math.round(f), min, max)));
                    } catch (Exception ignored) {
                        setValue(defaultValue);
                    }
                }
                moveCursorToEnd(false);
                return true;
            }
        }
        return super.keyPressed(keyInput);
    }

    @Override
    public void setValue(@NotNull String text) {
        super.setValue(text);
        this.moveCursorToStart(false);
    }
}
