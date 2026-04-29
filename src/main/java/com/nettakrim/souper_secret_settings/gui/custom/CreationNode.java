package com.nettakrim.souper_secret_settings.gui.custom;

import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public record CreationNode(Component name, Supplier<Node> factory) implements CreationEntry {
    @Override
    public Component getText() {
        return name;
    }
}
