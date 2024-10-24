package com.nettakrim.souper_secret_settings.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor("random")
    Random getRandom();
}
