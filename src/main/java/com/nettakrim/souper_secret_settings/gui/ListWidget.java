package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.ToggleAction;
import com.nettakrim.souper_secret_settings.shaders.Toggleable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public abstract class ListWidget extends CollapseWidget {
    protected static final Identifier ICON_TEXTURE = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "textures/gui/icons.png");

    protected int dragState;

    public static int texColBlack = ARGB.colorFromFloat(0.5f, 0f, 0f, 0f);
    public static int texColWhite = ARGB.colorFromFloat(0.5f, 1f, 1f, 1f);


    public ListWidget(int x, int width, Component message, ListScreen<?> listScreen) {
        super(x, width, message, listScreen);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SoupGui.BUTTON_TEXTURES.get(this.active && getToggleable().isActive(), this.isHovered()), this.getX(), this.getY(), this.getWidth(), getCollapseHeight(), -1);

        renderScrollingStringOverContents(guiGraphics.textRenderer(), getMessage().copy().setStyle(Style.EMPTY.withColor((this.active ? 16777215 : 10526880) | Mth.ceil(this.alpha * 255.0F) << 24)), 2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ICON_TEXTURE, getX(), getY(), 0, 0, 10, 20, 40, 20, dragState < 0 ? texColWhite : texColBlack);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ICON_TEXTURE, getX()+getWidth()-10, getY(), 10, 0, 10, 20, 40, 20, dragState > 0 ? texColWhite : texColBlack);

        int editState = getEditState();
        if (editState > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ICON_TEXTURE, getX()+getWidth()-20, getY(), 10 + 10*editState, 0, 10, 20, 40, 20, texColBlack);
        }

        super.renderWidget(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent click, boolean doubled) {
        dragState = 0;
        if (click.x() < getX()+getWidth()-10) {
            if (click.x() > getX()+10) {
                setExpanded(!expanded);
            } else {
                dragState = -1;
            }
        } else {
            dragState = 1;
        }
    }

    @Override
    public void onRelease(@NotNull MouseButtonEvent click) {
        if (dragState == 1) {
            Toggleable toggleable = getToggleable();
            if (listScreen.useHistory()) {
                new ToggleAction(toggleable).addToHistory();
            }
            toggleable.toggle();
        }
        if (dragState == -1 && click.x() < getX()+10 && click.y() > getY() && click.y() < getY()+getHeight()) {
            listScreen.removeEntry(this);
        }
        dragState = 0;
    }

    @Override
    protected void onDrag(@NotNull MouseButtonEvent click, double deltaX, double deltaY) {
        if (dragState <= 0 || deltaY == 0) return;

        double offset = click.y()-getY();
        if (offset < -getHeight()*0.25 && deltaY < 0) {
            listScreen.swapEntry(this, -1);
            dragState = 2;
        } else if (offset > getHeight()*1.25 && deltaY > 0) {
            listScreen.swapEntry(this, 1);
            dragState = 2;
        }
    }

    protected abstract Toggleable getToggleable();

    protected int getEditState() {
        return 0;
    }
}
