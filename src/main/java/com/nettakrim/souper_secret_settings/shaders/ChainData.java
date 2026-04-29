package com.nettakrim.souper_secret_settings.shaders;

import dev.dannytaylor.luminance.client.shaders.UniformBlock;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostPassInterface;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import java.util.*;

import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;

public class ChainData {
    public final ArrayList<Map<String, BlockData>> passBlocks;
    public BitSet passesExpanded;

    private static final Identifier overridePath = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "override");

    public ChainData(List<PostPass> passes) {
        this.passBlocks = new ArrayList<>(passes.size());
        this.passesExpanded = new BitSet(passes.size());

        for (PostPass pass : passes) {
            PostPassInterface postPass = (PostPassInterface)pass;
            Map<String, BlockData> blocks = new HashMap<>(postPass.luminance$getUniformBlocks().size());

            for (Map.Entry<String, UniformBlock> entry : postPass.luminance$getUniformBlocks().entrySet()) {
                blocks.put(entry.getKey(), new BlockData(entry.getValue(), postPass, getDataPath(entry.getKey())));
            }

            this.passBlocks.add(blocks);
        }
    }

    public static String getName(PostPassInterface pass) {
        // if the vertex shader isnt core/screenquad, then its probably important
        RenderPipeline pipeline = pass.luminance$getPipeline();
        Identifier identifier = pipeline.getVertexShader().equals(Identifier.withDefaultNamespace("core/screenquad")) ? pipeline.getFragmentShader() : pipeline.getVertexShader();
        return identifier.toString().replace(":post/",":");
    }

    private static Identifier getDataPath(String config) {
        return overridePath.withPath(config.toLowerCase(Locale.ROOT));
    }
}
