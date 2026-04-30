package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HoverButtonWidget extends SoupButtonWidget {
    protected @Nullable net.minecraft.network.chat.Component hoverText;

    protected HoverButtonWidget(Component message, @Nullable Component hoverText, OnPress onPress, int x, int y, int width, int height) {
        super(message, onPress, x, y, width, height);
        this.hoverText = hoverText;
    }

    public void setHoverText(@Nullable net.minecraft.network.chat.Component hoverText) {
        this.hoverText = hoverText;
    }

    @Override
    protected void renderContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float tickProgress) {
        renderSoupSprite(guiGraphics);
        renderText(guiGraphics);

        if (isHovered && hoverText != null && passesRangeCheck(mouseX, mouseY)) {
            SouperSecretSettingsClient.soupGui.setHoverText(hoverText);
        }
    }

    protected void renderText(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.textRenderer().acceptScrollingWithDefaultCenter(getMessage(), getX(), getX()+getWidth(), getY(), getY()+getHeight());
    }

    protected boolean passesRangeCheck(int mouseX, int mouseY) {
        return true;
    }

    public void setActiveText(@Nullable net.minecraft.network.chat.Component hoverText) {
        setHoverText(hoverText);
        active = hoverText != null;
    }

    public void deselect() {
        setFocused(false);
        isHovered = false;
    }
}
