package com.nettakrim.souper_secret_settings.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class StackScreen extends Screen {
    public StackScreen() {
        super(Text.literal(""));
    }

    @Override
    public void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("a"), null).dimensions(10, 10, 10, 10).build());
    }
}
