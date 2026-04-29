package com.nettakrim.souper_secret_settings.gui.shaders;

import dev.dannytaylor.luminance.client.shaders.UniformBlock;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostPassInterface;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.shaders.ChainData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import com.nettakrim.souper_secret_settings.gui.CollapseWidget;
import org.jetbrains.annotations.NotNull;

public class PassWidget extends CollapseWidget {
    public ShaderWidget shader;
    public PostPassInterface postPass;
    public int passIndex;
    public Identifier chain;

    protected boolean isFirstCustom;

    protected static int firstCustomHeight = 10;

    public PassWidget(ShaderWidget shader, PostPass postEffectPass, Identifier chain, int passIndex, int x, int width, ListScreen<?> listScreen) {
        super(x, width, Component.literal(ChainData.getName((PostPassInterface)postEffectPass)), listScreen);

        this.shader = shader;
        this.postPass = (PostPassInterface)postEffectPass;
        this.chain = chain;
        this.passIndex = passIndex;

        active = false;
        for (UniformBlock block : postPass.luminance$getUniformBlocks().values()) {
            if (!block.uniforms.isEmpty()) {
                active = true;
                break;
            }
        }


        if (chain != null && passIndex == 0) {
            setHeight(getHeight()+firstCustomHeight);
            isFirstCustom = true;
        }
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        int y = getY();
        Style style = Style.EMPTY.withColor((this.active ? 16777215 : 10526880) | Mth.ceil(this.alpha * 255.0F) << 24);
        if (isFirstCustom) {
            context.fill(getX(), y, getX() + getWidth(), y+firstCustomHeight, ARGB.colorFromFloat(0.4f, 0, 0, 0));
            context.textRenderer().acceptScrollingWithDefaultCenter(Component.literal(chain.getPath()).setStyle(style), this.getX()+2, this.getX()+this.getWidth()-2, y, y+firstCustomHeight);
            y += firstCustomHeight;
        }

        context.textRenderer().acceptScrollingWithDefaultCenter(this.getMessage().copy().setStyle(style), this.getX()+2, this.getX()+this.getWidth()-2, y, y+20);

        if (expanded) {
            context.fill(getX(), getY() + getCollapseHeight(), getX() + getWidth(), y+20, ARGB.colorFromFloat(0.2f, 0, 0, 0));
        }

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    protected void createChildren(int x, int width) {
        if (!active) {
            return;
        }

        postPass.luminance$getUniformBlocks().forEach((blockName, block) -> {
            BlockWidget blockWidget = new BlockWidget(this, block, blockName, x, width, listScreen);
            listScreen.addSelectable(blockWidget);
            children.add(blockWidget);
        });

        if (children.isEmpty()) active = false;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput builder) {

    }

    @Override
    public void onClick(@NotNull MouseButtonEvent click, boolean doubled) {
        if (isFirstCustom && click.y() < getY() + firstCustomHeight) {
            return;
        }
        super.onClick(click, doubled);
    }

    @Override
    protected boolean getStoredExpanded() {
        return shader.shaderData.getPassData(chain).passesExpanded.get(passIndex);
    }

    @Override
    protected void setStoredExpanded(boolean to) {
        shader.shaderData.getPassData(chain).passesExpanded.set(passIndex, to);
    }
}
