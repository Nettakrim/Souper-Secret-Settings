package com.nettakrim.souper_secret_settings.shaders;

import dev.dannytaylor.luminance.client.shaders.SpectatorHandler;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class SoupSpectateHandler implements SpectatorHandler {
    public @Nullable ShaderLayer shaderLayer;

    @Override
    public int getPriority(Entity entity) {
        return SouperSecretSettingsClient.soupData.resourceLayers.containsKey(getID(entity)) ? 100 : -1;
    }

    @Override
    public void apply(Entity entity) {
        shaderLayer = new ShaderLayer(getID(entity).toString());
    }

    private Identifier getID(Entity entity) {
        String s = entity.getType().toString().substring(7);
        int i = s.indexOf('.');
        return Identifier.fromNamespaceAndPath(s.substring(0, i), s.substring(i+1));
    }

    @Override
    public void clear() {
        shaderLayer = null;
    }
}
