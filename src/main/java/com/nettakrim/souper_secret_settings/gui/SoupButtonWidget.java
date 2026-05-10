package com.nettakrim.souper_secret_settings.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SoupButtonWidget extends Button {
    private boolean isNodeScreen = false;

    public SoupButtonWidget(Component component, OnPress onPress, int x, int y, int width, int height) {
        super(x, y, width, height, component, onPress, Button.DEFAULT_NARRATION);
    }

    protected void renderSoupSprite(GuiGraphics guiGraphics) {
        SoupGui.drawButton(guiGraphics, isNodeScreen, getX(), getY(), getWidth(), getHeight(), this.active, isHighlighted());
    }

    @Override
    protected void renderContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float tickProgress) {
        renderSoupSprite(guiGraphics);
        this.renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
    }

    protected boolean isHighlighted() {
        return active && this.isHoveredOrFocused();
    }

    public void inNodeScreen() {
        isNodeScreen = true;
    }
}
