package com.nettakrim.souper_secret_settings.gui.shaders;

import dev.dannytaylor.luminance.client.shaders.*;
import dev.dannytaylor.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.shaders.Group;
import com.nettakrim.souper_secret_settings.shaders.OverrideManager;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderLayer;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ShaderScreen extends ListScreen<ShaderData> {
    public final ShaderLayer layer;
    public final Identifier registry;

    public ShaderScreen(int scrollIndex, ShaderLayer layer, Identifier registry) {
        super(scrollIndex);
        this.layer = layer;
        this.registry = registry;
    }

    @Override
    protected List<ShaderData> getListValues() {
        return layer.getList(registry);
    }

    @Override
    protected ListWidget createListWidget(ShaderData value) {
        return new ShaderWidget(layer, value, this, SoupGui.listX, SoupGui.listWidth);
    }

    @Override
    public List<String> calculateAdditions() {
        return calculateAdditions(registry);
    }

    public static List<String> calculateAdditions(Identifier registry) {
        List<ShaderRegistryEntry> registryEntries = Shaders.getRegistry(registry);
        List<String> shaders = new ArrayList<>(registryEntries.size()+1);

        for (ShaderRegistryEntry shaderRegistry : registryEntries) {
            shaders.add(shaderRegistry.getID().toString());
        }

        Collections.sort(shaders);

        if (registryEntries.size() > 1) {
            shaders.addFirst("random");
        }

        Map<String, Group> registryGroups = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry);
        List<String> user = new ArrayList<>(registryGroups.keySet().size());
        List<String> random = new ArrayList<>(registryGroups.keySet().size());

        for (String s : registryGroups.keySet()) {
            if (s.startsWith("user/")) {
                user.add("random_"+s);
            } else {
                random.add("random_"+s);
            }
        }

        Collections.sort(user);
        shaders.addAll(user);
        Collections.sort(random);
        shaders.addAll(random);

        return shaders;
    }

    @Override
    public ShaderData tryGetAddition(String addition) {
        if (addition.isBlank()) {
            return null;
        }

        Identifier identifier = Shaders.guessPostShader(registry, addition).map(ShaderRegistryEntry::getID).orElse(Identifier.tryParse(addition));
        if (identifier != null) {
            List<ShaderData> shader = SouperSecretSettingsClient.soupRenderer.getShaderAdditions(layer, registry, identifier, 1, -1, true);
            if (shader != null) {
                return shader.getFirst();
            }
        }
        return null;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        OverrideManager.currentShaderIndex = 0;
        ShaderTime.currentRenderLocation = SouperSecretSettingsClient.soupRenderer.getRenderLocation();
        super.render(guiGraphics, mouseX, mouseY, delta);
        ShaderTime.currentRenderLocation = RenderLocations.UI;
    }

    @Override
    protected boolean canUseRandom() {
        return true;
    }

    @Override
    public Couple<Component,Component> getAdditionText(String addition) {
        if (addition.startsWith("random")) {
            if (addition.length() > 7) {
                String name = addition.substring(7);
                Group group = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry).get(name);
                if (group != null) {
                    return new Couple<>(Component.literal(addition), SouperSecretSettingsClient.translate("shader.group_suggestion", group.getComputed(registry, name).size()));
                }
            }

            return new Couple<>(Component.literal(addition), SouperSecretSettingsClient.translate("shader.group_suggestion", Shaders.getRegistry(registry).size()));
        }

        String s = "gui.luminance.shader."+addition.replace(':','.');
        Component description = Component.translatableWithFallback(s+".description", "");
        return new Couple<>(Component.translatableWithFallback(s, addition), description.getString().isBlank() ? null : description);
    }

    @Override
    protected boolean matchIdentifiers() {
        return true;
    }

    @Override
    protected void enterAdditionScreen() {
        minecraft.setScreen(new ShaderAdditionScreen(this));
    }
}
