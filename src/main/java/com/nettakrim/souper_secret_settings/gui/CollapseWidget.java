package com.nettakrim.souper_secret_settings.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class CollapseWidget extends AbstractWidget implements ListChild {
    protected boolean expanded;

    protected final List<AbstractWidget> children = new ArrayList<>();

    protected ListScreen<?> listScreen;

    protected int offset;

    protected int collapseHeight;

    private boolean initExpanded = true;
    private boolean initChildren = true;

    public CollapseWidget(int x, int width, Component message, ListScreen<?> listScreen) {
        super(x, 0, width, 20, message);

        this.listScreen = listScreen;

        visible = false;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (expanded) {
            for (AbstractWidget clickableWidget : children) {
                ((Renderable)clickableWidget).render(guiGraphics, mouseX, mouseY, delta);
            }
        }
    }

    protected void tryCreateChildren() {
        if (expanded && initChildren) {
            initChildren = false;
            createChildren(getX(), getWidth());
        }
    }

    protected abstract void createChildren(int x, int width);

    public void updateCollapse(int y) {
        if (initExpanded) {
            initExpanded = false;
            expanded = getStoredExpanded();
            tryCreateChildren();
        }

        offset = y;

        int height = getHeight();
        if (expanded) {
            for (AbstractWidget widget : children) {
                setVisible(widget, true);
                if (widget instanceof CollapseWidget collapseWidget) {
                    collapseWidget.updateCollapse(height + y);
                }
                height += getCollapseHeight(widget);
            }
        } else {
            for (AbstractWidget widget : children) {
                setVisible(widget, false);
            }
        }
        collapseHeight = height;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        int height = getHeight();
        if (expanded) {
            for (AbstractWidget widget : children) {
                widget.setY(height + y);
                height += getCollapseHeight(widget);
            }
        }
    }

    protected static void setVisible(AbstractWidget clickableWidget, boolean visible) {
        if (clickableWidget instanceof EditBox textFieldWidget) {
            textFieldWidget.setVisible(visible);
        } else {
            clickableWidget.visible = visible;
        }

        if (!visible && clickableWidget instanceof CollapseWidget collapseWidget) {
            for (AbstractWidget child : collapseWidget.children) {
                if (child.visible) {
                    setVisible(child, false);
                }
            }
        }
    }

    protected static int getCollapseHeight(AbstractWidget widget) {
        if (widget instanceof ListChild listChild) {
            return listChild.getCollapseHeight();
        } else {
            return widget.getHeight();
        }
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent click, boolean doubled) {
        setExpanded(!expanded);
    }

    protected void setExpanded(boolean to) {
        expanded = to;
        setStoredExpanded(to);
        tryCreateChildren();
        listScreen.updateSpacing();
    }

    @Override
    public int getCollapseHeight() {
        return collapseHeight;
    }

    @Override
    public void onRemove() {
        listScreen.removeSelectable(this);
        for (AbstractWidget clickableWidget : children) {
            if (clickableWidget instanceof ListChild listChild) {
                listChild.onRemove();
            } else {
                listScreen.removeSelectable(clickableWidget);
            }
        }
    }

    protected abstract boolean getStoredExpanded();

    protected abstract void setStoredExpanded(boolean to);

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
