package com.nettakrim.souper_secret_settings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.resource.ResourceManager;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(at = @At("HEAD"), method = "reload")
	public void reload(ResourceManager manager, CallbackInfo ci) {
        SouperSecretSettingsClient.shaderResourceLoader.reload(manager);
	}
}
