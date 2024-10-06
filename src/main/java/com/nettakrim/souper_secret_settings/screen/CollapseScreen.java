package com.nettakrim.souper_secret_settings.screen;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;

public abstract class CollapseScreen extends Screen {
    protected ArrayList<CollapseWidget> collapseWidgets;

    protected static final int listWidth = 150;
    protected static final int shaderGap = 2;
    protected static final int headerHeight = 20;
    protected static final int shaderStart = headerHeight+shaderGap*2;

    protected CollapseScreen(Text title) {
        super(title);
    }

    public <T extends Element & Selectable> void addSelectable(T child) {
        addSelectableChild(child);
    }

    public void updateSpacing() {
        int position = shaderStart;
        for (CollapseWidget collapseWidget : collapseWidgets) {
            collapseWidget.visible = true;
            collapseWidget.setY(position);
            position += collapseWidget.getHeight() + shaderGap;
        }
    }
}
