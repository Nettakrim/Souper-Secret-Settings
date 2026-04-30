package com.nettakrim.souper_secret_settings.gui.parameters;

import dev.dannytaylor.luminance.client.shaders.RenderLocations;
import dev.dannytaylor.luminance.client.shaders.ShaderTime;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculations;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class ParameterScreen extends ListScreen<Calculation> {
    public final ShaderLayer layer;

    public ParameterScreen(int scrollIndex, ShaderLayer layer) {
        super(scrollIndex);
        this.layer = layer;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        ShaderTime.currentRenderLocation = SouperSecretSettingsClient.soupRenderer.getRenderLocation();
        super.render(guiGraphics, mouseX, mouseY, delta);
        ShaderTime.currentRenderLocation = RenderLocations.UI;
    }

    @Override
    protected List<Calculation> getListValues() {
        return layer.calculations;
    }

    @Override
    protected ListWidget createListWidget(Calculation value) {
        return new ParameterWidget(value, layer, this, SoupGui.listX, SoupGui.listWidth);
    }

    @Override
    public List<String> calculateAdditions() {
        List<String> calculations = new ArrayList<>(Calculations.getIds());
        Collections.sort(calculations);
        return calculations;
    }

    @Override
    public Calculation tryGetAddition(String addition) {
        return Calculations.createCalculation(addition);
    }

    @Override
    protected boolean canUseRandom() {
        return false;
    }

    @Override
    protected boolean canPreview() {
        return false;
    }

    @Override
    protected boolean matchIdentifiers() {
        return false;
    }
}
