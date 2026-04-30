package com.nettakrim.souper_secret_settings.gui.shaders;

import dev.dannytaylor.luminance.client.shaders.UniformBlock;
import dev.dannytaylor.luminance.client.shaders.UniformInstance;
import com.nettakrim.souper_secret_settings.gui.CollapseWidget;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class BlockWidget extends CollapseWidget {
    public PassWidget pass;

    public UniformBlock block;
    public String blockName;

    public BlockWidget(PassWidget pass, UniformBlock block, String blockName, int x, int width, ListScreen<?> listScreen) {
        super(x, width, Component.literal(blockName), listScreen);
        this.pass = pass;
        this.block = block;
        this.blockName = blockName;
        setHeight(12);
        active = false;
    }

    @Override
    protected void createChildren(int x, int width) {
        int i = 0;
        for (UniformInstance uniform : block.uniforms) {
            UniformWidget uniformWidget = new UniformWidget(this, uniform, Component.literal(uniform.name), i++, x, width, listScreen);
            listScreen.addSelectable(uniformWidget);
            children.add(uniformWidget);
        }
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(guiGraphics, mouseX, mouseY, delta);
        renderScrollingStringOverContents(guiGraphics.textRenderer(), getMessage(), 2);
    }

    // force expanded always
    @Override
    protected boolean getStoredExpanded() {
        return true;
    }

    @Override
    protected void setStoredExpanded(boolean to) {

    }
}
