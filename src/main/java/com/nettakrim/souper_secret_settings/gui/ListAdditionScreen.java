package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;

public class ListAdditionScreen<V> extends ScrollScreen {
    public ListScreen<V> listScreen;

    protected List<AbstractWidget> children;

    protected String lastAddition;

    protected int headerHeight;

    protected ListAdditionScreen(ListScreen<V> listScreen) {
        super(Component.empty());
        this.listScreen = listScreen;
        this.lastAddition = null;
    }

    @Override
    protected void init() {
        headerHeight = createHeader();
        createScrollWidget(headerHeight);

        children = new ArrayList<>();
        for (String addition : listScreen.getAdditions()) {
            createAdditionButton(addition);
        }

        scrollWidget.setContentHeight(children.size()*(20+SoupGui.listGap) - SoupGui.listGap);
    }

    protected int createHeader() {
        addRenderableWidget(new SoupButtonWidget(Component.translatable("gui.back"), (widget) -> onClose(), SoupGui.listGap, SoupGui.listGap, SoupGui.headerWidthSmall, 20));

        return 20+SoupGui.listGap*2;
    }

    protected void createAdditionButton(String addition) {
        AdditionButton additionButton = new AdditionButton(addition, listScreen.getAdditionText(addition), this::add, SoupGui.listWidth, 20, SoupGui.listX);
        if (listScreen.canRemoveAddition(addition)) {
            additionButton.addRemoveListener(this::removeAddition);
        }
        children.add(additionButton);
        addWidget(additionButton);
    }

    protected void add(String addition) {
        if (lastAddition != null) {
            if (lastAddition.equals(addition) && !(listScreen.canUseRandom() && lastAddition.startsWith("random"))) {
                onClose();
                return;
            }

            SouperSecretSettingsClient.actions.undo();
            listScreen.listWidgets.removeLast();
        }

        if (listScreen.addAddition(addition) == null) {
            lastAddition = null;
        } else {
            lastAddition = addition;
        }

        if (!listScreen.canPreview()) {
            onClose();
        }
    }

    @Override
    public void renderScrollables(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        for (Renderable drawable : children) {
            drawable.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public void setScroll(int scroll) {
        int y = headerHeight - scroll;
        for (AbstractWidget widget : children) {
            widget.setY(y);
            y += widget.getHeight()+SoupGui.listGap;
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(listScreen);
    }

    protected void removeAddition(AdditionButton button) {
        removeWidget(button);
        children.remove(button);
        scrollWidget.setContentHeight(children.size()*(20+SoupGui.listGap) - SoupGui.listGap);

        listScreen.removeAddition(button.addition);
    }
}
