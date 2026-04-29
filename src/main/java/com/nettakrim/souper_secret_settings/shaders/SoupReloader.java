package com.nettakrim.souper_secret_settings.shaders;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.dannytaylor.luminance.client.util.JsonResourceReloader;
import com.mojang.serialization.JsonOps;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.data.LayerCodecs;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

public class SoupReloader extends JsonResourceReloader {
    public static final String resourceLocation = "souper_secret_settings";
    public SoupReloader() {
        super(new Gson(), resourceLocation);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        SouperSecretSettingsClient.soupData.resourceLayers.clear();
        SouperSecretSettingsClient.soupData.resourceGroups.clear();

        prepared.forEach((identifier, jsonElement) -> {
            try {
                if (identifier.getPath().startsWith("layers/")) {
                    Optional<LayerCodecs> layerCodecs = LayerCodecs.CODEC.parse(JsonOps.INSTANCE, jsonElement).result();
                    layerCodecs.ifPresent(codecs -> SouperSecretSettingsClient.soupData.resourceLayers.put(Identifier.fromNamespaceAndPath(identifier.getNamespace(), identifier.getPath().substring(7)), codecs));
                }
                else if (identifier.getPath().startsWith("groups/")) {
                    String full = identifier.getPath().substring(7);
                    int i = full.indexOf("/");

                    Map<String, Group> registryMap = SouperSecretSettingsClient.soupData.resourceGroups.computeIfAbsent(full.substring(0, i), (ignored) -> new HashMap<>());
                    Optional<Group> group = Group.CODEC.parse(JsonOps.INSTANCE, jsonElement).result();

                    String key = identifier.getNamespace() + "/" + full.substring(i + 1);
                    if (group.isPresent()) {
                        if (!registryMap.containsKey(key) || registryMap.get(key).file == null) {
                            registryMap.put(key, group.get());
                        }
                    }
                }
            }
            catch (Exception ignored) {}
        });
    }
}
