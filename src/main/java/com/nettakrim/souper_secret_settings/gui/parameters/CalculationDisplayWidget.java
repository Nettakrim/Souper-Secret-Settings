package com.nettakrim.souper_secret_settings.gui.parameters;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.DisplayWidget;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ParameterTextWidget;
import com.nettakrim.souper_secret_settings.shaders.ParameterOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;

public class CalculationDisplayWidget extends DisplayWidget {
    public Calculation calculation;

    protected ParameterTextWidget[] outputs;

    public CalculationDisplayWidget(Calculation calculation, Text name, int x, int width, ListScreen<?> listScreen) {
        super(calculation.inputs.length, name, x, width, listScreen);
        this.calculation = calculation;
        initValues();
    }

    @Override
    protected void initValues() {
        int outputCount = calculation.outputs.length;
        outputs = new ParameterTextWidget[outputCount];

        int width = (getWidth()-displayWidth)/outputCount;
        for (int i = 0; i < outputCount; i++) {
            ParameterTextWidget parameterTextWidget = new ParameterTextWidget(SouperSecretSettingsClient.client.textRenderer, getX() + width*i, width, 20, Text.literal("input"+i), "");
            listScreen.addSelectable(parameterTextWidget);
            outputs[i] = parameterTextWidget;
        }

        super.initValues();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        for (ParameterTextWidget parameterTextWidget : outputs) {
            parameterTextWidget.renderWidget(context, mouseX, mouseY, delta);
        }

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    protected ClickableWidget createChildWidget(String data, int i) {
        ParameterTextWidget parameterTextWidget = new ParameterTextWidget(SouperSecretSettingsClient.client.textRenderer, getX(), getWidth(), 20, Text.literal(""), data);
        parameterTextWidget.setText(data);
        return parameterTextWidget;
    }

    @Override
    protected String[] getChildData() {
        String[] data = new String[calculation.inputs.length];
        for (int i = 0; i < data.length; i++) {
            ParameterOverrideSource source = calculation.inputs[i];
            data[i] = source == null ? "" : source.getString();
        }
        return data;
    }

    @Override
    protected List<Float> getDisplayFloats() {
        return List.of();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        for (ParameterTextWidget parameterTextWidget : outputs) {
            parameterTextWidget.setY(y);
        }
    }

    @Override
    protected void setExpanded(boolean to) {

    }

    public void setExpandedWithoutUpdate(boolean to) {
        expanded = to;
    }
}
