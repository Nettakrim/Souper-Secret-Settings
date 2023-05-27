package com.nettakrim.souper_secret_settings.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.StackData;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
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

		if (SouperSecretSettingsClient.postProcessorStack.size() != 0) {
			for (StackData stackData : SouperSecretSettingsClient.postProcessorStack) {
				stackData.processor().render(tickDelta);
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "onResized")
	public void onResized(int width, int height, CallbackInfo ci) {
		for (StackData stackData : SouperSecretSettingsClient.postProcessorStack) {
			stackData.processor().setupDimensions(width, height);
		}
		if (SouperSecretSettingsClient.depthFrameBuffer == null) {
			SouperSecretSettingsClient.depthFrameBuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
		} else {
			SouperSecretSettingsClient.depthFrameBuffer.resize(width, height, false);
		}
	}
}
