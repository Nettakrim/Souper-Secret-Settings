package com.nettakrim.souper_secret_settings.gui;

import dev.dannytaylor.luminance.common.util.Couple;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.NotNull;

public class AdditionButton extends HoverButtonWidget {
    public String addition;
    protected Consumer<AdditionButton> onRemove;
    protected Consumer<AdditionButton> onEdit;

    protected int dragState;

    public AdditionButton(String addition, Couple<net.minecraft.network.chat.Component, net.minecraft.network.chat.Component> message, int x, int width, int height, Consumer<String> onPress) {
        super(x, 0, width, height, message.getFirst(), message.getSecond(), (widget) -> onPress.accept(addition));
        this.addition = addition;
        this.onRemove = null;
        this.onEdit = null;
    }

    public void addRemoveListener(Consumer<AdditionButton> onRemove) {
        this.onRemove = onRemove;
    }

    public void addEditListener(Consumer<AdditionButton> onEdit) {
        this.onEdit = onEdit;
    }

    @Override
    protected void renderContents(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderContents(context, mouseX, mouseY, delta);

        if (onRemove != null) {
            context.blit(RenderPipelines.GUI_TEXTURED, ListWidget.ICON_TEXTURE, getX(), getY(), 0, 0, 10, 20, 40, 20, dragState < 0 ? ListWidget.texColWhite : ListWidget.texColBlack);
        }

        if (onEdit != null) {
            context.blit(RenderPipelines.GUI_TEXTURED, ListWidget.ICON_TEXTURE, getX()+getWidth()-12, getY(), 20, 0, 10, 20, 40, 20, dragState > 0 ? ListWidget.texColWhite : ListWidget.texColBlack);
        }
    }

    @Override
    protected void renderText(@NotNull GuiGraphics context) {
        int i = this.getX() + (onRemove == null ? 4 : 12);
        int j = this.getX() + this.getWidth() - 2;
        context.textRenderer().acceptScrollingWithDefaultCenter(this.getMessage(), i, j, getY(), this.getY() + this.getHeight());
    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        dragState = 0;
        if (click.x() < getX()+getWidth()-14 || onEdit == null) {
            if (click.x() > getX()+10 || onRemove == null) {
                super.onClick(click, doubled);
            } else {
                dragState = -1;
            }
        } else {
            dragState = 1;
        }
    }

    @Override
    public void onRelease(@NotNull MouseButtonEvent click) {
        if (dragState == 1 && onEdit != null) {
            onEdit.accept(this);
        }
        if (dragState == -1 && click.x() < getX()+10 && click.y() > getY() && click.y() < getY()+getHeight() && onRemove != null) {
            onRemove.accept(this);
        }
        dragState = 0;
    }

    @Override
    protected boolean passesRangeCheck(int mouseX, int mouseY) {
        return onRemove == null || mouseX > getX() + 10;
    }
}
