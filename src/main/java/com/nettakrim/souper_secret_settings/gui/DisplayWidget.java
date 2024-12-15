package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.List;

public abstract class DisplayWidget<T> extends CollapseWidget {
    protected static int displayWidth = 10;

    public int count;
    public DisplayWidget(int count, Text name, int x, int width, ListScreen<?> listScreen) {
        super(x, width, name, listScreen);
        this.count = count;
        height = 20+(count*20);
    }

    protected void initValues() {
        List<T> values = getChildData();
        for (int i = 0; i < values.size(); i++) {
            ClickableWidget widget = createChildWidget(values.get(i), i);
            listScreen.addSelectable(widget);
            children.add(widget);
        }
    }

    protected abstract ClickableWidget createChildWidget(T data, int i);

    protected abstract List<T> getChildData();

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int color = ColorHelper.getArgb(255,255,255,255);
        drawScrollableText(context, SouperSecretSettingsClient.client.textRenderer, this.getMessage(), this.getX()+2, this.getY(), this.getX()+this.getWidth()-displayWidth-2, this.getY()+20, color);

        super.renderWidget(context, mouseX, mouseY, delta);

        List<Float> currentDisplay = getDisplayFloats();
        drawIndicator(context, currentDisplay);

        if (hovered && mouseX > this.getX()+this.getWidth()-displayWidth-2) {
            Text text = getHoverText(currentDisplay);
            context.fill(mouseX-2, mouseY-22, mouseX + SouperSecretSettingsClient.client.textRenderer.getWidth(text)+2, mouseY-10, ColorHelper.getArgb(128,0,0,0));
            context.drawText(SouperSecretSettingsClient.client.textRenderer, text, mouseX, mouseY-20, color, true);
        }
    }

    private static Text getHoverText(List<Float> currentDisplay) {
        StringBuilder stringBuilder = new StringBuilder("[ ");
        for (Float f : currentDisplay) {
            String s = f.toString();
            int point = s.indexOf('.');
            if (point > 0 && s.length() > point+4) {
                s = s.substring(0, point+4);
            }
            stringBuilder.append(s);
            stringBuilder.append(" ");
        }
        stringBuilder.append("]");

        return Text.of(stringBuilder.toString());
    }

    protected void drawIndicator(DrawContext context, List<Float> currentDisplay) {
        int x = getX()+getWidth();
        int y = getY();

        context.fill(x, y, x-displayWidth, y+20, getColor(currentDisplay));
    }

    protected int getColor(List<Float> values) {
        float scale = 0f;
        for (Float f : values) {
            scale = Math.max(scale, Math.abs(f));
        }
        float normalise = 1f;
        if (scale > 1) {
            normalise = 1f/scale;
        }

        float r;
        float g;
        float b;
        if (values.size() == 1) {
            r = values.get(0)*normalise;
            g = -r;
            b = g;
        } else if (values.size() == 2) {
            r = values.get(0)*normalise;
            g = values.get(1)*normalise;
            float rInverse = -Math.min(r, 0f);
            float gInverse = -Math.min(g, 0f);
            r += gInverse;
            g += rInverse;
            b = gInverse + rInverse;
        } else if (values.size() >= 3) {
            r = values.get(0)*normalise;
            g = values.get(1)*normalise;
            b = values.get(2)*normalise;
        } else {
            r = 0f;
            g = 0f;
            b = 0f;
        }

        r = Math.clamp(r, 0f, 1f);
        g = Math.clamp(g, 0f, 1f);
        b = Math.clamp(b, 0f, 1f);
        if (scale > 1) {
            scale = (scale-1)/(1.25f*scale);
            r = (r*(1-scale))+scale;
            g = (g*(1-scale))+scale;
            b = (b*(1-scale))+scale;
        }

        return ColorHelper.fromFloats(1f, r, g, b);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    protected abstract List<Float> getDisplayFloats();
}
