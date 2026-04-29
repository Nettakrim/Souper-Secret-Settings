package com.nettakrim.souper_secret_settings.gui.parameters;

import dev.dannytaylor.luminance.client.shaders.overrides.OverrideSource;
import com.nettakrim.souper_secret_settings.actions.ArrSetAction;
import com.nettakrim.souper_secret_settings.gui.DisplayWidget;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ParameterTextWidget;
import com.nettakrim.souper_secret_settings.shaders.ParameterOverrideSource;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CalculationWidget extends DisplayWidget {
    public Calculation calculation;
    protected ShaderLayer layer;

    protected ParameterTextWidget[] outputs;

    public CalculationWidget(Calculation calculation, ShaderLayer layer, Component name, int x, int width, ListScreen<?> listScreen) {
        super(calculation.inputs.length, name, x, width, listScreen);
        this.calculation = calculation;
        this.layer = layer;
        active = false;
    }

    @Override
    protected void createChildren(int x, int width) {
        int outputCount = calculation.outputs.length;
        outputs = new ParameterTextWidget[outputCount];

        int split = (getWidth()-displayWidth)/outputCount;
        for (int i = 0; i < outputCount; i++) {
            ParameterTextWidget parameterTextWidget = new ParameterTextWidget(getX() + split*i, split, 20, Component.literal("output"+i), layer, "");
            listScreen.addSelectable(parameterTextWidget);
            int finalI = i;
            parameterTextWidget.setValue(calculation.outputs[i]);
            parameterTextWidget.setResponder((s) -> onOutputChanged(finalI, s));
            outputs[i] = parameterTextWidget;
        }

        for (int i = 0; i < calculation.inputs.length; i++) {
            AbstractWidget widget = createChildWidget(calculation.inputs[i], i);
            listScreen.addSelectable(widget);
            children.add(widget);
        }
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        for (ParameterTextWidget parameterTextWidget : outputs) {
            parameterTextWidget.renderWidget(context, mouseX, mouseY, delta);
        }

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    protected AbstractWidget createChildWidget(OverrideSource data, int i) {
        String value = data.getString();
        return calculation.createWidget(getX(), getWidth(), i, layer, value, (s) -> onInputChanged(i, s));
    }

    @Override
    protected List<Float> getDisplayFloats() {
        return calculation.getLastOutput();
    }

    protected void onOutputChanged(int i, String s) {
        new ArrSetAction<>(calculation.outputs, i).addToHistory();
        calculation.outputs[i] = s;
    }

    protected void onInputChanged(int i, String s) {
        new ArrSetAction<>(calculation.inputs, i).addToHistory();
        calculation.inputs[i] = ParameterOverrideSource.parameterSourceFromString(s);
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

    @Override
    protected boolean getStoredExpanded() {
        return calculation.expanded;
    }

    @Override
    protected void setStoredExpanded(boolean to) {

    }

    public void setExpandedWithoutUpdate(boolean to) {
        expanded = to;
        tryCreateChildren();
        for (ParameterTextWidget parameterTextWidget : outputs) {
            parameterTextWidget.setVisible(expanded);
        }
    }
}
