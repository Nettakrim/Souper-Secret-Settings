package com.nettakrim.souper_secret_settings.mixin;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Inject(at = @At("TAIL"), method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V")
    public void onChat(Text messageText, MessageSignatureData messageSignatureData, MessageIndicator messageIndicator, CallbackInfo ci) {
        String message = messageText.getString();
        SouperSecretSettingsClient.LOGGER.info(message);
        int soupIndex = message.indexOf("soup:");
        if (soupIndex == -1) return;

        String data = message.substring(soupIndex+5);
        SouperSecretSettingsClient.LOGGER.info(data);
        int endIndex = data.indexOf('|');
        if (endIndex == -1) return;

        data = data.substring(0, endIndex);
        SouperSecretSettingsClient.LOGGER.info(data);

        int count = (data.length() - data.replace("/", "").length())+1;

        if (count == 1) data = "/"+data;

        SouperSecretSettingsClient.sayClickHere(
            "share.receive",
            "/soup:recipe load "+data,
            true,
            count
        );
    }
}
