package com.nettakrim.souper_secret_settings.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(at = @At("HEAD"), method = "onCameraEntitySet", cancellable = true)
	public void onCameraEntitySet(@Nullable Entity entity, CallbackInfo ci) {
		if (SouperSecretSettingsClient.isSouped) {
			if (entity != null && entity != SouperSecretSettingsClient.client.player) {
				SouperSecretSettingsClient.isSouped = false;
			} else {
				ci.cancel();
			}
		}
	}
}
