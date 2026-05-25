package com.nettakrim.souper_secret_settings.shaders.custom;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import dev.dannytaylor.luminance.client.data.ClientData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

public class Wire {
    private static final Identifier wireTexture = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "textures/gui/wire.png");

    public OutputPort source;
    public InputPort destination;

    public void render(@NotNull GuiGraphics guiGraphics) {
        int offsetX = destination.positionCache.x - source.positionCache.x;
        int offsetY = destination.positionCache.y - source.positionCache.y;

        Matrix3x2fStack matrixStack = guiGraphics.pose();
        matrixStack.pushMatrix();

        // if a wire only barely touches a node, it causes issues with text rendering (???)
        // rendering with a singe texture reduces this, but doesnt eliminate it entirely

        matrixStack.translate(source.positionCache.x + 0.5f, source.positionCache.y + 0.5f);
        matrixStack.rotate((float) Math.atan2(-offsetX, offsetY));
        matrixStack.translate(-0.5f, -0.5f);
        matrixStack.scale(1f, Mth.sqrt(offsetX * offsetX + offsetY * offsetY));

        AbstractTexture abstractTexture = ClientData.minecraft.getTextureManager().getTexture(wireTexture);
        guiGraphics.guiRenderState.submitGuiElement(new ColoredRectangleRenderState(
                RenderPipelines.GUI_TEXTURED,
                TextureSetup.singleTexture(abstractTexture.getTextureView(),abstractTexture.getSampler()),
                new Matrix3x2f(guiGraphics.pose()),
                -1, 0, 2, 1,
                source.portType.color, destination.portType.color,
                guiGraphics.scissorStack.peek()
        ));

        matrixStack.popMatrix();
    }

    public boolean cut(Vector2i a, Vector2i b) {
        int o1 = o(a, b, source.positionCache);
        int o2 = o(a, b, destination.positionCache);
        // signs dont match (intersection), or at most one is 0 (exact touch, with no false positive if a==b)
        if (o1 * o2 <= 0 && o1 != o2) {
            o1 = o(source.positionCache, destination.positionCache, a);
            o2 = o(source.positionCache, destination.positionCache, b);
            return o1 * o2 <= 0 && o1 != o2;
        }
        return false;
    }

    private int o(Vector2i a, Vector2i b, Vector2i c) {
        return x(b.x-a.x, b.y-a.y,c.x-a.x, c.y-a.y);
    }

    private int x(int ax, int ay, int bx, int by) {
        return Integer.compare(ax * by - ay * bx, 0);
    }

    public record ColoredRectangleRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose, int x0, int y0, int x1, int y1, int col1, int col2, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState {
        public ColoredRectangleRenderState(RenderPipeline renderPipeline, TextureSetup textureSetup, Matrix3x2fc matrix3x2fc, int i, int j, int k, int l, int m, int n, @Nullable ScreenRectangle screenRectangle) {
            this(renderPipeline, textureSetup, matrix3x2fc, i, j, k, l, m, n, screenRectangle, getBounds(i, j, k, l, matrix3x2fc, screenRectangle));
        }

        public void buildVertices(VertexConsumer vertexConsumer) {
            vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.x0(), (float)this.y0()).setUv(0,0).setColor(this.col1());
            vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.x0(), (float)this.y1()).setUv(0,1).setColor(this.col2());
            vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.x1(), (float)this.y1()).setUv(1,1).setColor(this.col2());
            vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.x1(), (float)this.y0()).setUv(1,0).setColor(this.col1());
        }

        private static @Nullable ScreenRectangle getBounds(int i, int j, int k, int l, Matrix3x2fc matrix3x2fc, @Nullable ScreenRectangle screenRectangle) {
            ScreenRectangle screenRectangle2 = (new ScreenRectangle(i, j, k - i, l - j)).transformMaxBounds(matrix3x2fc);
            return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
        }
    }
}
