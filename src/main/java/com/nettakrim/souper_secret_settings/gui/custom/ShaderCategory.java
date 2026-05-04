package com.nettakrim.souper_secret_settings.gui.custom;

import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.FragColorNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.SampleNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.TexCoordNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.TextureNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.maths.AdditionNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.vector.MergeNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.vector.SplitNode;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ShaderCategory extends CreationCategory {
    @Override
    public List<CreationEntry> getChildren() {
        return List.of(
                new MathCategory(),
                new VectorCategory(),
                new CreationNode(Component.literal("Texture"), TextureNode::new),
                new CreationNode(Component.literal("Sample"), SampleNode::new),
                new CreationNode(Component.literal("Screen Position"), TexCoordNode::new),
                new CreationNode(Component.literal("Output"), FragColorNode::new)
        );
    }

    @Override
    public Component getText() {
        return Component.literal("Shader");
    }

    protected static class MathCategory extends CreationCategory {
        @Override
        public List<CreationEntry> getChildren() {
            return List.of(
                    new CreationNode(Component.literal("Add"), AdditionNode::new)
            );
        }

        @Override
        public Component getText() {
            return Component.literal("Maths");
        }
    }

    protected static class VectorCategory extends CreationCategory {
        @Override
        public List<CreationEntry> getChildren() {
            return List.of(
                    new CreationNode(Component.literal("Split"), SplitNode::new),
                    new CreationNode(Component.literal("Vec2"), () -> new MergeNode(PortType.VEC2)),
                    new CreationNode(Component.literal("Vec3"), () -> new MergeNode(PortType.VEC3)),
                    new CreationNode(Component.literal("Vec4"), () -> new MergeNode(PortType.VEC4))
            );
        }

        @Override
        public Component getText() {
            return Component.literal("Vector");
        }
    }
}
