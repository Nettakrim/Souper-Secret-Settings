package com.nettakrim.souper_secret_settings.gui.shaders;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.OverrideManager;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import com.nettakrim.souper_secret_settings.shaders.Toggleable;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ShaderWidget extends ListWidget {
    public ShaderLayer layer;
    public ShaderData shaderData;

    public ShaderWidget(ShaderLayer layer, ShaderData shaderData, ListScreen<?> listScreen, int x, int width) {
        super(x, width, Component.literal(shaderData.shader.getShaderId().toString()), listScreen);

        this.layer = layer;
        this.shaderData = shaderData;
    }

    @Override
    protected void createChildren(int x, int width) {
        for (Identifier chain : SouperSecretSettingsClient.soupRenderer.getRegistryChains(((ShaderScreen)listScreen).registry)) {
            addChain(chain);
        }
    }

    protected void addChain(Identifier chain) {
        List<PostPass> passes = shaderData.shader.getPostChain().luminance$getPasses(chain);
        if (passes == null) {
            return;
        }

        int i = 0;
        for (PostPass postEffectPass : passes) {
            PassWidget passWidget = new PassWidget(this, postEffectPass, chain, i, getX(), width, listScreen);
            children.add(passWidget);
            listScreen.addSelectable(passWidget);
            i++;
        }
    }

    @Override
    protected boolean getStoredExpanded() {
        return shaderData.expanded;
    }

    @Override
    protected void setStoredExpanded(boolean to) {
        shaderData.expanded = expanded;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(guiGraphics, mouseX, mouseY, delta);
        if (shaderData.active) {
            OverrideManager.currentShaderIndex++;
        }
    }

    @Override
    protected Toggleable getToggleable() {
        return shaderData;
    }
}
