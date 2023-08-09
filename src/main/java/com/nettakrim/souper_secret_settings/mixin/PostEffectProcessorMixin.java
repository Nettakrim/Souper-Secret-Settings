package com.nettakrim.souper_secret_settings.mixin;

import com.google.gson.JsonElement;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PostEffectProcessor.class)
public class PostEffectProcessorMixin {
    @Unique
    private boolean useDepth;

    @Inject(at = @At(value = "INVOKE", target = "Ljava/lang/String;substring(II)Ljava/lang/String;"), method = "parsePass")
    public void detectDepth(TextureManager textureManager, JsonElement jsonPass, CallbackInfo ci) {
        if (SouperSecretSettingsClient.canFixDepth) {
            useDepth = true;
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "render")
    public void fixDepth(float tickDelta, CallbackInfo ci) {
        if (useDepth) {
            SouperSecretSettingsClient.client.getFramebuffer().copyDepthFrom(SouperSecretSettingsClient.depthFrameBuffer);
        }
    }
}
