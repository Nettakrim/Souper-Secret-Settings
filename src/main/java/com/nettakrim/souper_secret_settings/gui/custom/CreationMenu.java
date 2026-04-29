package com.nettakrim.souper_secret_settings.gui.custom;

import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class CreationMenu extends AbstractWidget {
    private final Stack<List<CreationEntry>> creationMenu = new Stack<>();
    private final Consumer<Node> onCreation;

    private static final int entryHeight = 8;
    private static final int headerHeight = 2;
    private static final int footerHeight = 2;

    public CreationMenu(int width, Consumer<Node> onCreation) {
        super(0, 0, width, 0, Component.empty());
        this.onCreation = onCreation;
    }

    public void init(CreationCategory root, MouseButtonEvent mouseButtonEvent) {
        creationMenu.clear();
        creationMenu.push(root.getChildren());
        updateHeight();
        setPosition((int)mouseButtonEvent.x(), (int)mouseButtonEvent.y());
    }

    public void setActive(boolean active) {
        this.active = active;
        this.visible = active;
    }

    private void selectEntry(int index) {
        int size = creationMenu.peek().size();
        if (index < 0 || index >= size) {
            if (size > 1) {
                creationMenu.pop();
                updateHeight();
            }
            return;
        }

        CreationEntry entry = creationMenu.peek().get(index);
        if (entry instanceof CreationCategory creationCategory) {
            creationMenu.push(creationCategory.getChildren());
            updateHeight();
        } else if (entry instanceof CreationNode creationNode) {
            Node node = creationNode.factory().get();
            node.position.set(getX(), getY());
            onCreation.accept(node);
        }
    }

    private void updateHeight() {
        setHeight(creationMenu.peek().size() * entryHeight + headerHeight + footerHeight);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float tickProgress) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SoupGui.BUTTON_TEXTURES.get(true, false), getX(), getY(), getWidth(), getHeight(), -1);

        if (creationMenu.isEmpty()) {
            return;
        }

        int y = getY() + headerHeight;
        for (CreationEntry entry : creationMenu.peek()) {
            guiGraphics.textRenderer().accept(getX() + headerHeight, y, entry.getText());
            y += entryHeight;
        }
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        if (mouseButtonEvent.button() == 0 && super.mouseClicked(mouseButtonEvent, doubleClick)) {
            double y = (mouseButtonEvent.y() - getY() - headerHeight)/entryHeight;
            selectEntry((int)y);
            return true;
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
