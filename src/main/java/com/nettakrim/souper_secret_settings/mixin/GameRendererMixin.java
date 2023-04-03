package com.nettakrim.souper_secret_settings.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.StackData;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(at = @At("HEAD"), method = "onCameraEntitySet", cancellable = true)
	public void onCameraEntitySet(@Nullable Entity entity, CallbackInfo ci) {
		if (SouperSecretSettingsClient.isSouped) {
			if (entity != null && entity != SouperSecretSettingsClient.client.player) {
				SouperSecretSettingsClient.isSouped = false;
				SouperSecretSettingsClient.canRestore = true;
			} else {
				ci.cancel();
			}
		}
		if (SouperSecretSettingsClient.canRestore && entity == SouperSecretSettingsClient.client.player) {
			SouperSecretSettingsClient.gameRendererAccessor.invokeLoadPostProcessor(SouperSecretSettingsClient.currentShader.shader);
			SouperSecretSettingsClient.isSouped = true;
			SouperSecretSettingsClient.canRestore = false;
			ci.cancel();
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 0), method = "render")
	public void render(PostEffectProcessor processor, float tickDelta) {
		processor.render(tickDelta);
		for (StackData stackData : SouperSecretSettingsClient.postProcessorStack) {
			stackData.processor.render(tickDelta);
		}
	}

	@Inject(at = @At("HEAD"), method = "onResized")
	public void onResized(int width, int height, CallbackInfo ci) {
		for (StackData stackData : SouperSecretSettingsClient.postProcessorStack) {
			stackData.processor.setupDimensions(width, height);
		}
	}

	@Inject(at = @At("HEAD"), method = "disablePostProcessor")
	public void disablePostProcessor(CallbackInfo ci) {
		SouperSecretSettingsClient.clearShaders();
	}
}
