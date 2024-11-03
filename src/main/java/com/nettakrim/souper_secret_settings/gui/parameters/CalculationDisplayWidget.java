package com.nettakrim.souper_secret_settings.gui.parameters;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.DisplayWidget;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ParameterTextWidget;
import com.nettakrim.souper_secret_settings.shaders.parameters.Calculation;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;

public class CalculationDisplayWidget extends DisplayWidget {
    public Calculation calculation;

    public CalculationDisplayWidget(Calculation calculation, Text name, int x, int width, ListScreen<?> listScreen) {
        super(3, name, x, width, listScreen);
        this.calculation = calculation;
        initValues();
    }

    @Override
    protected ClickableWidget createChildWidget(String data, int i) {
        ParameterTextWidget parameterTextWidget = new ParameterTextWidget(SouperSecretSettingsClient.client.textRenderer, getX(), getWidth(), 20, Text.literal(""), data);
        parameterTextWidget.setText(data);
        return parameterTextWidget;
    }

    @Override
    protected String[] getChildData() {
        return new String[] {"a", "b", "c"};
    }

    @Override
    protected List<Float> getDisplayFloats() {
        return List.of();
    }
}
