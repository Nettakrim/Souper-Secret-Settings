package com.nettakrim.souper_secret_settings.gui.custom;

import net.minecraft.network.chat.Component;

public class CreationBreak implements CreationEntry {
    public static final CreationBreak instance = new CreationBreak();

    private CreationBreak() {

    }

    @Override
    public Component getText() {
        return Component.empty();
    }
}
