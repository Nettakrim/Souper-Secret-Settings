package com.nettakrim.souper_secret_settings.shaders;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Collections;

public class LayerData {
    public final ArrayList<StackData> postProcessorStack = new ArrayList<>();

    public final ArrayList<AbstractLayerEffect> layerEffects = new ArrayList<>();

    public void render(float tickDelta) {
        if (postProcessorStack.size() == 0) return;

        if (layerEffects.size() == 0) {
            for (StackData stackData : postProcessorStack) {
                stackData.processor().render(tickDelta);
            }
            return;
        }

        ArrayList<AbstractLayerEffect> reversed = new ArrayList<>(layerEffects);
        Collections.reverse(reversed);

        for (AbstractLayerEffect abstractLayerEffect : reversed) {
            abstractLayerEffect.beforeStackRender(postProcessorStack, tickDelta);
        }

        for (StackData stackData : postProcessorStack) {
            for (AbstractLayerEffect abstractLayerEffect : reversed) {
                abstractLayerEffect.beforeShaderRender(stackData, tickDelta);
            }

            stackData.processor().render(tickDelta);

            for (AbstractLayerEffect abstractLayerEffect : layerEffects) {
                abstractLayerEffect.afterShaderRender(stackData, tickDelta);
            }
        }

        for (AbstractLayerEffect abstractLayerEffect : layerEffects) {
            abstractLayerEffect.afterStackRender(postProcessorStack, tickDelta);
        }
    }

    public void clear() {
        postProcessorStack.clear();
        layerEffects.clear();
    }

    public int getShaderCount() {
        return postProcessorStack.size();
    }

    public boolean stackShaderData(ShaderData shaderData, int stack) {
        if (stack == 0) {
            clear();
            stack = 1;
        }

        PostEffectProcessor processor = SouperSecretSettingsClient.getPostProcessor(shaderData.shader);
        if (processor == null) return false;

        StackData data = new StackData(processor, shaderData);
        for (int x = 0; x < stack; x++) {
            postProcessorStack.add(data);
        }
        return true;
    }

    public boolean addLayerEffect(AbstractLayerEffect layerEffect) {
        if (layerEffect == null) return false;
        layerEffects.add(layerEffect);
        return true;
    }

    public boolean addLayerEffectFromShader(ShaderData shaderData) {
        PostLayerEffect postLayerEffect = SouperSecretSettingsClient.getLayerEffect(shaderData.shader);
        if (postLayerEffect == null) return false;

        layerEffects.add(postLayerEffect);
        return true;
    }

    public ShaderData getRandomNotTop(Random random, ArrayList<ShaderData> shaderDatas) {
        int randomSize = shaderDatas.size();
        if (randomSize == 0) return null;
        int stackSize = postProcessorStack.size();
        if (stackSize == 0) return shaderDatas.get(random.nextInt(randomSize));

        ShaderData top = postProcessorStack.get(stackSize-1).data();

        for (int x = 0; x < 50; x++) {
            ShaderData s = shaderDatas.get(random.nextInt(randomSize));
            if (s != top || randomSize == 1) return s;
        }
        return shaderDatas.get(random.nextInt(randomSize));
    }

    public void resize(int width, int height) {
        for (StackData stackData : postProcessorStack) {
            stackData.processor().setupDimensions(width, height);
        }
        for (AbstractLayerEffect layerEffect : layerEffects) {
            layerEffect.resize(width, height);
        }
    }

    public void pop(int amount) {
        int size = postProcessorStack.size();
        int index = size-amount;
        if (index <= 0) {
            postProcessorStack.clear();
        } else {
            postProcessorStack.subList(index, size).clear();
        }
    }

    public int remove(String shader) {
        int start = postProcessorStack.size();
        postProcessorStack.removeIf(x -> (x.data().id.equals(shader)));
        return start-postProcessorStack.size();
    }
}
