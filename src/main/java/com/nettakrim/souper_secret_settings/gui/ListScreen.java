package com.nettakrim.souper_secret_settings.gui;

import dev.dannytaylor.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.ListAddAction;
import com.nettakrim.souper_secret_settings.actions.ListRemoveAction;
import com.nettakrim.souper_secret_settings.actions.ListShiftAction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class ListScreen<V> extends ScrollScreen {
    protected ArrayList<ListWidget> listWidgets;

    protected SuggestionEditBoxWidget suggestionTextFieldWidget;
    protected Button suggestionScreenButton;

    protected int currentListSize;

    private final int scrollIndex;

    protected List<String> additions;

    protected ListScreen(int scrollIndex) {
        super(Component.empty());
        this.scrollIndex = scrollIndex;
        this.additions = null;
    }

    @Override
    protected void init() {
        createScrollWidget(createHeader());

        List<V> listValues = getListValues();
        listWidgets = new ArrayList<>(listValues.size());
        for (V value : listValues) {
            ListWidget listWidget = createListWidget(value);
            addWidget(listWidget);
            listWidgets.add(listWidget);
        }

        suggestionTextFieldWidget = new SuggestionEditBoxWidget(SoupGui.listX, SoupGui.listWidth-20-SoupGui.listGap, 20, Component.literal("list addition"), false);
        suggestionTextFieldWidget.setListeners(this::getAdditions, this::addAddition, matchIdentifiers());
        addRenderableWidget(suggestionTextFieldWidget);

        suggestionScreenButton = new SoupButtonWidget(SouperSecretSettingsClient.translate("gui.addition"), (widget) -> enterAdditionScreen(), SoupGui.listX+SoupGui.listWidth-20, 0, 20, 20);
        addRenderableWidget(suggestionScreenButton);

        updateSpacing();
        if (scrollIndex >= 0) {
            scrollWidget.offsetScroll(SouperSecretSettingsClient.soupGui.currentScroll[scrollIndex]);
        }
    }

    @Override
    public void renderScrollables(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        for (Renderable drawable : listWidgets) {
            drawable.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    protected int createHeader() {
        for (AbstractWidget clickableWidget : SouperSecretSettingsClient.soupGui.getHeader()) {
            addRenderableWidget(clickableWidget);
        }

        return SoupGui.listStart;
    }

    protected abstract List<V> getListValues();

    protected abstract ListWidget createListWidget(V value);

    public <T extends GuiEventListener & NarratableEntry> void addSelectable(T child) {
        addWidget(child);
    }

    public void removeSelectable(GuiEventListener child) {
        removeWidget(child);
    }

    public void updateSpacing() {
        currentListSize = SoupGui.listStart;
        for (CollapseWidget collapseWidget : listWidgets) {
            collapseWidget.visible = true;
            collapseWidget.updateCollapse(currentListSize);
            currentListSize += collapseWidget.getCollapseHeight() + SoupGui.listGap;
        }

        scrollWidget.setContentHeight(currentListSize-SoupGui.listStart + suggestionTextFieldWidget.getHeight());
    }

    @Override
    public void setScroll(int scroll) {
        for (CollapseWidget collapseWidget : listWidgets) {
            collapseWidget.setY(collapseWidget.offset - scroll);
        }

        suggestionTextFieldWidget.setY(currentListSize - scroll);
        suggestionScreenButton.setY(suggestionTextFieldWidget.getY());
        if (scrollIndex >= 0) {
            SouperSecretSettingsClient.soupGui.currentScroll[scrollIndex] = scroll;
        }
    }

    public V addAddition(String addition) {
        V entry = tryGetAddition(addition);
        if (entry != null) {
            List<V> list = getListValues();
            int position = list.size();
            if (useHistory()) {
                new ListAddAction<>(list, entry, position).addToHistory();
            }
            ListWidget listWidget = createListWidget(entry);
            addEntry(position, entry, listWidget);
            addSelectable(listWidget);
            updateSpacing();
        }
        suggestionTextFieldWidget.setValue("");
        return entry;
    }

    public List<String> getAdditions() {
        if (additions == null) {
            additions = calculateAdditions();
        }

        return additions;
    }

    public abstract List<String> calculateAdditions();

    public void recalculateAdditions() {
        additions = null;
    }

    @Nullable
    public abstract V tryGetAddition(String addition);

    public void swapEntry(ListWidget listWidget, int direction) {
        int index = listWidgets.indexOf(listWidget);
        int newIndex = Mth.clamp(index+direction, 0, listWidgets.size()-1);

        if (index != newIndex) {
            if (useHistory()) {
                new ListShiftAction<>(getListValues(), index, direction).addToHistory();
            }

            V entry = removeEntry(index, false);
            addEntry(newIndex, entry, listWidget);

            updateSpacing();
        }
    }

    public void removeEntry(ListWidget listWidget) {
        int index = listWidgets.indexOf(listWidget);
        if (useHistory()) {
            new ListRemoveAction<>(getListValues(), index).addToHistory();
        }

        removeWidget(listWidget);
        removeEntry(index, true);

        updateSpacing();
    }

    protected void addEntry(int index, V entry, ListWidget widget) {
        listWidgets.add(index, widget);
        getListValues().add(index, entry);
    }

    protected V removeEntry(int index, boolean delete) {
        ListWidget listWidget = listWidgets.remove(index);
        if (delete) {
            listWidget.onRemove();
        }
        return getListValues().remove(index);
    }

    protected void enterAdditionScreen() {
        minecraft.setScreen(new ListAdditionScreen<>(this));
    }

    protected boolean canRemoveAddition(String addition) {
        return false;
    }

    protected void removeAddition(String addition) {
        additions.remove(addition);
    }

    protected abstract boolean canUseRandom();

    protected boolean canPreview() {
        return true;
    }

    protected Couple<Component,Component> getAdditionText(String addition) {
        return new Couple<>(Component.literal(addition), null);
    }

    protected abstract boolean matchIdentifiers();

    @Override
    public void onClose() {
        SouperSecretSettingsClient.soupGui.onClose();
    }

    protected boolean useHistory() {
        return true;
    }
}
