package com.nettakrim.souper_secret_settings.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class CollapseWidget extends ClickableWidget {
    protected boolean expanded;

    protected final List<ClickableWidget> children = new ArrayList<>();

    protected static final int baseHeight = 20;

    protected ListScreen<?> listScreen;

    public CollapseWidget(int x, int width, Text message, ListScreen<?> listScreen) {
        super(x, 0, width, baseHeight, message);

        this.listScreen = listScreen;

        visible = false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (expanded) {
            for (ClickableWidget clickableWidget : children) {
                ((Drawable)clickableWidget).render(context, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        int height = baseHeight;
        if (expanded) {
            for (ClickableWidget widget : children) {
                setVisible(widget, true);
                widget.setY(height + y);
                height += widget.getHeight();
            }
        } else {
            for (ClickableWidget widget : children) {
                setVisible(widget, false);
            }
        }
        setHeight(height);
    }

    protected static void setVisible(ClickableWidget clickableWidget, boolean visible) {
        if (clickableWidget instanceof TextFieldWidget textFieldWidget) {
            textFieldWidget.setVisible(visible);
        } else {
            clickableWidget.visible = visible;
        }

        if (!visible && clickableWidget instanceof CollapseWidget collapseWidget) {
            for (ClickableWidget child : collapseWidget.children) {
                if (child.visible) {
                    setVisible(child, false);
                }
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (mouseY < getY()+baseHeight) {
            setExpanded(!expanded);
        }
    }

    protected void setExpanded(boolean to) {
        expanded = to;
        listScreen.updateSpacing();
    }
}
