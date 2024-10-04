package com.nettakrim.souper_secret_settings.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public abstract class ParameterWidget extends ClickableWidget {
    public int count;

    public ParameterWidget(int count, Text name, int xBuffer, int width) {
        super(xBuffer, 0, width, 20, name);
        this.count = count;
    }


    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {

    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
