package com.nettakrim.souper_secret_settings.screen;

import com.mclegoman.luminance.mixin.client.shaders.PostEffectProcessorAccessor;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class ShaderWidget extends CollapseWidget {
    public ShaderData shaderData;
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("widget/button"), Identifier.ofVanilla("widget/button_disabled"), Identifier.ofVanilla("widget/button_highlighted"));

    public ShaderWidget(ShaderData shaderData, CollapseScreen collapseScreen, int x, int width) {
        super(x, width, Text.literal(shaderData.shader.getShaderId().toString()), collapseScreen);

        this.shaderData = shaderData;

        List<PostEffectPass> passes = ((PostEffectProcessorAccessor)shaderData.shader.getPostProcessor()).getPasses();
        int i = 0;
        for (PostEffectPass postEffectPass : passes) {
            PassWidget passWidget = new PassWidget(this, postEffectPass, i, x, width, collapseScreen);
            children.add(passWidget);
            collapseScreen.addSelectable(passWidget);
            i++;
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURES.get(this.active, this.isSelected()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ColorHelper.getWhite(this.alpha));

        drawScrollableText(context, SouperSecretSettingsClient.client.textRenderer, this.getMessage(), this.getX()+2, this.getY(), this.getX()+this.getWidth()-2, this.getY()+20, (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
