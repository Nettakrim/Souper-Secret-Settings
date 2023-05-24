package com.nettakrim.souper_secret_settings.mixin;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
    private void disconnect(CallbackInfo ci) {
        SouperSecretSettingsClient.clearShaders();
    }

    @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("RETURN"))
    private void join(ClientWorld world, CallbackInfo ci) {
        SouperSecretSettingsClient.tryLoadEntityShader("entity.minecraft.player");
    }
}
