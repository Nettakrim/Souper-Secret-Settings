package com.nettakrim.souper_secret_settings.screen;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class ScreenWrapper extends Screen {
    protected ScreenWrapper(Text title) {
        super(title);
    }

    public <T extends Element & Drawable & Selectable> void addChild(T child) {
        addDrawableChild(child);
    }
}
