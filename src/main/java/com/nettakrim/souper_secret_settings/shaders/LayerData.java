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
        //TODO fix speed properly (luminance will probably do this, this seems to work close enough)
        tickDelta *= 0.325f;

        int stackSize = postProcessorStack.size();
        if (stackSize == 0) return;

        if (layerEffects.isEmpty()) {
            for (StackData stackData : postProcessorStack) {
                stackData.processor().render(tickDelta);
            }
            return;
        }

        ArrayList<AbstractLayerEffect> reversed = new ArrayList<>(layerEffects);
        Collections.reverse(reversed);

        for (AbstractLayerEffect abstractLayerEffect : reversed) {
            abstractLayerEffect.beforeStackRender(postProcessorStack, tickDelta, stackSize);
        }

        int i = 0;
        for (StackData stackData : postProcessorStack) {
            for (AbstractLayerEffect abstractLayerEffect : reversed) {
                abstractLayerEffect.beforeShaderRender(stackData, tickDelta, i);
            }

            stackData.processor().render(tickDelta);

            for (AbstractLayerEffect abstractLayerEffect : layerEffects) {
                abstractLayerEffect.afterShaderRender(stackData, tickDelta, i);
            }
            i++;
        }

        for (AbstractLayerEffect abstractLayerEffect : layerEffects) {
            abstractLayerEffect.afterStackRender(postProcessorStack, tickDelta, stackSize);
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
        return addLayerEffect(SouperSecretSettingsClient.getLayerEffect(shaderData));
    }

    public ShaderData getRandomNotTop(Random random, ArrayList<ShaderData> shaderDatas, boolean ignoreSoupFriendly) {
        int randomSize = shaderDatas.size();
        if (randomSize == 0) return null;
        if (randomSize == 1) return shaderDatas.get(0);
        int stackSize = postProcessorStack.size();
        if (stackSize == 0) return shaderDatas.get(random.nextInt(randomSize));

        ShaderData top = postProcessorStack.get(stackSize-1).data();

        for (int x = 0; x < 50; x++) {
            ShaderData s = shaderDatas.get(random.nextInt(randomSize));
            if ((ignoreSoupFriendly || s.soupFriendly) && (s != top)) return s;
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
        pop(postProcessorStack, amount);
    }

    public int removeShader(String shader) {
        int start = postProcessorStack.size();
        postProcessorStack.removeIf(x -> (x.data().id.equals(shader)));
        return start-postProcessorStack.size();
    }

    public int removeLayerEffect(String shader) {
        int start = layerEffects.size();
        layerEffects.removeIf(x -> (x.shaderData.id.equals(shader)));
        return start-layerEffects.size();
    }

    public void popLayerEffect(int amount) {
        pop(layerEffects, amount);
    }

    private void pop(ArrayList<?> list, int amount) {
        int size = list.size();
        int index = size-amount;
        if (index <= 0) {
            list.clear();
        } else {
            list.subList(index, size).clear();
        }
    }
}
