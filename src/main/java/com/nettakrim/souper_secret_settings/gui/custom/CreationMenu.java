package com.nettakrim.souper_secret_settings.gui.custom;

import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class CreationMenu extends AbstractWidget {
    private final Stack<List<CreationEntry>> creationMenu = new Stack<>();
    private final Consumer<Node> onCreation;

    private static final int entryHeight = 8;
    private static final int headerHeight = 6;
    private static final int footerHeight = 2;

    private Component depthIndicator;

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
        if (index < 0) {
            if (creationMenu.size() > 1) {
                creationMenu.pop();
                updateHeight();
            }
            return;
        }

        CreationEntry entry = creationMenu.peek().get(Math.min(creationMenu.peek().size()-1, index));
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
        depthIndicator = Component.literal("-".repeat(creationMenu.size()-1)).append(Component.literal("-").setStyle(Style.EMPTY.withColor(0xFFCCCCCC)));
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float tickProgress) {
        SoupGui.drawButton(guiGraphics, true, getX(), getY(), getWidth(), getHeight(), true, false);

        if (creationMenu.isEmpty()) {
            return;
        }

        int y = getY();

        guiGraphics.textRenderer().accept(getX() + 2, getY(), depthIndicator);

        y += headerHeight;
        for (CreationEntry entry : creationMenu.peek()) {
            guiGraphics.textRenderer().accept(getX() + 2, y, entry.getText());
            y += entryHeight;
        }
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        if (super.mouseClicked(mouseButtonEvent, doubleClick)) {
            selectEntry(mouseButtonEvent.button() == 0 ? Mth.floor((mouseButtonEvent.y() - getY() - headerHeight)/entryHeight) : -1);
            return true;
        }

        return false;
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo mouseButtonInfo) {
        return mouseButtonInfo.button() <= 1;
    }

    @Override
    public boolean mouseDragged(@NotNull MouseButtonEvent mouseButtonEvent, double d, double e) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }
}
