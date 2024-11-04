package com.nettakrim.souper_secret_settings.gui.parameters;

import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

public class CalculationListWidget extends ListWidget {
    public CalculationListWidget(Calculation calculation, ListScreen<?> listScreen, int x, int width) {
        super(x, width, Text.literal(""), listScreen);

        CalculationDisplayWidget calculationDisplayWidget = new CalculationDisplayWidget(calculation, Text.literal("test"), x, width, listScreen);
        children.add(calculationDisplayWidget);
        listScreen.addSelectable(calculationDisplayWidget);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
