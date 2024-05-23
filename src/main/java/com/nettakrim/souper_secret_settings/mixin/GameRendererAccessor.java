package com.nettakrim.souper_secret_settings.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor
    Random getRandom();

    @Accessor
    ResourceManager getResourceManager();

    @Invoker("loadPostProcessor")
    void invokeLoadPostProcessor(Identifier id);
}
