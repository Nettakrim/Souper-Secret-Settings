package com.nettakrim.souper_secret_settings.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.StewItem;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

@Mixin(StewItem.class)
public class StewItemMixin {
	@Inject(at = @At("HEAD"), method = "finishUsing")
	private void finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
		if (world.isClient && stack.isOf(Items.BEETROOT_SOUP)) SouperSecretSettingsClient.setShader("random");
	}
}
