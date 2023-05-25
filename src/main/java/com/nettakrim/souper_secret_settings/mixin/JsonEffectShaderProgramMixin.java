package com.nettakrim.souper_secret_settings.mixin;

import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(JsonEffectShaderProgram.class)
public class JsonEffectShaderProgramMixin {
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
    private Identifier fixJsonIdentifier(String id) {
        return fixIdentifier(id);
    }

    @Redirect(method = "loadEffect", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
    private static Identifier fixShaderIdentifier(String id) {
        return fixIdentifier(id);
    }

    private static Identifier fixIdentifier(String id) {
        if (!id.contains(":")) return new Identifier(id);
        String s = id.replace("shaders/program/","");
        String[] halves = s.split("\\.");
        String[] identifier = halves[0].split(":");
        return new Identifier(identifier[0], "shaders/program/" + identifier[1] + "." + halves[1]);
    }
}
