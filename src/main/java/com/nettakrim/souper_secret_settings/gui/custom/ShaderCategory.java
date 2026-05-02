package com.nettakrim.souper_secret_settings.gui.custom;

import com.nettakrim.souper_secret_settings.shaders.custom.shader.FragColorNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.SampleNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.TexCoordNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.TextureNode;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ShaderCategory extends CreationCategory {
    @Override
    public List<CreationEntry> getChildren() {
        return List.of(
                new CreationNode(Component.literal("Texture"), TextureNode::new),
                new CreationNode(Component.literal("Sample"), SampleNode::new),
                new CreationNode(Component.literal("FragColor"), FragColorNode::new),
                new CreationNode(Component.literal("TexCoord"), TexCoordNode::new)
        );
    }

    @Override
    public Component getText() {
        return Component.literal("Shader");
    }
}
