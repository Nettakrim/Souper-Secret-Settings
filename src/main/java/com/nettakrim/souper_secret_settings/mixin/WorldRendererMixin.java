package com.nettakrim.souper_secret_settings.mixin;

import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 0), method = "render")
    public void soup$saveDepthOutlines(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        SouperSecretSettingsClient.depthFrameBuffer.copyDepthFrom(SouperSecretSettingsClient.client.getFramebuffer());
        SouperSecretSettingsClient.client.getFramebuffer().beginWrite(false);
    }
    @Inject(at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 1),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V", ordinal = 1)
    }, method = "render")
    public void soup$saveDepth(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        SouperSecretSettingsClient.depthFrameBuffer.copyDepthFrom(SouperSecretSettingsClient.client.getFramebuffer());
        if (SouperSecretSettingsClient.client.options.getGraphicsMode().getValue().getId() <= GraphicsMode.FANCY.getId()) SouperSecretSettingsClient.client.getFramebuffer().beginWrite(false);
    }
}
