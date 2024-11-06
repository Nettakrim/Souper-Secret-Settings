package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public abstract class DisplayWidget extends CollapseWidget {
    protected static int displayWidth = 10;

    public int count;
    public DisplayWidget(int count, Text name, int x, int width, ListScreen<?> listScreen) {
        super(x, width, name, listScreen);
        this.count = count;
        height = 20+(count*20);
    }

    protected void initValues() {
        String[] values = getChildData();
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            ClickableWidget widget = createChildWidget(value, i);
            listScreen.addSelectable(widget);
            children.add(widget);
        }
    }

    protected abstract ClickableWidget createChildWidget(String data, int i);

    protected abstract String[] getChildData();

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        drawScrollableText(context, SouperSecretSettingsClient.client.textRenderer, this.getMessage(), this.getX()+2, this.getY(), this.getX()+this.getWidth()-displayWidth-2, this.getY()+20, (this.active ? 16777215 : 10526880) | MathHelper.ceil(this.alpha * 255.0F) << 24);

        super.renderWidget(context, mouseX, mouseY, delta);

        drawIndicator(context);
    }

    protected void drawIndicator(DrawContext context) {
        int x = getX()+getWidth();
        int y = getY();

        context.fill(x, y, x-displayWidth, y+20, getColor(getDisplayFloats()));
    }

    protected int getColor(List<Float> values) {
        float over = 0f;
        for (Float f : values) {
            over = Math.max(over, Math.abs(f));
        }
        if (over > 1) {
            for (int i = 0; i < values.size(); i++) {
                values.set(i, values.get(i)/over);
            }
        }

        float r;
        float g;
        float b;
        if (values.size() == 1) {
            r = values.get(0);
            g = -r;
            b = g;
        } else if (values.size() == 2) {
            r = values.get(0);
            g = values.get(1);
            float rInverse = -Math.min(r, 0f);
            float gInverse = -Math.min(g, 0f);
            r += gInverse;
            g += rInverse;
            b = gInverse + rInverse;
        } else if (values.size() >= 3) {
            r = values.get(0);
            g = values.get(1);
            b = values.get(2);
        } else {
            r = 0f;
            g = 0f;
            b = 0f;
        }

        r = Math.clamp(r, 0f, 1f);
        g = Math.clamp(g, 0f, 1f);
        b = Math.clamp(b, 0f, 1f);
        if (over > 1) {
            over = (over-1)/(1.25f*over);
            r = (r*(1-over))+over;
            g = (g*(1-over))+over;
            b = (b*(1-over))+over;
        }

        return ColorHelper.fromFloats(1f, r, g, b);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    protected abstract List<Float> getDisplayFloats();
}
