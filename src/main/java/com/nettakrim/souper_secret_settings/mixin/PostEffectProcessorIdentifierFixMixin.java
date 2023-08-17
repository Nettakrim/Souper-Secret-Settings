package com.nettakrim.souper_secret_settings.mixin;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PostEffectProcessor.class)
public class PostEffectProcessorIdentifierFixMixin {
    @Redirect(method = "parsePass", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
    private static Identifier fixShaderIdentifier(String id) {
        return fixIdentifier(id);
    }

    @Unique
    private static Identifier fixIdentifier(String id) {
        if (!id.contains(":")) return new Identifier(id);
        String[] halves = id.substring(16).split(":");
        return new Identifier(halves[0], "textures/effect/" + halves[1]);
    }
}
