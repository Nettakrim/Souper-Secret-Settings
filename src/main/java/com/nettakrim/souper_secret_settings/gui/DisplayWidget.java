package com.nettakrim.souper_secret_settings.gui;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;

public abstract class DisplayWidget extends CollapseWidget {
    protected static int displayWidth = 10;

    public int count;
    public DisplayWidget(int count, Component name, int x, int width, ListScreen<?> listScreen) {
        super(x, width, name, listScreen);
        this.count = count;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.textRenderer().acceptScrollingWithDefaultCenter(this.getMessage(), this.getX()+2, this.getX()+this.getWidth()-displayWidth-2, this.getY(), this.getY()+20);

        super.renderWidget(guiGraphics, mouseX, mouseY, delta);

        List<Float> currentDisplay = getDisplayFloats();
        drawIndicator(guiGraphics, currentDisplay);

        if (isHovered && mouseX > this.getX()+this.getWidth()-displayWidth-2) {
            SouperSecretSettingsClient.soupGui.setHoverText(getHoverText(currentDisplay));
        }
    }

    private static Component getHoverText(List<Float> currentDisplay) {
        StringBuilder stringBuilder = new StringBuilder("[ ");
        for (Float f : currentDisplay) {
            String s = f.toString();
            int e = s.indexOf('E');
            String exponent = null;
            if (e > 0) {
                exponent = s.substring(e);
                s = s.substring(0, e);
            }

            int point = s.indexOf('.');
            if (point > 0 && s.length() > point+4) {
                s = s.substring(0, point+4);
            }
            stringBuilder.append(s);
            if (exponent != null) {
                stringBuilder.append(exponent);
            }
            stringBuilder.append(" ");
        }
        stringBuilder.append("]");

        return Component.nullToEmpty(stringBuilder.toString());
    }

    protected void drawIndicator(GuiGraphics guiGraphics, List<Float> currentDisplay) {
        int x = getX()+getWidth();
        int y = getY();

        guiGraphics.fill(x, y, x-displayWidth, y+20, getColor(currentDisplay));
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
            r = values.getFirst()*normalise;
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

        return ARGB.colorFromFloat(1f, r, g, b);
    }

    protected abstract List<Float> getDisplayFloats();
}
