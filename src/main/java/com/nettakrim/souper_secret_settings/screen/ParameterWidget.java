package com.nettakrim.souper_secret_settings.screen;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

public abstract class ParameterWidget extends ClickableWidget {
    public int count;

    public String[] values;

    public ArrayList<ParameterTextWidget> textWidgets = new ArrayList<>();

    public ParameterWidget(int count, Text name, int x, int width, ScreenWrapper screenWrapper) {
        super(x, 0, width, 20, name);
        this.count = count;
        height = 20+(count*20);
    }

    protected void initValues(ScreenWrapper screenWrapper) {
        values = getValues();
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            ParameterTextWidget widget = new ParameterTextWidget(SouperSecretSettingsClient.client.textRenderer, getX(), width, 20, Text.literal("value"));
            widget.setText(value);
            int finalI = i;
            widget.setChangedListener((s) -> onValueChanged(finalI, s));
            screenWrapper.addChild(widget);
            textWidgets.add(widget);
        }
    }

    abstract String[] getValues();

    protected void onValueChanged(int i, String s) {
        values[i] = s;
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
        for (int i = 0; i < textWidgets.size(); i++) {
            textWidgets.get(i).setY(y + 20*(i+1));
        }
    }
}
