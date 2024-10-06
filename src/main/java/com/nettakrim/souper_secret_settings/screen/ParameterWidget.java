package com.nettakrim.souper_secret_settings.screen;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public abstract class ParameterWidget extends CollapseWidget {
    public int count;

    public String[] values;

    public ParameterWidget(int count, Text name, int x, int width, CollapseScreen collapseScreen) {
        super(x, width, name, collapseScreen);
        this.count = count;
        height = 20+(count*20);
    }

    protected void initValues(CollapseScreen collapseScreen) {
        values = getValues();
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            ParameterTextWidget widget = new ParameterTextWidget(SouperSecretSettingsClient.client.textRenderer, getX(), width, 20, Text.literal("value"));
            widget.setText(value);
            int finalI = i;
            widget.setChangedListener((s) -> onValueChanged(finalI, s));
            collapseScreen.addSelectable(widget);
            children.add(widget);
        }
    }

    abstract String[] getValues();

    protected void onValueChanged(int i, String s) {
        values[i] = s;
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
