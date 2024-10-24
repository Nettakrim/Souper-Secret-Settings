package com.nettakrim.souper_secret_settings.screen;

import com.mclegoman.luminance.client.shaders.ShaderDataloader;
import com.mclegoman.luminance.client.shaders.ShaderRegistry;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class StackScreen extends CollapseScreen {
    public final ShaderStack stack;

    public ListAdditionWidget listAdditionWidget;

    public StackScreen(ShaderStack stack) {
        super(Text.literal(""));
        this.stack = stack;
    }

    @Override
    public void init() {
        ButtonWidget toggleButton = ButtonWidget.builder(Text.literal("parameters"), (widget) -> SouperSecretSettingsClient.LOGGER.info("TODO!")).dimensions(shaderGap, shaderGap, 100, headerHeight).build();
        addDrawableChild(toggleButton);

        collapseWidgets = new ArrayList<>(stack.shaderDatas.size());
        for (ShaderData shaderData : stack.shaderDatas) {
            ShaderWidget shaderWidget = new ShaderWidget(shaderData, this, shaderGap, listWidth);
            addDrawableChild(shaderWidget);
            collapseWidgets.add(shaderWidget);
            addSelectable(shaderWidget);
        }

        listAdditionWidget = new ListAdditionWidget(SouperSecretSettingsClient.client.textRenderer, shaderGap, listWidth, 20, Text.literal("list addition"), this::getShaders, this::addShader);
        addDrawableChild(listAdditionWidget);

        updateSpacing();
    }

    @Override
    public int updateSpacing() {
        int position = super.updateSpacing();
        listAdditionWidget.setY(position);
        return position+shaderGap+listAdditionWidget.getHeight();
    }

    public List<String> getShaders() {
        List<String> shaders = new ArrayList<>(ShaderDataloader.registry.size());

        for (ShaderRegistry shaderRegistry : ShaderDataloader.registry) {
            shaders.add(shaderRegistry.getID().toString());
        }

        return shaders;
    }

    private void addShader(String shader) {
        SouperSecretSettingsClient.soupRenderer.addShader(Identifier.of(shader), 1);
        SouperSecretSettingsClient.client.setScreen(new StackScreen(SouperSecretSettingsClient.soupRenderer.getActiveStack()));
    }
}
