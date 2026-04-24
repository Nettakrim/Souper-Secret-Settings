package com.nettakrim.souper_secret_settings.gui;

import java.util.function.BiFunction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class LabelledWidget extends AbstractWidget {
    public final AbstractWidget widget;

    public LabelledWidget(int x, int width, Component message, BiFunction<Integer, Integer, AbstractWidget> widgetFunction) {
        super(x, 0, width/3, 20, message);
        widget = widgetFunction.apply(x + width/3, width - width/3);
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, SoupGui.BUTTON_TEXTURES.get(true, false), getX(), getY(), getWidth()*2, getHeight(), -1);
        renderScrollingStringOverContents(context.textRenderer(), getMessage(), 2);
        widget.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput builder) {

    }

    @Override
    public void setY(int y) {
        super.setY(y);
        widget.setY(y);
    }
}
