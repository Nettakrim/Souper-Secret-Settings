package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class ListScreen<V> extends Screen {
    protected ArrayList<ListWidget> listWidgets;

    public SuggestionTextFieldWidget suggestionTextFieldWidget;

    protected static final int listWidth = 150;
    protected static final int listGap = 2;
    protected static final int headerHeight = 20;
    protected static final int listStart = headerHeight+ listGap *2;

    protected ListScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        addDrawableChild(getToggleButton());

        List<V> listValues = getListValues();
        listWidgets = new ArrayList<>(listValues.size());
        for (V value : listValues) {
            ListWidget listWidget = createListWidget(value);
            addDrawableChild(listWidget);
            listWidgets.add(listWidget);
        }

        suggestionTextFieldWidget = new SuggestionTextFieldWidget(SouperSecretSettingsClient.client.textRenderer, listGap, listWidth, 20, Text.literal("list addition"));
        suggestionTextFieldWidget.setListeners(this::getAdditions, this::addAddition);
        addDrawableChild(suggestionTextFieldWidget);

        updateSpacing();
    }

    //TODO: needs to be a proper header system
    protected abstract ButtonWidget getToggleButton();

    protected abstract List<V> getListValues();

    protected abstract ListWidget createListWidget(V value);

    public <T extends Element & Selectable> void addSelectable(T child) {
        addSelectableChild(child);
    }

    public void updateSpacing() {
        int position = listStart;
        for (CollapseWidget collapseWidget : listWidgets) {
            collapseWidget.visible = true;
            collapseWidget.setY(position);
            position += collapseWidget.getHeight() + listGap;
        }

        suggestionTextFieldWidget.setY(position);
    }

    @Override
    protected void applyBlur() {

    }

    @Override
    protected void renderDarkening(DrawContext context, int x, int y, int width, int height) {

    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public abstract List<String> getAdditions();

    public abstract void addAddition(String addition);
}
