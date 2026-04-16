package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.client.events.Runnables;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.interfaces.FramePassInterface;
import com.mclegoman.luminance.common.util.Couple;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.key.ScrollCalculation;
import net.minecraft.client.input.MouseButtonInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.joml.Vector2i;

public class ShaderLayer implements Toggleable {
    @NotNull
    public String name;

    public final List<ShaderData> shaders;
    public final List<ShaderData> modifiers;
    public final List<Calculation> calculations;

    public final Map<String, Float> parameterValues;

    public boolean active = true;
    public boolean expanded = false;

    private static final Identifier beforeLayerRender = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "before_layer_render");
    private static final Identifier beforeShaderRender = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "before_shader_render");
    private static final Identifier afterShaderRender = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "after_shader_render");
    private static final Identifier afterLayerRender = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "after_layer_render");

    private static ShaderLayer renderingLayer;

    private final Set<ScrollCalculation> activeScrollers = new HashSet<>();

    public ShaderLayer(@NotNull String name) {
        this.name = name;

        shaders = new ArrayList<>();
        modifiers = new ArrayList<>();
        calculations = new ArrayList<>();

        SouperSecretSettingsClient.soupData.loadLayer(this);

        disambiguateName();

        parameterValues = new HashMap<>();
    }

    public void clear() {
        shaders.clear();
        modifiers.clear();
        calculations.clear();
    }

    public void render(Runnables.LevelRender.Data data) {
        if (!active) {
            return;
        }

        renderingLayer = this;

        activeScrollers.clear();
        parameterValues.clear();
        for (Calculation calculation : calculations) {
            calculation.update(this);
        }

        Queue<Couple<ShaderData, Identifier>> shaderQueue = new LinkedList<>();
        FramePassInterface.createForcedPass(data.builder(), Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "layer_start"), () -> {
            renderingLayer = this;
            OverrideManager.startShaderQueue(shaderQueue);
        });

        renderList(modifiers, shaderQueue, data, beforeLayerRender);

        for (ShaderData shaderData : shaders) {
            if (shaderData.active) {
                renderList(modifiers, shaderQueue, data, beforeShaderRender);
                renderShader(shaderData, shaderQueue, data, null);
                renderList(modifiers.reversed(), shaderQueue, data, afterShaderRender);
                shaderQueue.add(null);
            }
        }

        renderList(modifiers.reversed(), shaderQueue, data, afterLayerRender);
    }

    public void renderList(List<ShaderData> shaders, Queue<Couple<ShaderData, Identifier>> shaderQueue, Runnables.LevelRender.Data data, @Nullable Identifier customChain) {
        for (ShaderData shaderData : shaders) {
            renderShader(shaderData, shaderQueue, data, customChain);
        }
    }

    public void renderShader(ShaderData shaderData, Queue<Couple<ShaderData, Identifier>> shaderQueue, Runnables.LevelRender.Data data, @Nullable Identifier customChain) {
        if (shaderData.render(data, customChain)) {
            shaderQueue.add(new Couple<>(shaderData, customChain));
        }
    }

    public List<ShaderData> getList(Identifier registry) {
        if (Shaders.getMainRegistryId().equals(registry)) {
            return shaders;
        }

        if (SoupRenderer.modifierRegistry.equals(registry)) {
            return modifiers;
        }

        return List.of();
    }

    public MutableComponent[] getInfo() {
        int shaders = 0;
        int modifiers = 0;
        int passes = 0;

        for (ShaderData shaderData : this.shaders) {
            if (!shaderData.active) continue;
            passes += shaderData.getRenderPassCount(null);
            shaders++;
        }

        for (ShaderData modifier : this.modifiers) {
            if (!modifier.active) continue;
            passes += modifier.getRenderPassCount(beforeLayerRender);
            passes += modifier.getRenderPassCount(beforeShaderRender)*shaders;
            passes += modifier.getRenderPassCount(afterShaderRender)*shaders;
            passes += modifier.getRenderPassCount(afterLayerRender);
            modifiers++;
        }

        return new MutableComponent[]{
                Component.translatable(SouperSecretSettingsClient.MODID+".layer.info.shaders", shaders),
                Component.translatable(SouperSecretSettingsClient.MODID+".layer.info.modifiers", modifiers),
                Component.translatable(SouperSecretSettingsClient.MODID+".layer.info.passes", passes)
        };
    }

    public static ShaderLayer getRenderingLayer() {
        return renderingLayer;
    }

    public static void renderCleanup(@Nullable FrameGraphBuilder builder) {
        renderingLayer = null;
        if (builder != null) {
            FramePassInterface.createForcedPass(builder, Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, "layer_cleanup"), () -> renderingLayer = null);
        }
    }

    public boolean isEmpty() {
        return shaders.isEmpty() && modifiers.isEmpty() && calculations.isEmpty();
    }

    public void disambiguateName() {
        String oldName = name;
        int i = 1;
        while (true) {
            int lastI = i;
            for (ShaderLayer layer : SouperSecretSettingsClient.soupRenderer.shaderLayers) {
                if (layer != this && layer.name.equals(name)) {
                    i++;
                    name = oldName+"_"+i;
                }
            }
            if (i == lastI) {
                break;
            }
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean to) {
        active = to;
    }

    public void addActiveSliderCalculation(ScrollCalculation scrollCalculation) {
        activeScrollers.add(scrollCalculation);
    }

    public boolean onMouseScroll(Vector2i scroll) {
        if (!activeScrollers.isEmpty()) {
            if (ClientData.minecraft.player != null) {
                int scrollAmount = scroll.y == 0 ? -scroll.x : scroll.y;
                for (ScrollCalculation calculation : activeScrollers) {
                    calculation.adjust(scrollAmount);
                }
                return true;
            }
        }
        return false;
    }

    public boolean onMouseButton(MouseButtonInfo mouseButtonInfo) {
        if (!activeScrollers.isEmpty()) {
            if (mouseButtonInfo.button() == 2) {
                for (ScrollCalculation calculation : activeScrollers) {
                    calculation.reset();
                }
                return true;
            }
        }
        return false;
    }
}
