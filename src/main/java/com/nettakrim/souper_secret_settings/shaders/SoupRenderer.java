package com.nettakrim.souper_secret_settings.shaders;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.dannytaylor.luminance.client.data.ClientData;
import dev.dannytaylor.luminance.client.events.Events;
import dev.dannytaylor.luminance.client.events.Runnables;
import dev.dannytaylor.luminance.client.shaders.RenderLocations;
import dev.dannytaylor.luminance.client.shaders.Shader;
import dev.dannytaylor.luminance.client.shaders.ShaderRegistryEntry;
import dev.dannytaylor.luminance.client.shaders.Shaders;
import dev.dannytaylor.luminance.client.shaders.uniforms.Uniform;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands;
import dev.dannytaylor.perspective.seam.client.events.SeamClientEvents;
import dev.dannytaylor.perspective.seam.mixin.client.render.GameRendererAccessor;
import net.minecraft.client.input.MouseButtonInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.joml.Vector2i;

public class SoupRenderer implements Runnables.LevelRender {
    public final List<ShaderLayer> shaderLayers;
    public ShaderLayer activeLayer;

    private final SoupSpectateHandler spectateHandler;

    private List<Identifier> validUniforms;

    private RenderLocations.RenderLocation<?> renderLocation;

    public final Map<Identifier, Map<String, Group>> shaderGroups;
    public final Map<Identifier, Map<String, List<ShaderRegistryEntry>>> shaderGroupRegistries;

    public static final Identifier modifierRegistry = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "modifiers");

    public int randomTimer;

    public SoupRenderer() {
        shaderLayers = new ArrayList<>();
        shaderGroups = new HashMap<>();
        shaderGroupRegistries = new HashMap<>();
        renderLocation = RenderLocations.GAME;

        spectateHandler = new SoupSpectateHandler();
        Events.SpectatorHandlers.register(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "spectate_handler"), spectateHandler);
        SeamClientEvents.AfterVanillaPostEffectRender.register(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "rendering"), (data) -> {
            if (SouperSecretSettingsClient.soupData.config.disableState == 0) {
                if (spectateHandler.shaderLayer != null) {
                    Runnables.LevelRender.fromGameData(spectateHandler.shaderLayer::render, data);
                    ShaderLayer.renderCleanup(null);
                }
                if (renderLocation == RenderLocations.GAME) {
                    Runnables.LevelRender.fromGameData(this, data);
                }
            }
        });
        SeamClientEvents.AfterUiRender.register(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "rendering"), (data) -> {
            if (renderLocation == RenderLocations.UI && SouperSecretSettingsClient.soupData.config.disableState == 0) {
                Runnables.LevelRender.fromGameData(this, data);
            }
        });

        Events.OnShaderDataReset.register(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "reset"), () -> {
            clearAll();
            SouperSecretSettingsClient.actions.clear();
            shaderGroupRegistries.clear();
            SouperSecretSettingsClient.soupRenderer.shaderGroups.clear();
        });
        Events.OnShaderDataRegistered.register(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "add"), (shaderRegistryEntry, registries) -> runForGroups(shaderRegistryEntry, registries, (registry, group) -> shaderGroupRegistries.computeIfAbsent(registry, (i) -> new HashMap<>()).computeIfAbsent(group, (i) -> new ArrayList<>()).add(shaderRegistryEntry)));
        Events.OnShaderDataRemoved.register(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "remove"), (shaderRegistryEntry, registries) -> runForGroups(shaderRegistryEntry, registries, (registry, group) -> {
            Map<String, List<ShaderRegistryEntry>> map = shaderGroupRegistries.get(registry);
            if (map != null) {
                List<ShaderRegistryEntry> list = map.get(group);
                list.remove(shaderRegistryEntry);
            }
        }));
        SeamClientEvents.AfterClientResourceReload.register(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "reload"), this::loadDefault);

        Events.BeforeShaderRender.register(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "before_render"), new OverrideManager.BeforeShaderRender());
        Events.AfterShaderRender.register(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "after_render"), new OverrideManager.AfterShaderRender());

        SeamClientEvents.OnMouseScroll.register(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "adjust_values"), this::onMouseScroll);
        SeamClientEvents.OnMouseButton.register(Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "reset_values"), this::onMouseButton);
    }

    @Override
    public void run(Runnables.LevelRender.Data data) {
        if (shaderLayers != null) {
            for (ShaderLayer layer : shaderLayers) {
                layer.render(data);
            }
        }

        ShaderLayer.renderCleanup(data.builder());
    }

    public void tick() {
        if (randomTimer > 0) {
            randomTimer--;
            if (randomTimer == 0) {
                SouperSecretSettingsCommands.shaderCommand.removeAll(null);
            }
        }
    }

    @Nullable
    public List<ShaderData> getShaderAdditions(ShaderLayer layer, Identifier registry, Identifier id, int amount, int position, boolean log) {
        if (id.getNamespace().equals("minecraft") && id.getPath().startsWith("random")) {
            int i = id.getPath().indexOf("_");
            return getRandomShaders(layer, registry, i == -1 ? null : id.getPath().substring(i+1), amount, position);
        }

        ShaderRegistryEntry shaderRegistry = getRegistryEntry(registry, id);
        if (shaderRegistry == null) {
            if (log) {
                SouperSecretSettingsClient.say("shader.missing", 1, id);
            }
            return null;
        }

        List<ShaderData> shaders = new ArrayList<>();

        Shader shader = new Shader(shaderRegistry, this::getRenderLocation);
        int i = 0;
        while (i < amount) {
            shaders.add(new ShaderData(shader));
            i++;
        }

        return shaders;
    }

    @Nullable
    private List<ShaderData> getRandomShaders(ShaderLayer layer, Identifier registry, String group, int amount, int position) {
        List<ShaderData> shaders = new ArrayList<>();
        ShaderRegistryEntry shaderRegistry = null;

        if (layer != null && !layer.shaders.isEmpty()) {
            if (position == 0) {
                shaderRegistry = layer.shaders.getFirst().shader.getShaderData();
            } else {
                if (position < 0 || position > layer.shaders.size()) {
                    position = layer.shaders.size();
                }
                shaderRegistry = layer.shaders.get(position - 1).shader.getShaderData();
            }
        }

        int i = 0;
        while (i < amount) {
            shaderRegistry = getRandomShader(registry, group, shaderRegistry);
            if (shaderRegistry == null) {
                SouperSecretSettingsClient.say("shader.registry_empty", 1);
                return null;
            }
            shaders.add(new ShaderData(new Shader(shaderRegistry, this::getRenderLocation)));
            i++;
        }

        return shaders;
    }

    private ShaderRegistryEntry getRandomShader(Identifier registry, String groupName, ShaderRegistryEntry previous) {
        List<ShaderRegistryEntry> registryEntries;
        if (groupName == null) {
            registryEntries = Shaders.getRegistry(registry);
        } else {
            Group group = getShaderGroups(registry).get(groupName);
            if (group == null) {
                registryEntries = null;
            } else {
                registryEntries = group.getComputed(registry, groupName);
            }
        }

        if (registryEntries == null) {
            return null;
        }

        int size = registryEntries.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return registryEntries.getFirst();
        }

        RandomSource random = ((GameRendererAccessor)ClientData.minecraft.gameRenderer).seam$getRandom();
        ShaderRegistryEntry newShader;

        int attempts = 0;
        do {
            newShader = registryEntries.get(random.nextIntBetweenInclusive(0, size-1));
            attempts++;
        } while (attempts < 100 && previous == newShader);

        return newShader;
    }

    public static ShaderRegistryEntry getRegistryEntry(Identifier registry, Identifier identifier) {
        List<ShaderRegistryEntry> registryEntries = Shaders.getRegistry(registry);

        for (ShaderRegistryEntry shaderRegistry : registryEntries) {
            if (shaderRegistry.getID().equals(identifier)) {
                return shaderRegistry;
            }
        }

        if (identifier.getNamespace().equals("minecraft")) {
            for (ShaderRegistryEntry shaderRegistry : registryEntries) {
                if (shaderRegistry.getID().getPath().equals(identifier.getPath())) {
                    return shaderRegistry;
                }
            }
        }

        return null;
    }

    public void clearAll() {
        shaderLayers.clear();
    }

    public void loadDefault() {
        activeLayer = new ShaderLayer("default");
        shaderLayers.add(activeLayer);
    }

    public List<Identifier> getValidUniforms() {
        if (validUniforms == null) {
            validUniforms = new ArrayList<>();
            for (Map.Entry<Identifier, Uniform> entry : Events.ShaderUniform.registry.entrySet()) {
                if (entry.getValue().getLength() == 1) {
                    validUniforms.add(entry.getKey());
                }
            }
        }

        return validUniforms;
    }

    public void cycleRenderLocation(Button buttonWidget) {
        setRenderLocation(renderLocation == RenderLocations.UI ? RenderLocations.GAME : RenderLocations.UI);
        buttonWidget.setMessage(getRenderLocationText());
    }

    public void setRenderLocation(RenderLocations.RenderLocation<?> renderLocation) {
        this.renderLocation = renderLocation;
    }

    public RenderLocations.RenderLocation<?> getRenderLocation() {
        return renderLocation;
    }

    public Component getRenderLocationText() {
        return SouperSecretSettingsClient.translate(renderLocation == RenderLocations.UI ? "gui.ui" : "gui.game");
    }

    private void runForGroups(ShaderRegistryEntry shaderRegistryEntry, List<Identifier> registries, BiConsumer<Identifier, String> consumer) {
        List<String> groups = getGroups(shaderRegistryEntry);
        for (Identifier registry : registries) {
            for (String group : groups) {
                consumer.accept(registry, group);
            }
        }
    }

    public List<String> getGroups(ShaderRegistryEntry shaderRegistryEntry) {
        JsonElement soup = shaderRegistryEntry.getCustom().get(SouperSecretSettingsClient.MODID);
        if (soup != null) {
            try {
                JsonElement element = soup.getAsJsonObject().get("groups");
                if (element != null) {
                    JsonArray jsonGroups = element.getAsJsonArray();
                    List<String> groups = new ArrayList<>(jsonGroups.size());
                    for (JsonElement jsonElement : jsonGroups) {
                        try {
                            groups.add(jsonElement.getAsString());
                        } catch (IllegalStateException ignored) {}
                    }
                    return groups;
                }
            } catch (IllegalStateException ignored) {}
        }

        return List.of("edible");
    }

    private static final Identifier[] modifierChains = new Identifier[]{
            Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "before_layer_render"),
            Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "before_shader_render"),
            Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "after_shader_render"),
            Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "after_layer_render")
    };

    public Identifier[] getRegistryChains(@Nullable Identifier registry) {
        if (modifierRegistry.equals(registry)) {
            return modifierChains;
        } else {
            return new Identifier[]{null};
        }
    }

    @NotNull
    public Map<String, Group> getShaderGroups(Identifier registry) {
        if (!shaderGroups.containsKey(registry)) {
            Map<String, Group> map = new HashMap<>();
            SouperSecretSettingsClient.soupData.loadGroups(map, registry);
            shaderGroupRegistries.get(registry).forEach((name, group) -> map.putIfAbsent(name, new Group(List.of("+random_"+name))));
            shaderGroups.put(registry, map);
            return map;
        }
        return shaderGroups.get(registry);
    }

    public void fixActiveLayer() {
        if (!shaderLayers.isEmpty() && !shaderLayers.contains(activeLayer)) {
            activeLayer = shaderLayers.getLast();
        }
    }

    public boolean onMouseScroll(long windowHandle, double horizontal, double vertical, Vector2i scroll) {
        boolean cancel = false;
        for (ShaderLayer layer : shaderLayers) {
            if (layer.onMouseScroll(scroll)) {
                cancel = true;
            }
        }
        if (spectateHandler.shaderLayer != null && spectateHandler.shaderLayer.onMouseScroll(scroll)) {
            cancel = true;
        }
        return cancel;
    }

    public boolean onMouseButton(long windowHandle, MouseButtonInfo mouseButtonInfo, int action) {
        boolean cancel = false;
        for (ShaderLayer layer : shaderLayers) {
            if (layer.onMouseButton(mouseButtonInfo)) {
                cancel = true;
            }
        }
        if (spectateHandler.shaderLayer != null && spectateHandler.shaderLayer.onMouseButton(mouseButtonInfo)) {
            cancel = true;
        }
        return cancel;
    }
}
