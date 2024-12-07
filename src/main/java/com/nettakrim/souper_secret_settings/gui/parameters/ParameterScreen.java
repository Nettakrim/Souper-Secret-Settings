package com.nettakrim.souper_secret_settings.gui.parameters;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculations;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParameterScreen extends ListScreen<Calculation> {
    public final ShaderStack stack;

    public ParameterScreen(ShaderStack stack) {
        super(Text.literal(""));
        this.stack = stack;
    }

    @Override
    protected List<Calculation> getListValues() {
        return stack.calculations;
    }

    @Override
    protected ListWidget createListWidget(Calculation value) {
        return new CalculationListWidget(value, stack,this, listGap, listWidth);
    }

    @Override
    public List<String> getAdditions() {
        List<String> calculations = new ArrayList<>(Calculations.getIds());
        Collections.sort(calculations);
        return calculations;
    }

    @Override
    public void addAddition(String addition) {
        Calculation calculation = Calculations.createCalcultion(addition);
        if (calculation != null) {
            stack.calculations.add(calculation);
            SouperSecretSettingsClient.client.setScreen(new ParameterScreen(SouperSecretSettingsClient.soupRenderer.getActiveStack()));
        }
    }
}
