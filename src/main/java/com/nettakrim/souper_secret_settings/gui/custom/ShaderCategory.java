package com.nettakrim.souper_secret_settings.gui.custom;

import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.FragColorNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.SampleNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.TexCoordNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.TextureNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.color.HSVtoRGBNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.color.HueRotateNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.color.RGBtoHSVNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.color.SaturationNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.maths.ComplexPowNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.maths.CrossNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.maths.FloatOutputMathsNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.maths.DynamicOutputMathsNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.vector.FloatNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.vector.MergeNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.vector.SplitNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.vector.VectorNode;
import com.nettakrim.souper_secret_settings.shaders.custom.shader.world.LinearizeDepthNode;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ShaderCategory extends CreationCategory {
    @Override
    public List<CreationEntry> getChildren() {
        return List.of(
                new VectorCategory(),
                new MathCategory(),
                new RoundingCategory(),
                new TrigonometryCategory(),
                new ColorCategory(),
                new WorldCategory(),
                CreationBreak.instance,
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

    protected static class VectorCategory extends CreationCategory {
        @Override
        public List<CreationEntry> getChildren() {
            return List.of(
                    new CreationNode(Component.literal("Float"), FloatNode::new),
                    new CreationNode(Component.literal("Vector 2"), () -> new VectorNode(PortType.VEC2)),
                    new CreationNode(Component.literal("Vector 3"), () -> new VectorNode(PortType.VEC3)),
                    new CreationNode(Component.literal("Vector 4"), () -> new VectorNode(PortType.VEC4)),
                    new CreationNode(Component.literal("Split"), SplitNode::new),
                    new CreationNode(Component.literal("Merge"), MergeNode::new),

                    CreationBreak.instance,

                    DynamicOutputMathsNode.get(Component.literal("Normalize"), "normalize(%s)"),
                    FloatOutputMathsNode.get(Component.literal("Length"), "length(%s)"),
                    FloatOutputMathsNode.get(Component.literal("Distance"), "distance(%s, %s)"),
                    FloatOutputMathsNode.get(Component.literal("Dot"), "dot(%s, %s)"),
                    new CreationNode(Component.literal("Cross"), CrossNode::new),
                    DynamicOutputMathsNode.get(Component.literal("Reflect"), "reflect(%s, %s)")
            );
        }

        @Override
        public Component getText() {
            return Component.literal("Vector");
        }
    }

    protected static class MathCategory extends CreationCategory {
        @Override
        public List<CreationEntry> getChildren() {
            return List.of(
                    DynamicOutputMathsNode.get(Component.literal("Add"), "%s + %s"),
                    DynamicOutputMathsNode.get(Component.literal("Subtract"), "%s - %s"),
                    DynamicOutputMathsNode.get(Component.literal("Multiply"), "%s * %s"),
                    DynamicOutputMathsNode.get(Component.literal("Divide"), "%s / %s"),

                    CreationBreak.instance,

                    DynamicOutputMathsNode.get(Component.literal("Pow"), "pow(%s, %s)", "Base", "Exponent"),
                    DynamicOutputMathsNode.get(Component.literal("Exp"), "exp(%s)", "Exponent"),
                    DynamicOutputMathsNode.get(Component.literal("Log"), "ln(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Square Root"), "sqrt(%s)"),
                    new CreationNode(Component.literal("Complex Pow"), ComplexPowNode::new),

                    CreationBreak.instance,

                    DynamicOutputMathsNode.get(Component.literal("Linear Mix"), "mix(%s, %s, %s)", "A", "B", "Mix")
            );
        }

        @Override
        public Component getText() {
            return Component.literal("Maths");
        }
    }

    protected static class RoundingCategory extends CreationCategory {
        @Override
        public List<CreationEntry> getChildren() {
            return List.of(
                    DynamicOutputMathsNode.get(Component.literal("Floor"), "floor(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Round"), "round(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Ceil"), "ceil(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Fract"), "fract(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Mod"), "mod(%s, %s)"), // %% escapes % if ints are ever natively supported

                    CreationBreak.instance,

                    DynamicOutputMathsNode.get(Component.literal("Min"), "min(%s, %s)"),
                    DynamicOutputMathsNode.get(Component.literal("Max"), "max(%s, %s)"),
                    DynamicOutputMathsNode.get(Component.literal("Clamp"), "clamp(%s, %s, %s)", "Value", "Min", "Max"),
                    DynamicOutputMathsNode.get(Component.literal("Clamp01"), "clamp(%s, 0.0, 1.0)", "Value"),

                    CreationBreak.instance,

                    DynamicOutputMathsNode.get(Component.literal("Absolute"), "abs(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Sign"), "sign(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Step"), "step(%s, %s)", "Edge", "Value"),
                    DynamicOutputMathsNode.get(Component.literal("Smooth Step"), "smoothstep(%s, %s, %s)", "Edge A", "Edge B", "Value")
            );
        }

        @Override
        public Component getText() {
            return Component.literal("Rounding");
        }
    }

    protected static class TrigonometryCategory extends CreationCategory {
        @Override
        public List<CreationEntry> getChildren() {
            return List.of(
                    DynamicOutputMathsNode.get(Component.literal("Sin"), "sin(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Cos"), "cos(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Tan"), "tan(%s)"),

                    CreationBreak.instance,

                    DynamicOutputMathsNode.get(Component.literal("Arcsin"), "asin(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Arccos"), "acos(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Arctan"), "atan(%s)"),

                    CreationBreak.instance,

                    DynamicOutputMathsNode.get(Component.literal("Hyperbolic Sin"), "sinh(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Hyperbolic Cos"), "cosh(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Hyperbolic Tan"), "tanh(%s)"),

                    CreationBreak.instance,

                    DynamicOutputMathsNode.get(Component.literal("Hyperbolic Arcsin"), "asinh(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Hyperbolic Arccos"), "acosh(%s)"),
                    DynamicOutputMathsNode.get(Component.literal("Hyperbolic Arctan"), "atanh(%s)")
            );
        }

        @Override
        public Component getText() {
            return Component.literal("Trigonometry");
        }
    }

    protected static class ColorCategory extends CreationCategory {
        @Override
        public List<CreationEntry> getChildren() {
            return List.of(
                    new CreationNode(Component.literal("Hue Rotate"), HueRotateNode::new),
                    new CreationNode(Component.literal("Saturation"), SaturationNode::new),
                    new CreationNode(Component.literal("RGB to HSV"), RGBtoHSVNode::new),
                    new CreationNode(Component.literal("HSV to RGB"), HSVtoRGBNode::new)
            );
        }

        @Override
        public Component getText() {
            return Component.literal("Color");
        }
    }

    protected static class WorldCategory extends CreationCategory {
        @Override
        public List<CreationEntry> getChildren() {
            return List.of(
                    new CreationNode(Component.literal("Linearize Depth"), LinearizeDepthNode::new)
            );
        }

        @Override
        public Component getText() {
            return Component.literal("World");
        }
    }
}
