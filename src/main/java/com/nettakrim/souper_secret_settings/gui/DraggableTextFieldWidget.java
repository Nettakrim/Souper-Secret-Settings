package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class DraggableTextFieldWidget extends TextFieldWidget {
    public DraggableTextFieldWidget(int x, int width, int height, Text message) {
        super(SouperSecretSettingsClient.client.textRenderer, x, 0, width, height, message);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        try {
            float f = Float.parseFloat(getText());
            setText(Float.toString(f + (float)(deltaX/50.0 * Math.max(Math.abs(f), 0.5f))));
            this.setCursorToStart(false);
        } catch (Exception ignored) {}
    }
}
