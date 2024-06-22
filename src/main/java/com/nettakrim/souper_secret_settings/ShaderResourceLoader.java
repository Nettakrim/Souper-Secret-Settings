package com.nettakrim.souper_secret_settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class ShaderResourceLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    public ShaderResourceLoader() {
        super(new Gson(), "shaders/shaders");
    }

    @Override
    public void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        SouperSecretSettingsClient.clearShaders();
        SouperSecretSettingsClient.clearResources();

        releaseFromType(ShaderStage.Type.FRAGMENT);
        releaseFromType(ShaderStage.Type.VERTEX);

        parseAll(manager);

        //Perspective has its own resourcepack support with slightly different syntax, but its fundamentally about the same as mine, so it's easy to add support
        prepared.forEach((identifier, jsonElement) -> {
            try{
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                parsePerspectiveShader(jsonObject);
            } catch (Exception e) {
                SouperSecretSettingsClient.LOGGER.warn("Failed to load Perspective shader: {}", (Object)e);
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(SouperSecretSettingsClient.MODID, "shaders/shaders");
    }

    private void releaseFromType(ShaderStage.Type type) {
        StringBuilder log = new StringBuilder("Releasing from type \"").append(type.toString()).append("\":");
        List<Map.Entry<String,ShaderStage>> array = type.getLoadedShaders().entrySet().stream().toList();

        for (int i = array.size()-1; i>-1; i--) {
            Map.Entry<String,ShaderStage> entry = array.get(i);
            String name = entry.getKey();
            if (name.startsWith("rendertype_")) continue;
            if (name.startsWith("position_")) continue;
            if (name.equals("position") || name.equals("particle")) continue;
            log.append(" ").append(name);
            entry.getValue().release();
        }
        SouperSecretSettingsClient.LOGGER.info(log.toString());
    }

    public void parseAll(ResourceManager manager) {
        for (String namespace : manager.getAllNamespaces()) {
            Identifier identifier = Identifier.of(namespace, "shaders.json");
            try {
                for (Resource resource : manager.getAllResources(identifier)) {
                    parseResource(resource);
                }
            } catch (IOException ioException) {
                SouperSecretSettingsClient.LOGGER.warn("Failed to load shader List: {}", (Object)ioException);
            }
        }
    }

    public void parseResource(Resource resource) throws IOException {
        BufferedReader reader = resource.getReader();
        JsonObject jsonObject = JsonHelper.deserialize(reader);

        boolean replace = JsonHelper.getBoolean(jsonObject, "replace", false);
        if (replace) SouperSecretSettingsClient.clearResources();

        parseShaderList(jsonObject);
    }

    public void parseShaderList(JsonObject jsonObject) {
        JsonArray jsonArray;
        if (JsonHelper.hasArray(jsonObject, "namespaces")) {
            jsonArray = jsonObject.getAsJsonArray("namespaces");
            for (JsonElement jsonElement : jsonArray) {
                parseNamespaceList(jsonElement);
            }
        }

        if (JsonHelper.hasJsonObject(jsonObject, "entity_links")) {
            JsonObject entityLinks = JsonHelper.getObject(jsonObject, "entity_links");
            parseEntityLinks(entityLinks);
        }
    }

    public void parseNamespaceList(JsonElement jsonNamespaceList) {
        JsonObject jsonObject = JsonHelper.asObject(jsonNamespaceList, "namespacelist");
        boolean replace = JsonHelper.getBoolean(jsonObject, "replace", false);
        String namespace = JsonHelper.getString(jsonObject, "namespace", "minecraft");
        JsonArray shaders = JsonHelper.getArray(jsonObject, "shaders", new JsonArray());
        JsonArray layerEffects = JsonHelper.getArray(jsonObject, "layer_effects", new JsonArray());

        if (replace) {
            SouperSecretSettingsClient.shaderListClearNamespace(namespace);
            SouperSecretSettingsClient.layerEffectListClearNamespace(namespace);
        }

        JsonArray disableSoupJson = JsonHelper.getArray(jsonObject, "disable_soup", new JsonArray());
        List<String> disableSoup = new ArrayList<>(disableSoupJson.size());

        for (JsonElement disable : disableSoupJson) {
            disableSoup.add(disable.getAsString());
        }

        for (JsonElement jsonShader : shaders) {
            String shader = jsonShader.getAsString();
            SouperSecretSettingsClient.shaderListAdd(namespace, shader,  !disableSoup.contains(shader));
        }

        for (JsonElement jsonEffect : layerEffects) {
            SouperSecretSettingsClient.layerEffectListAdd(namespace, jsonEffect.getAsString());
        }
    }

    public void parseEntityLinks(JsonObject entityLinks) {
        Set<Map.Entry<String, JsonElement>> entitySet = entityLinks.entrySet();
        for (Map.Entry<String, JsonElement> entry: entitySet) {
            SouperSecretSettingsClient.entityLinksAdd(entry.getKey(), JsonHelper.asString(entry.getValue(), "shader"));
        }
    }

    //https://github.com/MCLegoMan/Perspective/blob/release-1.20.3-4/src/main/java/com/mclegoman/perspective/client/shaders/ShaderDataLoader.java
    public void parsePerspectiveShader(JsonObject jsonObject) {
        String namespace = JsonHelper.getString(jsonObject, "namespace", "perspective");
        String shader = JsonHelper.getString(jsonObject, "shader");
        boolean enabled = JsonHelper.getBoolean(jsonObject, "enabled", true);
        boolean disableSoup = JsonHelper.getBoolean(jsonObject, "disable_soup", false);
        if (enabled) {
            SouperSecretSettingsClient.shaderListAdd(namespace, shader, !disableSoup);
        } else {
            SouperSecretSettingsClient.shaderListRemove(namespace, shader);
        }
    }
}
