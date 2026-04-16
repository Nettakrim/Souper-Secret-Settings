package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.shaders.UniformInstance;
import com.mclegoman.luminance.client.shaders.Uniforms;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.nettakrim.souper_secret_settings.actions.UniformChangeAction;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.DisplayWidget;
import com.nettakrim.souper_secret_settings.shaders.BlockData;
import com.nettakrim.souper_secret_settings.shaders.UniformData;
import java.util.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public class UniformWidget extends DisplayWidget {
    public BlockWidget block;

    public int uniformIndex;
    public UniformInstance uniform;

    public UniformWidget(BlockWidget block, UniformInstance uniform, Component name, int index, int x, int width, ListScreen<?> listScreen) {
        super(uniform.length(), name, x, width, listScreen);
        this.block = block;
        this.uniformIndex = index;
        this.uniform = uniform;
    }

    @Override
    protected void createChildren(int x, int width) {
        UniformData uniformData = getBlockData().uniformDatas.get(uniformIndex);
        assert uniformData.defaultValue != null;
        List<String> valueStrings = (uniformData.override).getStrings();
        List<String> defaultStrings = (uniformData.defaultValue.override).getStrings();

        for (int i = 0; i < valueStrings.size(); i++) {
            AbstractWidget widget = createChildWidget(valueStrings.get(i), defaultStrings.get(i), uniformData.config, uniformData.defaultValue.config, i);
            listScreen.addSelectable(widget);
            children.add(widget);
        }
    }

    protected AbstractWidget createChildWidget(String initialValue, String defaultValue, UniformConfig initialConfig, UniformConfig defaultConfig, int i) {
        ConfigWidget configWidget = new ConfigWidget(getX(), getWidth(), 20, Component.empty(), block.pass.shader.layer, listScreen, initialValue, defaultValue, initialConfig, defaultConfig, i);
        configWidget.onChangeListener((w) -> onValueChanged(i, w));
        return configWidget;
    }

    protected void onValueChanged(int i, ConfigWidget widget) {
        UniformData uniformData = getBlockData().uniformDatas.get(uniformIndex);

        new UniformChangeAction(uniformIndex, i, uniformData.override, uniformData.config).addToHistory();

        uniformData.override.overrideSources.set(i, widget.overrideSource);

        String prefix = i+"_";
        uniformData.config.config().keySet().removeIf((s) -> s.startsWith(prefix));
        uniformData.config.mergeWithConfig(widget.getConfig(prefix));

        widget.dragValue = null;

        listScreen.updateSpacing();
    }

    @Override
    protected List<Float> getDisplayFloats() {
        UniformData uniformData = getBlockData().uniformDatas.get(uniformIndex);

        List<Float> display = uniformData.override.getOverride(uniformData.config, Uniforms.shaderTime);
        for (int i = 0; i < display.size(); i++) {
            if (display.get(i) == null) {
                display.set(i, uniform.defaultValue.get(i).floatValue());
            }

            //allow dragging luminance:alpha/smooth to replace it with its current value
            if (expanded) {
                ConfigWidget configWidget = ((ConfigWidget)children.get(i));
                if (configWidget.getValue().startsWith("luminance:alpha")) {
                    configWidget.dragValue = display.get(i);
                }
            }
        }
        return display;
    }

    @Override
    protected boolean getStoredExpanded() {
        return getBlockData().uniformExpanded.get(uniformIndex);
    }

    @Override
    protected void setStoredExpanded(boolean to) {
        getBlockData().uniformExpanded.set(uniformIndex);
    }

    private BlockData getBlockData() {
        return block.pass.shader.shaderData.getPassData(block.pass.chain).passBlocks.get(block.pass.passIndex).get(block.blockName);
    }
}
