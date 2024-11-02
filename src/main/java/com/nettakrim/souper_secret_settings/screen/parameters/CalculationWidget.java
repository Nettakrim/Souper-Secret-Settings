package com.nettakrim.souper_secret_settings.screen.parameters;

import com.nettakrim.souper_secret_settings.screen.ListScreen;
import com.nettakrim.souper_secret_settings.screen.ParameterWidget;
import net.minecraft.text.Text;

public abstract class CalculationWidget extends ParameterWidget {
    public CalculationWidget(int count, Text name, int x, int width, ListScreen listScreen) {
        super(count, name, x, width, listScreen);
    }
}
