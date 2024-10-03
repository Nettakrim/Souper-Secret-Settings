package com.nettakrim.souper_secret_settings.screen;

import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class StackScreen extends Screen {
    public final ShaderStack stack;

    private ArrayList<ShaderWidget> shaderWidgets;

    private static final int shaderGap = 5;
    private static final int shaderStart = 10;

    public StackScreen(ShaderStack stack) {
        super(Text.literal(""));
        this.stack = stack;
    }

    @Override
    public void init() {
        shaderWidgets = new ArrayList<>(stack.shaderDatas.size());
        for (ShaderData shaderData : stack.shaderDatas) {
            ShaderWidget shaderWidget = new ShaderWidget(shaderData);
            addDrawableChild(shaderWidget);
            addSelectableChild(shaderWidget);
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
            position += shaderWidget.getHeight()+shaderGap;
        }
    }

    @Override
    protected void applyBlur() {

    }

    @Override
    protected void renderDarkening(DrawContext context, int x, int y, int width, int height)  {

    }
}
