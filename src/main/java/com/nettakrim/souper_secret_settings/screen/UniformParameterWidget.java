package com.nettakrim.souper_secret_settings.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class UniformParameterWidget extends ParameterTextWidget {
    public int maxWidth;
    public int minWidth;

    public DraggableTextFieldWidget widgetA;
    public DraggableTextFieldWidget widgetB;

    public String value;
    public float a;
    public float b;

    protected Consumer<UniformParameterWidget> onChange;

    public UniformParameterWidget(TextRenderer textRenderer, int x, int maxWidth, int minWidth, int height, Text message, String defaultValue) {
        super(textRenderer, x, maxWidth, height, message, defaultValue);
        this.maxWidth = maxWidth;
        this.minWidth = minWidth;
        setText(defaultValue);

        int size = (maxWidth - minWidth)/2;
        widgetA = new DraggableTextFieldWidget(textRenderer, x + minWidth, size, height, Text.literal("a-").append(message));
        widgetB = new DraggableTextFieldWidget(textRenderer, x + minWidth + size, size, height, Text.literal("b-").append(message));
        widgetA.setText("0");
        widgetB.setText("1");

        setChangedListener(this::setValue);
        widgetA.setChangedListener(this::setA);
        widgetB.setChangedListener(this::setB);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        widgetA.renderWidget(context, mouseX, mouseY, delta);
        widgetB.renderWidget(context, mouseX, mouseY, delta);
    }

    public void onChange(Consumer<UniformParameterWidget> onChange) {
        this.onChange = onChange;
    }

    protected void setValue(String value) {
        this.value = value;
        try {
            if (!value.isEmpty()) {
                Float.parseFloat(value);
            }
            setWidth(maxWidth);
            a = 0;
            b = 1;
            widgetA.visible = false;
            widgetB.visible = false;
        } catch (Exception ignored) {
            setWidth(minWidth);
            widgetA.visible = visible;
            widgetB.visible = visible;
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

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        boolean mixVisible = getWidth() == minWidth && visible;
        widgetA.visible = mixVisible;
        widgetB.visible = mixVisible;
    }
}
