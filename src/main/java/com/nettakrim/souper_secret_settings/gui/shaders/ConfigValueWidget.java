package com.nettakrim.souper_secret_settings.gui.shaders;

import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.SuggestionEditBoxWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ConfigValueWidget extends AbstractWidget {
    public final String name;

    protected boolean visible;

    protected final List<SuggestionEditBoxWidget> children;

    protected Consumer<ConfigValueWidget> onChangeCallback;

    public List<Object> objects;

    public ConfigValueWidget(int x, int width, int height, String name, @NotNull List<Object> objects, @NotNull List<Object> defaultObjects) {
        super(x, 0, width, height, Component.literal(name));

        this.name = name;
        this.objects = new ArrayList<>(objects);

        children = new ArrayList<>();
        if (!objects.isEmpty()) {
            int childStart = width / 3;
            setWidth(childStart);
            int childWidth = (width - childStart) / objects.size();
            for (int i = 0; i < objects.size(); i++) {
                SuggestionEditBoxWidget textFieldWidget = new SuggestionEditBoxWidget(x + childStart + (childWidth * i), childWidth, height, Component.literal(String.valueOf(i)), true);
                int finalI = i;
                Object object = objects.get(i);
                textFieldWidget.setValue(String.valueOf(object));
                textFieldWidget.setResponder((s) -> valueChanged(s, finalI));
                textFieldWidget.setListeners(() -> Collections.singletonList(String.valueOf(defaultObjects.get(finalI))), null, false);
                children.add(textFieldWidget);

                if (object instanceof String) {
                    textFieldWidget.disableDrag = true;
                }
            }
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (visible) {
            renderScrollingStringOverContents(guiGraphics.textRenderer(), getMessage(), 2);
            for (SuggestionEditBoxWidget textFieldWidget : children) {
                textFieldWidget.renderWidget(guiGraphics, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        for (SuggestionEditBoxWidget textFieldWidget : children) {
            textFieldWidget.setVisible(visible);
        }
    }

    public void addToScreen(ListScreen<?> listScreen) {
        listScreen.addSelectable(this);
        for (SuggestionEditBoxWidget textFieldWidget : children) {
            listScreen.addSelectable(textFieldWidget);
        }
    }

    public void removeFromScreen(ListScreen<?> listScreen) {
        listScreen.removeSelectable(this);
        for (SuggestionEditBoxWidget textFieldWidget : children) {
            listScreen.removeSelectable(textFieldWidget);
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        for (SuggestionEditBoxWidget textFieldWidget : children) {
            textFieldWidget.setY(y);
        }
    }

    public void valueChanged(String s, int i) {
        Object objectAtIndex = objects.get(i);
        Object object;
        if (objectAtIndex == null || objectAtIndex instanceof Number) {
            try {
                object = Float.parseFloat(s);
            } catch (Exception ignored) {
                if (s.equals("null")) {
                    object = null;
                } else {
                    return;
                }
            }
        } else {
            object = s;
        }

        objects.set(i, object);
        if (onChangeCallback != null) {
            onChangeCallback.accept(this);
        }
    }

    public void setChangedListener(Consumer<ConfigValueWidget> callback) {
        onChangeCallback = callback;
    }
}
