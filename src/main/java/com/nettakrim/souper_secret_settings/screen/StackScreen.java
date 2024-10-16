package com.nettakrim.souper_secret_settings.screen;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class StackScreen extends CollapseScreen {
    public final ShaderStack stack;

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

        updateSpacing();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
         super.render(context, mouseX, mouseY, delta);
    }
}
