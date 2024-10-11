package com.nettakrim.souper_secret_settings.screen;

import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.interfaces.ShaderProgramInterface;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class PassWidget extends CollapseWidget {
    public static final List<String> uniformsToIgnore = List.of("ProjMat", "InSize", "OutSize");

    public ShaderWidget shader;
    public PostEffectPass postEffectPass;
    public int passIndex;

    public PassWidget(ShaderWidget shader, PostEffectPass postEffectPass, int passIndex, int x, int width, CollapseScreen collapseScreen) {
        super(x, width, Text.literal(((PostEffectPassInterface)postEffectPass).luminance$getID()), collapseScreen);

        this.shader = shader;
        this.postEffectPass = postEffectPass;
        this.passIndex = passIndex;

        ShaderProgram program = ((PostEffectPassInterface)postEffectPass).luminance$getProgram();
        for (String name : ((ShaderProgramInterface)program).luminance$getUniformNames()) {
            GlUniform uniform = program.getUniform(name);
            if (uniform != null && !uniformsToIgnore.contains(name)) {
                UniformWidget uniformWidget = new UniformWidget(this, uniform, Text.literal(uniform.getName()), x, width, collapseScreen);
                collapseScreen.addSelectable(uniformWidget);
                children.add(uniformWidget);
            }
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        drawScrollableText(context, SouperSecretSettingsClient.client.textRenderer, this.getMessage(), this.getX()+2, this.getY(), this.getX()+this.getWidth()-2, this.getY()+20, (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);

        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
