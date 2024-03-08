package com.nettakrim.souper_secret_settings.mixin;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Unique
	private boolean clearOnCameraChange = false;

    @Inject(at = @At("TAIL"), method = "onCameraEntitySet")
	public void onCameraEntitySet(@Nullable Entity entity, CallbackInfo ci) {
		if (clearOnCameraChange) {
			SouperSecretSettingsClient.clearShaders();
			clearOnCameraChange = false;
		}
		if (entity == null) {
			clearOnCameraChange = SouperSecretSettingsClient.tryLoadEntityShader("entity.minecraft.player");
		} else {
			clearOnCameraChange = SouperSecretSettingsClient.tryLoadEntityShader(entity.getType().toString());
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;beginWrite(Z)V", ordinal = 0), method = "render")
	public void render(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		if (SouperSecretSettingsClient.isSoupToggledOff) return;

		SouperSecretSettingsClient.layer.render(tickDelta);
	}

	@Inject(at = @At("HEAD"), method = "onResized")
	public void onResized(int width, int height, CallbackInfo ci) {
		SouperSecretSettingsClient.layer.resize(width, height);
		if (SouperSecretSettingsClient.depthFrameBuffer == null) {
			SouperSecretSettingsClient.depthFrameBuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
		} else {
			SouperSecretSettingsClient.depthFrameBuffer.resize(width, height, false);
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V"), method = "renderWorld")
	public void disableVanillaHand(GameRenderer instance, MatrixStack matrices, Camera camera, float tickDelta) {
		SouperSecretSettingsClient.renderHandMatrixStack = matrices;
		SouperSecretSettingsClient.getGameRendererAccessor().invokeRenderHand(matrices, camera, tickDelta);
	}
}
