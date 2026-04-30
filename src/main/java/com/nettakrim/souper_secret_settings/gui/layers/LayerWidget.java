package com.nettakrim.souper_secret_settings.gui.layers;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.LayerRenameAction;
import com.nettakrim.souper_secret_settings.actions.ShaderLoadAction;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.gui.SoupButtonWidget;
import com.nettakrim.souper_secret_settings.gui.SuggestionEditBoxWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.Toggleable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class LayerWidget extends ListWidget {
    public final ShaderLayer layer;

    private Button saveButton;
    private Button loadButton;

    private SuggestionEditBoxWidget nameWidget;

    private static final int infoHeight = 15;

    boolean saveConfirmed;

    public LayerWidget(ShaderLayer layer, ListScreen<?> listScreen, int x, int width) {
        super(x, width, getNameText(layer.name), listScreen);
        this.layer = layer;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(guiGraphics, mouseX, mouseY, delta);

        if (!expanded) {
            return;
        }
        loadButton.render(guiGraphics, mouseX, mouseY, delta);

        Component[] info = layer.getInfo();
        int infoPos = this.getY()+collapseHeight - info.length*infoHeight - 1;
        for (Component text : info) {
            int next = infoPos + infoHeight;
            guiGraphics.textRenderer().acceptScrollingWithDefaultCenter(text.copy().setStyle(Style.EMPTY.withColor((this.active ? 16777215 : 10526880) | Mth.ceil(this.alpha * 255.0F) << 24)), this.getX(), this.getX() + this.getWidth(), infoPos, next);
            infoPos = next;
        }
    }

    @Override
    protected void createChildren(int x, int width) {
        saveButton = new SoupButtonWidget(SouperSecretSettingsClient.translate("gui.save"), (buttonWidget) -> save(), x,0,width/2,20);
        loadButton = new SoupButtonWidget(SouperSecretSettingsClient.translate("gui.load"), (buttonWidget) -> load(), x + width/2,0,width/2,20);

        updateDataButtons();

        nameWidget = new SuggestionEditBoxWidget(x, width, 20, Component.nullToEmpty("layer id"), false);
        nameWidget.setListeners(() -> SouperSecretSettingsClient.soupData.getSavedLayers(true), this::setNameDisambiguate, false);
        nameWidget.submitOnLostFocus = true;
        nameWidget.setValue(layer.name);
        nameWidget.setResponder(this::setName);

        children.add(nameWidget);
        listScreen.addSelectable(nameWidget);

        children.add(saveButton);
        listScreen.addSelectable(saveButton);
        listScreen.addSelectable(loadButton);
    }

    @Override
    protected boolean getStoredExpanded() {
        return layer.expanded;
    }

    @Override
    protected void setStoredExpanded(boolean to) {
        layer.expanded = to;
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent click, boolean doubled) {
        int distance = getX()+getWidth() - (int)click.x();
        if (distance < 20 && distance > 10) {
            SouperSecretSettingsClient.soupRenderer.activeLayer = layer;
            SouperSecretSettingsClient.soupGui.updateActiveLayerMessageOrScreen();
            return;
        }
        super.onClick(click, doubled);
    }

    @Override
    protected Toggleable getToggleable() {
        return layer;
    }

    @Override
    public void updateCollapse(int y) {
        super.updateCollapse(y);
        if (expanded) {
            collapseHeight += layer.getInfo().length * infoHeight;
            loadButton.setY(saveButton.getY());
        }
        if (loadButton != null) {
            loadButton.visible = expanded;
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        if (loadButton != null) {
            loadButton.setY(saveButton.getY());
        }
    }

    private void setName(String name) {
        if (layer.name.equals(name)) return;

        new LayerRenameAction(layer).addToHistory();
        layer.name = name;
        setMessage(getNameText(name));

        updateDataButtons();
        SouperSecretSettingsClient.soupGui.setActiveLayerMessage();
    }

    private void setNameDisambiguate(String name) {
        new LayerRenameAction(layer).addToHistory();
        layer.name = name;
        layer.disambiguateName();
        setMessage(getNameText(name));

        updateDataButtons();

        nameWidget.setValue(layer.name);
        nameWidget.moveCursorToEnd(false);
        setMessage(getNameText(layer.name));
    }

    private static Component getNameText(String name) {
        return name.isBlank() ? SouperSecretSettingsClient.translate("gui.unnamed") : Component.literal(name);
    }

    private void save() {
        if (loadButton.active && !saveConfirmed) {
            setConfirm(true);
            return;
        }

        SouperSecretSettingsClient.soupData.saveLayer(layer, this::updateDataButtons);
        updateDataButtons();
        listScreen.recalculateAdditions();
        setConfirm(false);
    }

    private void load() {
        new ShaderLoadAction(layer, layer.name).addToHistory();
        layer.clear();
        SouperSecretSettingsClient.soupData.loadLayer(layer);
        updateDataButtons();
    }

    private void updateDataButtons() {
        saveButton.active = SouperSecretSettingsClient.soupData.isValidName(layer.name);
        loadButton.active = SouperSecretSettingsClient.soupData.savedLayerExists(layer.name) || (layer.name.contains(":") && SouperSecretSettingsClient.soupData.resourceLayers.containsKey(Identifier.tryParse(layer.name)));

        if (saveButton.active && !loadButton.active && layer.isEmpty()) {
            saveButton.active = false;
        }
    }

    @Override
    protected void setExpanded(boolean to) {
        super.setExpanded(to);
        setConfirm(false);
    }

    private void setConfirm(boolean to) {
        saveConfirmed = to;
        saveButton.setMessage(SouperSecretSettingsClient.translate(to ? "gui.confirm" : "gui.save"));
    }

    @Override
    public void onRemove() {
        super.onRemove();
        listScreen.removeSelectable(loadButton);
    }

    @Override
    protected int getEditState() {
        return SouperSecretSettingsClient.soupRenderer.activeLayer == layer ? 2 : 1;
    }
}
