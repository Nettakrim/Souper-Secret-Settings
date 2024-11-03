package com.nettakrim.souper_secret_settings.gui.parameters;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.DisplayWidget;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ParameterTextWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;

public class CalculationDisplayWidget extends DisplayWidget {
    public CalculationDisplayWidget(int count, Text name, int x, int width, ListScreen<?> listScreen) {
        super(count, name, x, width, listScreen);
    }

    @Override
    protected ClickableWidget createChildWidget(String data, int i) {
        return new ParameterTextWidget(SouperSecretSettingsClient.client.textRenderer, getX(), getWidth(), 20, Text.literal(""), data);
    }

    @Override
    protected String[] getChildData() {
        return new String[0];
    }

    @Override
    protected List<Float> getDisplayFloats() {
        return List.of();
    }
}
