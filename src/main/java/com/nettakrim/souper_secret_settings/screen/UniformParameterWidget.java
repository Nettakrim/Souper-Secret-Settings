package com.nettakrim.souper_secret_settings.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class UniformParameterWidget extends ParameterTextWidget {
    public int maxWidth;
    public int minWidth;

    public ParameterTextWidget widgetA;
    public ParameterTextWidget widgetB;

    public String value;
    public float a;
    public float b;

    protected Consumer<UniformParameterWidget> onChange;

    public UniformParameterWidget(TextRenderer textRenderer, int x, int maxWidth, int minWidth, int height, Text message) {
        super(textRenderer, x, maxWidth, height, message);
        this.maxWidth = maxWidth;
        this.minWidth = minWidth;

        int size = (maxWidth - minWidth)/2;
        widgetA = new ParameterTextWidget(textRenderer, x + minWidth, size, height, Text.literal("a-").append(message));
        widgetB = new ParameterTextWidget(textRenderer, x + minWidth + size, size, height, Text.literal("b-").append(message));
        widgetA.setText("0");
        widgetB.setText("1");

        setChangedListener(this::setValue);
        widgetA.setChangedListener(this::setA);
        widgetB.setChangedListener(this::setB);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        if (getWidth() == minWidth) {
            widgetA.renderWidget(context, mouseX, mouseY, delta);
            widgetB.renderWidget(context, mouseX, mouseY, delta);
        }
    }

    public void onChange(Consumer<UniformParameterWidget> onChange) {
        this.onChange = onChange;
    }

    protected void setValue(String value) {
        this.value = value;
        try {
            float f = Float.parseFloat(value);
            setWidth(maxWidth);
            a = 0;
            b = 1;
        } catch (Exception ignored) {
            setWidth(minWidth);
        }
        onChange();
    }

    protected void setA(String value) {
        try {
            a = Float.parseFloat(value);
            onChange();
        } catch (Exception ignored) {}
    }

    protected void setB(String value) {
        try {
            b = Float.parseFloat(value);
            onChange();
        } catch (Exception ignored) {}
    }

    protected void onChange() {
        if (onChange != null) {
            onChange.accept(this);
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        widgetA.setY(y);
        widgetB.setY(y);
    }
}
