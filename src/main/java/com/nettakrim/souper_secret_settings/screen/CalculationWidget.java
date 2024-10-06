package com.nettakrim.souper_secret_settings.screen;

import net.minecraft.text.Text;

public abstract class CalculationWidget extends ParameterWidget {
    public CalculationWidget(int count, Text name, int x, int width, CollapseScreen collapseScreen) {
        super(count, name, x, width, collapseScreen);
    }
}
