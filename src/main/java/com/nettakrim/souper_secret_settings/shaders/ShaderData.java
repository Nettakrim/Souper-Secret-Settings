package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.events.Runnables;
import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.interfaces.PostChainInterface;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ShaderData implements Toggleable {
    public Shader shader;

    public Map<Identifier, ChainData> chainDatas;

    public boolean active = true;
    public boolean expanded = false;

    static long uuidCounter = 0;
    private final Identifier uuid;

    public ShaderData(Shader shader) {
        this.shader = shader;
        if (this.shader.getPostChain() == null) {
            this.shader.loadPostChain();
        }

        PostChainInterface processor = this.shader.getPostChain();
        Set<Identifier> customChains = processor.luminance$getCustomChainNames();

        List<PostPass> defaultPasses = processor.luminance$getPasses(null);

        if (defaultPasses.isEmpty()) {
            chainDatas = new HashMap<>(customChains.size());
        } else {
            chainDatas = new HashMap<>(customChains.size() + 1);
            chainDatas.put(null, new ChainData(defaultPasses));
        }

        for (Identifier customChain : customChains) {
            List<PostPass> passes = processor.luminance$getPasses(customChain);
            assert passes != null;
            chainDatas.put(customChain, new ChainData(passes));
        }

        uuid = Identifier.fromNamespaceAndPath(SouperSecretSettingsClient.MODID, String.valueOf(uuidCounter++));
    }

    public boolean render(Runnables.LevelRender.Data data, @Nullable Identifier chain) {
        if (!active) return false;
        PostChainInterface processor = shader.getPostChain();
        if (chain != null && !processor.luminance$getCustomChainNames().contains(chain)) {
            return false;
        }

        processor.luminance$setPersistentBufferSource(uuid);
        Shaders.renderShaderFromLevelData(shader, data, chain);
        processor.luminance$setPersistentBufferSource(null);
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
        String s = shader.getShaderId().toString();
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
