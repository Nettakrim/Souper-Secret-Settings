package com.nettakrim.souper_secret_settings.screen;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class StackScreen extends ScreenWrapper {
    public final ShaderStack stack;

    private ArrayList<ShaderWidget> shaderWidgets;

    private static final int listWidth = 150;
    private static final int shaderGap = 2;
    private static final int headerHeight = 20;
    private static final int shaderStart = headerHeight+shaderGap*2;

    public StackScreen(ShaderStack stack) {
        super(Text.literal(""));
        this.stack = stack;
    }

    @Override
    public void init() {
        ButtonWidget toggleButton = ButtonWidget.builder(Text.literal("parameters"), (widget) -> SouperSecretSettingsClient.LOGGER.info("TODO!")).dimensions(shaderGap, shaderGap, 100, headerHeight).build();
        addDrawableChild(toggleButton);

        shaderWidgets = new ArrayList<>(stack.shaderDatas.size());
        for (ShaderData shaderData : stack.shaderDatas) {
            ShaderWidget shaderWidget = new ShaderWidget(shaderData, this, shaderGap, listWidth);
            addDrawableChild(shaderWidget);
            shaderWidgets.add(shaderWidget);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateSpacing();

        super.render(context, mouseX, mouseY, delta);
    }

    private void updateSpacing() {
        int position = shaderStart;
        for (ShaderWidget shaderWidget : shaderWidgets) {
            shaderWidget.setY(position);
            position += shaderWidget.getSize() + shaderGap;
        }
    }

    @Override
    protected void applyBlur() {

    }

    @Override
    protected void renderDarkening(DrawContext context, int x, int y, int width, int height) {

    }
}
