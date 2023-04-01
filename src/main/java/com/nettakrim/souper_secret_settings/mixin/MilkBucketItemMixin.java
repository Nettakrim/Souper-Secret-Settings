package com.nettakrim.souper_secret_settings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.world.World;

@Mixin(MilkBucketItem.class)
public class MilkBucketItemMixin {
    @Inject(at = @At("HEAD"), method = "finishUsing")
	private void finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
		if (world.isClient) SouperSecretSettingsClient.setShader("none");
	}
}
