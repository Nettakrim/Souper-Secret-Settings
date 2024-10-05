package com.nettakrim.souper_secret_settings.screen;

import com.mclegoman.luminance.client.shaders.ShaderProgramInterface;
import com.mclegoman.luminance.mixin.client.shaders.PostEffectPassAccessor;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class PassWidget extends ClickableWidget {
    public static final List<String> uniformsToIgnore = List.of("ProjMat", "InSize", "OutSize");

    private final List<UniformWidget> uniformWidgets = new ArrayList<>();

    public PassWidget(PostEffectPass postEffectPass, int x, int width, ScreenWrapper screenWrapper) {
        super(x, 0, width, 20, Text.literal(((PostEffectPassAccessor)postEffectPass).getID()));

        PostEffectPassAccessor accessor = (PostEffectPassAccessor)postEffectPass;
        SouperSecretSettingsClient.LOGGER.info("| PASS: {}", accessor.getID());
        ShaderProgram program = accessor.getProgram();
        for (String name : ((ShaderProgramInterface)program).luminance$getUniformNames()) {
            SouperSecretSettingsClient.LOGGER.info("| | UNIFORM: {}", name);
            GlUniform uniform = program.getUniform(name);
            if (uniform != null && !uniformsToIgnore.contains(name)) {
                UniformWidget uniformWidget = new UniformWidget(uniform, Text.literal(uniform.getName()), x, width, screenWrapper);
                screenWrapper.addChild(uniformWidget);
                uniformWidgets.add(uniformWidget);
            }
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        drawScrollableText(context, SouperSecretSettingsClient.client.textRenderer, this.getMessage(), this.getX()+2, this.getY(), this.getX()+this.getWidth()-2, this.getY()+20, (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void setY(int y) {
        super.setY(y);
        int height = 20;
        for (UniformWidget uniformWidget : uniformWidgets) {
            uniformWidget.setY(height+y);
            height+=uniformWidget.getHeight();
        }
        setHeight(height);
    }
}
