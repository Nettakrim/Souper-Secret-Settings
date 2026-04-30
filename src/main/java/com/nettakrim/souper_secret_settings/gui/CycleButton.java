package com.nettakrim.souper_secret_settings.gui;


import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CycleButton extends SoupButtonWidget {
    protected final Consumer<Integer> advance;
    protected final Supplier<net.minecraft.network.chat.Component> getText;

    public CycleButton(Supplier<Component> getText, Consumer<Integer> advance, int width, int x) {
        super(getText.get(), (button) -> {}, x, 0, width, 20);
        this.advance = advance;
        this.getText = getText;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        advance.accept(input.hasShiftDown() ? -1 : 1);
        setMessage(getText.get());
    }

    @Override
    protected void renderContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float tickProgress) {
        renderSoupSprite(guiGraphics);
        renderDefaultLabel(guiGraphics.textRenderer());
    }

    public static int cycleInt(int value, int max) {
        if (value < 0) return max;
        if (value > max) return 0;
        return value;
    }
}
