package com.nettakrim.souper_secret_settings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    //for some reason, shaders/post/blur does not work if the vignette is rendering
    @Inject(at = @At("HEAD"), method = "renderVignetteOverlay", cancellable = true)
	private void stopVignette(Entity entity, CallbackInfo ci) {
		if (SouperSecretSettingsClient.removeVignette()) {
            ci.cancel();
        }
	}
}
