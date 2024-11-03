package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.mixin.client.shaders.PostEffectProcessorAccessor;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.List;

public class ShaderWidget extends ListWidget {
    public ShaderData shaderData;

    public ShaderWidget(ShaderData shaderData, ListScreen<?> listScreen, int x, int width) {
        super(x, width, Text.literal(shaderData.shader.getShaderId().toString()), listScreen);

        this.shaderData = shaderData;

        List<PostEffectPass> passes = ((PostEffectProcessorAccessor)shaderData.shader.getPostProcessor()).getPasses();
        int i = 0;
        for (PostEffectPass postEffectPass : passes) {
            PassWidget passWidget = new PassWidget(this, postEffectPass, i, x, width, listScreen);
            children.add(passWidget);
            listScreen.addSelectable(passWidget);
            i++;
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
