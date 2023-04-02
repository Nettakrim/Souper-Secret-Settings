package com.nettakrim.souper_secret_settings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor
    Random getRandom();

    @Invoker("loadPostProcessor")
    public void invokeLoadPostProcessor(Identifier id);
}
