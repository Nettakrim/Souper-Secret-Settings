package com.nettakrim.souper_secret_settings.gui.parameters;

import com.nettakrim.souper_secret_settings.gui.ParameterTextWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class CalculationInputWidget extends ParameterTextWidget {
    private final int textWidth;

    public CalculationInputWidget(int x, int width, int height, Component message, ShaderLayer layer, String defaultValue) {
        super(x + width/3, width - width/3, height, message, layer, defaultValue);
        this.textWidth = width/3;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.textRenderer().acceptScrollingWithDefaultCenter(getMessage().copy().setStyle(Style.EMPTY.withColor((this.active ? 16777215 : 10526880) | Mth.ceil(this.alpha * 255.0F) << 24)), this.getX()-textWidth, this.getX(), this.getY(), this.getY()+20);
    }
}
