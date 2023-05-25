package com.nettakrim.souper_secret_settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class ShaderResourceLoader {
    public void reload(ResourceManager manager) {
        SouperSecretSettingsClient.clearShaders();
        SouperSecretSettingsClient.clearResources();

        Identifier identifier = new Identifier(SouperSecretSettingsClient.MODID, "shaders.json");

        try {
            parseAll(manager, identifier);
        } catch (IOException ioException) {
            SouperSecretSettingsClient.LOGGER.warn("Failed to load shader List: {}", (Object)ioException);
        }
    }

    public void parseAll(ResourceManager manager, Identifier id) throws IOException {
        for (Resource resource : manager.getAllResources(id)) {
            parseResource(resource);
        }
    }

    public void parseResource(Resource resource) throws IOException {
        BufferedReader reader = resource.getReader();
        JsonArray jsonArray;
        JsonObject jsonObject = JsonHelper.deserialize(reader);
        if (!JsonHelper.hasArray(jsonObject, "values")) return;

        jsonArray = jsonObject.getAsJsonArray("values");
        for (JsonElement jsonElement : jsonArray) {
            parseNamespaceList(jsonElement);
        }

        JsonObject entityLinks = JsonHelper.getObject(jsonObject, "entity_links", null);
        if (entityLinks != null) {
            parseEntityLinks(entityLinks);
        }
    }

    public void parseNamespaceList(JsonElement jsonNamespaceList) {
        JsonObject jsonObject = JsonHelper.asObject(jsonNamespaceList, "namespacelist");
        boolean replace = JsonHelper.getBoolean(jsonObject, "replace", false);
        String namespace = JsonHelper.getString(jsonObject, "namespace", "minecraft");
        JsonArray shaders = JsonHelper.getArray(jsonObject, "shaders", new JsonArray());

        if (replace) {
            SouperSecretSettingsClient.shaderListClearNamespace(namespace);
        }

        for (JsonElement jsonShader : shaders) {
            SouperSecretSettingsClient.shaderListAdd(namespace, jsonShader.getAsString());
        }
    }

    public void parseEntityLinks(JsonObject entityLinks) {
        Set<Map.Entry<String, JsonElement>> entitySet = entityLinks.entrySet();
        for (Map.Entry<String, JsonElement> entry: entitySet) {
            SouperSecretSettingsClient.entityLinksAdd(entry.getKey(), JsonHelper.asString(entry.getValue(), "shader"));
        }
    }
}
