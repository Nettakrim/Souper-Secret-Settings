package com.nettakrim.souper_secret_settings.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;

public class SoupButtonWidget extends Button {
    public SoupButtonWidget(Component component, OnPress onPress, int x, int y, int width, int height) {
        super(x, y, width, height, component, onPress, Button.DEFAULT_NARRATION);
    }

    protected void renderSoupSprite(GuiGraphics guiGraphics) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SoupGui.BUTTON_TEXTURES.get(this.active, isHighlighted()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
    }

    @Override
    protected void renderContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float tickProgress) {
        renderSoupSprite(guiGraphics);
        this.renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
    }

    protected boolean isHighlighted() {
        return active && this.isHoveredOrFocused();
    }
}
