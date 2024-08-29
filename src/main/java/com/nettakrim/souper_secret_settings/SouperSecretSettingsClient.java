package com.nettakrim.souper_secret_settings;

import com.nettakrim.souper_secret_settings.network.packets.ApplyShaderPacket;
import com.nettakrim.souper_secret_settings.shaders.LayerData;
import com.nettakrim.souper_secret_settings.shaders.PostLayerEffect;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.nettakrim.souper_secret_settings.commands.SouperSecretSettingsCommands;
import com.nettakrim.souper_secret_settings.mixin.GameRendererAccessor;

public class SouperSecretSettingsClient implements ClientModInitializer {
    public static final String MODID = "souper_secret_settings";
    public static final Logger LOGGER = LoggerFactory.getLogger("souper_secret_settings");

    private static GameRendererAccessor gameRendererAccessor;
    public static MinecraftClient client;

    public static boolean isSoupToggledOff;
    public static boolean soupToggleStay;

    public static final LayerData layer = new LayerData();

    public static ShaderResourceLoader shaderResourceLoader;
    public static RecipeManager recipeManager;

    public static final ArrayList<ShaderData> postShaders = new ArrayList<>();
    public static final ArrayList<ShaderData> layerEffects = new ArrayList<>();
    public static final HashMap<String, String> entityLinks = new HashMap<>();

    private static boolean warningPrimed = true;
    private final static int shaderLimit = 100;

    private static final TextColor textColor = TextColor.fromRgb(0xAAAAAA);
    private static final TextColor nameTextColor = TextColor.fromRgb(0xB6484C);

    public static Framebuffer depthFrameBuffer;
    public static boolean canFixDepth;
    public static MatrixStack renderHandMatrixStack = new MatrixStack();

    @Override
    public void onInitializeClient() {
        client = MinecraftClient.getInstance();

        shaderResourceLoader = new ShaderResourceLoader();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(shaderResourceLoader);

        recipeManager = new RecipeManager();

        ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of("expanded_shaders"), FabricLoader.getInstance().getModContainer(MODID).orElseThrow(), Text.literal("Expanded Shaders"), ResourcePackActivationType.DEFAULT_ENABLED);

        SouperSecretSettingsCommands.initialize();

        registerPackets();
    }

    /**
     * Registers the necessary packets for shader communication between client and server.
     *
     * <p>This method registers the {@link ApplyShaderPacket} for both client-to-server (C2S) and server-to-client (S2C)
     * communication. It also registers a global receiver on the client side to handle incoming {@link ApplyShaderPacket}
     * instances.</p>
     *
     * <p>When a client sends an {@link ApplyShaderPacket}, the registered receiver will:</p>
     * <ul>
     *   <li>Retrieve the shader ID from the packet.</li>
     *   <li>Clear all existing shaders if the shader ID is "clear".</li>
     *   <li>Otherwise, update the toggle state and apply the shader specified by the ID.</li>
     * </ul>
     *
     * @author RazorPlay01
     */
    private static void registerPackets() {
        PayloadTypeRegistry.playC2S().register(ApplyShaderPacket.PACKET_ID, ApplyShaderPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ApplyShaderPacket.PACKET_ID, ApplyShaderPacket.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ApplyShaderPacket.PACKET_ID, (payload, context) -> {
            ClientPlayerEntity player = context.client().player;
            context.client().execute(() -> {
                assert player != null;
                if (payload.shaderId().equalsIgnoreCase("clear")) {
                    SouperSecretSettingsClient.clearShaders();
                } else {
                    SouperSecretSettingsClient.updateToggle();
                    SouperSecretSettingsClient.stackShader(payload.shaderId(), 0);
                }
            });
        });
    }

    public static GameRendererAccessor getGameRendererAccessor() {
        if (gameRendererAccessor == null) {
            gameRendererAccessor = (GameRendererAccessor) client.gameRenderer;
        }
        return gameRendererAccessor;
    }

    public static boolean setShader(String id) {
        return stackShader(id, 0);
    }

    public static boolean stackShader(String id, int stack) {
        if (warningPrimed && stack + layer.getShaderCount() > shaderLimit) {
            say("shader.warn_stacking", shaderLimit);
            stack = shaderLimit - layer.getShaderCount();
            warningPrimed = false;
            if (stack <= 0) return false;
        }

        ShaderData shaderData = getPostShaderFromID(id);
        if (shaderData == null) {
            say("shader.missing", id);
            return false;
        }

        return stackShaderData(shaderData, stack);
    }

    public static boolean stackShaderData(ShaderData shaderData, int stack) {
        updateToggle();
        return layer.stackShaderData(shaderData, stack);
    }

    public static ShaderData getPostShaderFromID(String id) {
        if (id.equals("random")) {
            return layer.getRandomNotTop(getGameRendererAccessor().getRandom(), postShaders, true);
        } else if (id.equals("random_soup")) {
            return layer.getRandomNotTop(getGameRendererAccessor().getRandom(), postShaders, false);
        } else {
            for (ShaderData shaderData : postShaders) {
                if (id.equals(shaderData.id)) {
                    return shaderData;
                }
            }
            return null;
        }
    }

    public static boolean addLayerEffect(String id) {
        ShaderData shaderData = getLayerEffectFromID(id);
        if (shaderData == null) {
            say("shader.missing", id);
            return false;
        }

        return layer.addLayerEffectFromShader(shaderData);
    }

    public static ShaderData getLayerEffectFromID(String id) {
        if (id.equals("random")) {
            return layerEffects.get(getGameRendererAccessor().getRandom().nextInt(layerEffects.size()));
        } else {
            for (ShaderData shaderData : layerEffects) {
                if (id.equals(shaderData.id)) {
                    return shaderData;
                }
            }
            return null;
        }
    }

    public static PostEffectProcessor getPostProcessor(Identifier identifier) {
        try {
            canFixDepth = true;
            PostEffectProcessor postProcessor = new PostEffectProcessor(client.getTextureManager(), getGameRendererAccessor().getResourceManager(), client.getFramebuffer(), identifier);
            postProcessor.setupDimensions(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
            canFixDepth = false;
            return postProcessor;
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.warn("Failed to load shader \"{}\":\n{}", identifier, e);
            return null;
        }
    }

    public static boolean tryLoadEntityShader(String type) {
        String recipeData = SouperSecretSettingsClient.entityLinks.get(type);
        if (recipeData == null) return false;
        return recipeManager.loadFromRecipeData(recipeData, false);
    }

    public static PostLayerEffect getLayerEffect(ShaderData shaderData) {
        try {
            PostLayerEffect postLayerEffect = new PostLayerEffect(client.getTextureManager(), getGameRendererAccessor().getResourceManager(), client.getFramebuffer(), shaderData);
            postLayerEffect.resize(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
            return postLayerEffect;
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.warn("Failed to load layer effect \"{}\":\n{}", shaderData.shader, e);
            return null;
        }
    }

    public static void clearShaders() {
        warningPrimed = true;
        layer.clear();
        updateToggle();
    }

    public static void clearResources() {
        postShaders.clear();
        entityLinks.clear();
    }

    public static void layerEffectListAdd(String namespace, String id) {
        layerEffects.add(new ShaderData(namespace, id, false, true));
    }

    public static void layerEffectListClearNamespace(String namespace) {
        layerEffects.removeIf(data -> data.shader.getNamespace().equals(namespace));
    }

    public static void shaderListAdd(String namespace, String id, boolean soupFriendly) {
        postShaders.add(new ShaderData(namespace, id, soupFriendly, false));
    }

    public static void shaderListClearNamespace(String namespace) {
        postShaders.removeIf(data -> data.shader.getNamespace().equals(namespace));
    }

    public static void shaderListRemove(String namespace, String id) {
        postShaders.removeIf(data -> data.id.equals(id) && data.shader.getNamespace().equals(namespace));
    }

    public static void entityLinksAdd(String id, String shader) {
        entityLinks.put(id, shader);
    }

    public static void say(String key, Object... args) {
        if (client.player == null) return;
        Text text = Text.translatable(MODID + ".say").setStyle(Style.EMPTY.withColor(nameTextColor)).append(Text.translatable(MODID + "." + key, args).setStyle(Style.EMPTY.withColor(textColor)));
        client.player.sendMessage(text);
    }

    public static void say(MutableText text) {
        if (client.player == null) return;
        client.player.sendMessage(Text.translatable(MODID + ".say").setStyle(Style.EMPTY.withColor(nameTextColor)).append(text.setStyle(Style.EMPTY.withColor(textColor))));
    }

    public static void updateToggle() {
        if (isSoupToggledOff && !soupToggleStay) {
            say("shader.toggle_prompt");
            isSoupToggledOff = false;
        }
    }

    public static void sayClickHere(String actionKey, String command, boolean run, Object... formatting) {
        Text message = Text.translatable(MODID + ".say").setStyle(Style.EMPTY.withColor(textColor)).append(
                Text.translatable(SouperSecretSettingsClient.MODID + ".share.click").setStyle(Style.EMPTY
                                .withClickEvent(
                                        new ClickEvent(run ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND, command)
                                )
                                .withColor(nameTextColor))
                        .append(Text.translatable(MODID + "." + actionKey, formatting).setStyle(Style.EMPTY.withColor(textColor)))
        );
        client.player.sendMessage(message);
    }
}
