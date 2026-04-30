package com.nettakrim.souper_secret_settings.shaders;

import dev.dannytaylor.luminance.client.events.Runnables;
import dev.dannytaylor.luminance.client.shaders.UniformInstance;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostPassInterface;
import dev.dannytaylor.luminance.common.util.Couple;
import java.util.*;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;

public class OverrideManager {
    private static Queue<Couple<ShaderData, Identifier>> currentShaders;
    private static int currentPassIndex;

    public static int currentShaderIndex;

    public static void startShaderQueue(Queue<Couple<ShaderData, Identifier>> shaderQueue) {
        if (!shaderQueue.isEmpty()) {
            currentShaders = shaderQueue;
            currentPassIndex = -1;
            currentShaderIndex = 0;
        }
    }

    private static void searchFor(PostPass postEffectPass) {
        if (currentShaders == null) return;

        while (!currentShaders.isEmpty()) {
            currentPassIndex++;
            Couple<ShaderData, Identifier> shaderData = currentShaders.peek();
            if (shaderData == null) {
                currentShaders.remove();
                continue;
            }

            if (shaderData.getFirst().active) {
                List<PostPass> currentPasses = shaderData.getFirst().postChainInterface.luminance$getPasses(shaderData.getSecond());
                // render queue is only added to when the passes *do* exist
                assert currentPasses != null;

                while (currentPassIndex < currentPasses.size()) {
                    if (currentPasses.get(currentPassIndex) == postEffectPass) {
                        return;
                    }
                    currentPassIndex++;
                }
            }

            currentShaders.remove();
            currentPassIndex = -1;

            if (!currentShaders.isEmpty()) {
                shaderData = currentShaders.peek();
                if (shaderData == null) {
                    currentShaderIndex++;
                    currentShaders.remove();
                }
            }
        }
    }

    public static class BeforeShaderRender implements Runnables.Shader {
        @Override
        public void run(PostPass postEffectPass) {
            searchFor(postEffectPass);
            if (currentShaders == null || currentShaders.isEmpty()) {
                return;
            }

            assert currentShaders.peek() != null;
            Couple<ShaderData, Identifier> shaderData = currentShaders.peek();

            PostPassInterface pass = ((PostPassInterface)postEffectPass);

            // TODO: properly account for shadergraphs
            if (pass.luminance$getCustomData(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "compiled")).isPresent()) {
                return;
            }

            Map<String, BlockData> blockDataMap = shaderData.getFirst().getPassData(shaderData.getSecond()).passBlocks.get(currentPassIndex);

            // set overrides to current soup values
            pass.luminance$getUniformBlocks().forEach((blockName, block) -> {
                BlockData blockData = blockDataMap.get(blockName);

                for (int i = 0; i < block.uniforms.size(); i++) {
                    UniformInstance instance = block.uniforms.get(i);
                    UniformData data = blockData.uniformDatas.get(i);
                    instance.override = data.override;
                    instance.config = data.config;
                }
            });
        }
    }

    public static class AfterShaderRender implements Runnables.Shader {
        @Override
        public void run(PostPass postEffectPass) {
            if (currentShaders == null || currentShaders.isEmpty()) {
                return;
            }

            Couple<ShaderData, Identifier> shaderData = currentShaders.peek();

            PostPassInterface pass = ((PostPassInterface)postEffectPass);

            // TODO: properly account for shadergraphs
            if (pass.luminance$getCustomData(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "compiled")).isPresent()) {
                return;
            }

            Map<String, BlockData> blockDataMap = shaderData.getFirst().getPassData(shaderData.getSecond()).passBlocks.get(currentPassIndex);

            // return overrides to how soup first found them
            pass.luminance$getUniformBlocks().forEach((blockName, block) -> {
                BlockData blockData = blockDataMap.get(blockName);

                for (int i = 0; i < block.uniforms.size(); i++) {
                    UniformInstance instance = block.uniforms.get(i);
                    UniformData data = blockData.uniformDatas.get(i);

                    assert data.defaultValue != null;
                    instance.override = data.defaultValue.override;
                    instance.config = data.defaultValue.config;
                }
            });
        }
    }
}
