package com.nettakrim.souper_secret_settings.gui.parameters;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.parameters.Calculation;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.text.Text;

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
        return new CalculationListWidget(value, this, listGap, listWidth);
    }

    @Override
    public List<String> getAdditions() {
        return List.of();
    }

    @Override
    public void addAddition(String addition) {
        stack.calculations.add(new Calculation());
        SouperSecretSettingsClient.client.setScreen(new ParameterScreen(SouperSecretSettingsClient.soupRenderer.getActiveStack()));
    }
}
