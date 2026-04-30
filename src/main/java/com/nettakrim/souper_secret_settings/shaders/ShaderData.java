package com.nettakrim.souper_secret_settings.shaders;

import com.nettakrim.souper_secret_settings.shaders.custom.chain.ChainGraph;
import dev.dannytaylor.luminance.client.events.Runnables;
import dev.dannytaylor.luminance.client.shaders.Shader;
import dev.dannytaylor.luminance.client.shaders.ShaderRegistryEntry;
import dev.dannytaylor.luminance.client.shaders.interfaces.PostChainInterface;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ShaderData implements Toggleable {
    public final @NotNull PostChainInterface postChainInterface;
    public final @NotNull Identifier shaderID;
    public final @Nullable ShaderRegistryEntry registryEntry;
    public final @Nullable ChainGraph chainGraph;

    public final Map<Identifier, ChainData> chainDatas;

    public boolean active = true;
    public boolean expanded = false;

    static long uuidCounter = 0;
    private final Identifier uuid;

    public ShaderData(@NotNull Shader shader) {
        this(get(shader), shader.getShaderId(), shader.getShaderData(), null);
    }

    private static @NotNull PostChainInterface get(Shader shader) {
        if (shader.getPostChain() == null) {
            shader.loadPostChain();
        }
        return shader.getPostChain();
    }

    public ShaderData(@NotNull PostChainInterface postChainInterface, @NotNull Identifier shaderID, @Nullable ShaderRegistryEntry registryEntry, @Nullable ChainGraph chainGraph) {
        this.postChainInterface = postChainInterface;
        this.shaderID = shaderID;
        this.registryEntry = registryEntry;
        this.chainGraph = chainGraph;

        Set<Identifier> customChains = postChainInterface.luminance$getCustomChainNames();

        List<PostPass> defaultPasses = postChainInterface.luminance$getPasses(null);

        if (defaultPasses.isEmpty()) {
            chainDatas = new HashMap<>(customChains.size());
        } else {
            chainDatas = new HashMap<>(customChains.size() + 1);
            chainDatas.put(null, new ChainData(defaultPasses));
        }

        for (Identifier customChain : customChains) {
            List<PostPass> passes = postChainInterface.luminance$getPasses(customChain);
            assert passes != null;
            chainDatas.put(customChain, new ChainData(passes));
        }

        uuid = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, String.valueOf(uuidCounter++));
    }

    public boolean render(Runnables.LevelRender.Data data, @Nullable Identifier chain) {
        if (!active) return false;
        if (chain != null && !postChainInterface.luminance$getCustomChainNames().contains(chain)) {
            return false;
        }

        postChainInterface.luminance$setPersistentBufferSource(uuid);
        postChainInterface.luminance$render(data.builder(), data.textureWidth(), data.textureHeight(), data.targetBundle(), chain);
        postChainInterface.luminance$setPersistentBufferSource(null);
        return true;
    }

    public ChainData getPassData(@Nullable Identifier chain) {
        return chainDatas.get(chain);
    }

    public int getRenderPassCount(@Nullable Identifier chain) {
        ChainData chainData = chainDatas.get(chain);
        if (chainData == null) {
            return 0;
        }

        return chainData.passBlocks.size();
    }

    public Component getTranslatedName() {
        String s = shaderID.toString();
        return Component.translatableWithFallback("gui.luminance.shader."+s.replace(':','.'), s);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean to) {
        active = to;
    }
}
