package com.nettakrim.souper_secret_settings.gui.custom;

import com.google.common.collect.ImmutableList;
import com.nettakrim.souper_secret_settings.shaders.ChainData;
import com.nettakrim.souper_secret_settings.shaders.custom.chain.PassNode;
import com.nettakrim.souper_secret_settings.shaders.custom.chain.ReadTargetNode;
import com.nettakrim.souper_secret_settings.shaders.custom.chain.ShaderNode;
import com.nettakrim.souper_secret_settings.shaders.custom.chain.WriteTargetNode;
import dev.dannytaylor.luminance.client.data.ClientData;
import dev.dannytaylor.luminance.client.shaders.ShaderRegistryEntry;
import dev.dannytaylor.luminance.client.shaders.Shaders;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostChainInterface;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostPassInterface;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;

public class ChainCategory extends CreationCategory {
    @Override
    public List<CreationEntry> getChildren() {
        return List.of(
                new RegistryCategory(Shaders.getMainRegistryId()),
                new CreationNode(Component.literal("Custom Shader"), ShaderNode::new),
                new CreationNode(Component.literal("Read Target"), ReadTargetNode::new),
                new CreationNode(Component.literal("Write Target"), WriteTargetNode::new)
        );
    }

    @Override
    public Component getText() {
        return Component.literal("Chain");
    }

    protected static class RegistryCategory extends CreationCategory {
        private final Identifier registry;

        public RegistryCategory(Identifier registry) {
            this.registry = registry;
        }

        @Override
        public List<CreationEntry> getChildren() {
            List<ShaderRegistryEntry> entries = Shaders.getRegistry(registry);

            HashMap<String, ListCategory.Builder> namespaces = new HashMap<>();

            for (ShaderRegistryEntry shaderRegistryEntry : entries) {
                namespaces.computeIfAbsent(shaderRegistryEntry.getID().getNamespace(), ListCategory.Builder::new).add(shaderRegistryEntry.getID().getPath(), new ShaderCategory(shaderRegistryEntry));
            }

            return namespaces.values().stream().map(ListCategory.Builder::build).toList();
        }

        @Override
        public Component getText() {
            return Component.literal("From Shader");
        }
    }

    protected static class ShaderCategory extends CreationCategory {
        private final ShaderRegistryEntry shaderRegistryEntry;

        public ShaderCategory(ShaderRegistryEntry shaderRegistryEntry) {
            this.shaderRegistryEntry = shaderRegistryEntry;
        }

        @Override
        public List<CreationEntry> getChildren() {
            // this is the most expensive bit in the category, so it should be delayed as much as possible
            PostChainInterface reference = (PostChainInterface)ClientData.minecraft.getShaderManager().getPostChain(shaderRegistryEntry.getPostEffectIdentifier(false), LevelTargetBundle.SORTING_TARGETS);
            if (reference == null) {
                return List.of();
            }

            ImmutableList.Builder<CreationEntry> builder = new ImmutableList.Builder<>();

            addPasses(reference, builder, null);
            for (Identifier chain : reference.luminance$getCustomChainNames()) {
                addPasses(reference, builder, chain);
            }

            return builder.build();
        }

        private static void addPasses(PostChainInterface reference, ImmutableList.Builder<CreationEntry> builder, Identifier customChain) {
            List<PostPass> passes = reference.luminance$getPasses(customChain);

            if (passes != null) {
                for (PostPass postPass : passes) {
                    PostPassInterface postPassInterface = (PostPassInterface) postPass;
                    builder.add(new CreationNode(Component.literal(ChainData.getName(postPassInterface)), () -> new PassNode(postPassInterface)));
                }
            }
        }

        @Override
        public Component getText() {
            return Component.literal(shaderRegistryEntry.getID().getPath());
        }
    }
}
