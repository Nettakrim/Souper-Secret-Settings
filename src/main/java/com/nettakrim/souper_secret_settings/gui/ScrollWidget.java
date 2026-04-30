package com.nettakrim.souper_secret_settings.gui;

import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ScrollWidget extends AbstractWidget {
    private static final Identifier SCROLLER_TEXTURE = Identifier.withDefaultNamespace("widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("widget/scroller_background");

    protected double scrollY;

    protected int scrollHeight;

    protected final IntConsumer onSetScroll;

    public ScrollWidget(int x, int y, int width, int height, Component message, IntConsumer onSetScroll) {
        super(x, y, width, height, message);
        this.onSetScroll = onSetScroll;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_BACKGROUND_TEXTURE, getX(), getY(), getWidth(), getHeight(), ARGB.white(alpha));

        int barHeight = (int)(getBarHeight()*getHeight());
        int y = scrollHeight > 0 ? (int)Math.round(scrollY/scrollHeight * (getHeight()-barHeight)) : 0;

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_TEXTURE, getX(), y + getY(), getWidth(), barHeight, ARGB.white(alpha));
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_TEXTURE, getX(), y + getY() + Mth.floor(barHeight/2f), getWidth(), 1, ARGB.color(255, 128, 128, 128));
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput builder) {

    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        setScroll(click.y());
    }

    @Override
    protected void onDrag(MouseButtonEvent click, double deltaX, double deltaY) {
        setScroll(click.y());
    }

    public void offsetScroll(double offset) {
        setScrollY(scrollY + offset);
    }

    protected void setScroll(double mouseY) {
        double barHeight = getBarHeight();
        if (barHeight == 1) {
            setScrollY(0);
        } else {
            double fraction = (mouseY - getY()) / getHeight();
            setScrollY((barHeight - 2 * fraction) / (2 * barHeight - 2) * scrollHeight);
        }
    }

    protected void setScrollY(double to) {
        scrollY = Math.clamp(to, 0, scrollHeight);
        onSetScroll.accept((int)scrollY);
    }

    public void setContentHeight(int height) {
        scrollHeight = Math.max(height - getHeight(), 0);
        setScrollY(Math.min(scrollY, scrollHeight));
    }

    protected double getBarHeight() {
        return getHeight()/((double)scrollHeight+getHeight());
    }
}
