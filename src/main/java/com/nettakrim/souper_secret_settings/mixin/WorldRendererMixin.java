package com.nettakrim.souper_secret_settings.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.minecraft.client.render.WorldRenderer;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 1), method = "render")
    public void saveDepthFabulous(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        SouperSecretSettingsClient.depthFrameBuffer.copyDepthFrom(SouperSecretSettingsClient.client.getFramebuffer());
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V", ordinal = 1), method = "render")
    public void saveDepthFastFancy(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        SouperSecretSettingsClient.depthFrameBuffer.copyDepthFrom(SouperSecretSettingsClient.client.getFramebuffer());
        SouperSecretSettingsClient.client.getFramebuffer().beginWrite(false);
    }
}
