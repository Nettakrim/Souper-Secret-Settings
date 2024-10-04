package com.nettakrim.souper_secret_settings.screen;

import com.mclegoman.luminance.client.shaders.ShaderProgramInterface;
import com.mclegoman.luminance.mixin.client.shaders.PostEffectPassAccessor;
import com.mclegoman.luminance.mixin.client.shaders.PostEffectProcessorAccessor;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class ShaderWidget extends ClickableWidget {
    public ShaderData shaderData;
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("widget/button"), Identifier.ofVanilla("widget/button_disabled"), Identifier.ofVanilla("widget/button_highlighted"));

    private boolean expanded;

    public List<String> uniformsToIgnore = List.of("ProjMat", "InSize", "OutSize");

    public ShaderWidget(ShaderData shaderData, ScreenWrapper screenWrapper, int x, int width) {
        super(x, 0, width, 20, Text.literal(shaderData.shader.getShaderId().toString()));

        this.shaderData = shaderData;

        screenWrapper.addChild(this);

        SouperSecretSettingsClient.LOGGER.info("SHADER: "+shaderData.shader.getShaderId().toString());
        createPasses();
    }

    protected void createPasses() {
        List<PostEffectPass> passes = ((PostEffectProcessorAccessor)shaderData.shader.getPostProcessor()).getPasses();
        for (PostEffectPass postEffectPass : passes) {
            createPass(postEffectPass);
        }
    }

    protected void createPass(PostEffectPass postEffectPass) {
        PostEffectPassAccessor accessor = (PostEffectPassAccessor)postEffectPass;
        SouperSecretSettingsClient.LOGGER.info("| PASS: {}", accessor.getID());
        ShaderProgram program = accessor.getProgram();
        for (String name : ((ShaderProgramInterface)program).luminance$getUniformNames()) {
            SouperSecretSettingsClient.LOGGER.info("| | UNIFORM: {}", name);
            GlUniform uniform = program.getUniform(name);
            if (uniform != null && !uniformsToIgnore.contains(name)) {
                if (uniform.getDataType() <= 3) {
                    int[] arr = new int[uniform.getCount()];
                    uniform.getIntData().position(0);
                    uniform.getIntData().get(arr);
                    for (int value : arr) {
                        SouperSecretSettingsClient.LOGGER.info("| | | {}i", value);
                    }
                } else {
                    float[] arr = new float[uniform.getCount()];
                    uniform.getFloatData().position(0);
                    uniform.getFloatData().get(arr);
                    for (float value : arr) {
                        SouperSecretSettingsClient.LOGGER.info("| | | {}f", value);
                    }
                }
            }
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURES.get(this.active, this.isSelected()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ColorHelper.getWhite(this.alpha));

        drawScrollableText(context, SouperSecretSettingsClient.client.textRenderer, this.getMessage(), this.getX()+2, this.getY(), this.getX()+this.getWidth()-2, this.getY()+20, (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        expanded = !expanded;
        height = expanded ? 100 : 20;
    }

    public int getSize() {
        return height;
    }
}
