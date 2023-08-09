package com.nettakrim.souper_secret_settings.shaders;

import java.util.List;

public abstract class AbstractLayerEffect {
    public abstract void resize(int width, int height);

    public abstract void beforeStackRender(List<StackData> postProcessorStack, float tickDelta);

    public abstract void afterStackRender(List<StackData> postProcessorStack, float tickDelta);

    public abstract void beforeShaderRender(StackData stackData, float tickDelta);

    public abstract void afterShaderRender(StackData stackData, float tickDelta);
}
