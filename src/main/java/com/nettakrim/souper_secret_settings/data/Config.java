package com.nettakrim.souper_secret_settings.data;

import dev.dannytaylor.luminance.client.shaders.ShaderRegistryEntry;
import dev.dannytaylor.luminance.client.shaders.Shaders;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.Actions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Config {
    boolean transferredOldData;

    public ItemStack randomItem;
    public ItemStack clearItem;
    public String randomShader;
    public int randomCount;
    public int randomDuration;
    public boolean randomSound;

    public int disableState;
    public boolean warning;
    public int messageFilter;
    public int undoLimit;

    public static final Codec<Config> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.BOOL.fieldOf("transferredOldData").forGetter((config) -> config.transferredOldData),
            ItemStack.CODEC.optionalFieldOf("randomItem", new ItemStack(Items.BEETROOT_SOUP)).forGetter((config -> config.randomItem)),
            ItemStack.CODEC.optionalFieldOf("clearItem", new ItemStack(Items.MILK_BUCKET)).forGetter((config -> config.clearItem)),
            Codec.STRING.optionalFieldOf("randomShader", "random_edible").forGetter((config -> config.randomShader)),
            Codec.INT.optionalFieldOf("randomCount", 1).forGetter((config -> config.randomCount)),
            Codec.INT.optionalFieldOf("randomDuration", 0).forGetter((config -> config.randomDuration)),
            Codec.BOOL.optionalFieldOf("randomSound", true).forGetter((config -> config.randomSound)),
            Codec.INT.optionalFieldOf("disableState", 0).forGetter((config -> config.disableState)),
            Codec.BOOL.optionalFieldOf("warning", true).forGetter((config) -> config.warning),
            Codec.INT.optionalFieldOf("messageFilter", 0).forGetter(config -> config.messageFilter),
            Codec.INT.optionalFieldOf("undoLimit", Actions.defaultLength).forGetter(config -> config.undoLimit)
            ).apply(instance, Config::new));

    public Config(boolean transferredOldData, ItemStack randomItem, ItemStack clearItem, String randomShader, int randomCount, int randomDuration, boolean randomSound, int disableState, boolean warning, int messageFilter, int undoLimit) {
        this.transferredOldData = transferredOldData;

        this.randomItem = randomItem;
        this.clearItem = clearItem;
        this.randomShader = randomShader;
        this.randomCount = randomCount;
        this.randomDuration = randomDuration;
        this.randomSound = randomSound;

        this.disableState = disableState;
        this.warning = warning;
        this.messageFilter = messageFilter;
        this.undoLimit = undoLimit;
    }

    public static Config getDefaultConfig() {
        return new Config(false, new ItemStack(Items.BEETROOT_SOUP), new ItemStack(Items.MILK_BUCKET), "random_edible", 1, 0, true, 0, true, 0, Actions.defaultLength);
    }

    public void transferOldData() {
        if (transferredOldData) {
            return;
        }
        SouperSecretSettingsClient.log("transferring old data...");

        File data = FabricLoader.getInstance().getGameDir().resolve("souper_secret_settings.txt").toFile();
        if (data.exists()) {
            try {
                Scanner scanner = new Scanner(data);
                if (scanner.hasNextLine()) scanner.nextLine();

                while (scanner.hasNextLine()) {
                    String s = scanner.nextLine();
                    String[] sections = s.split(": ");
                    if (sections.length != 2) continue;
                    try {
                        transferRecipe(sections[0], sections[1]);
                    } catch (Exception e) {
                        SouperSecretSettingsClient.log("failed to transfer recipe", s);
                    }
                }

                scanner.close();
            } catch (IOException e) {
                SouperSecretSettingsClient.log("failed to transfer old data", e);
            }
        }

        transferredOldData = true;
        SouperSecretSettingsClient.soupData.saveConfig();
    }

    private void transferRecipe(String name, String data) {
        Path path = SouperSecretSettingsClient.soupData.getLayerPath(name);
        if (path.toFile().exists()) {
            SouperSecretSettingsClient.log("layer", name, "already exists, skipping...");
            return;
        }

        String[] shaderIDs = data.split("/");

        List<LayerCodecs.Shader> shaders = new ArrayList<>();
        for (String id : shaderIDs) {
            int split;
            for (split = 0; split < id.length(); split++) {
                if (!Character.isDigit(id.charAt(split))) {
                    break;
                }
            }
            int count = Integer.parseInt(id.substring(0, split));
            id = id.substring(split);
            id = switch(id) {
                case "pixels" -> "pixelated";
                case "blur" -> "box_blur";
                case "sepia" -> "soup:sepia";
                case "halftone" -> "halftone_rgb";
                default -> id;
            };

            Optional<ShaderRegistryEntry> registryEntry = Shaders.guessPostShader(Shaders.getMainRegistryId(), id);
            if (registryEntry.isPresent()) {
                id = registryEntry.get().getID().toString();
            }

            LayerCodecs.Shader shader = new LayerCodecs.Shader(id, Optional.empty(), true);
            for (int i = 0; i < count; i++) {
                shaders.add(shader);
            }
        }

        LayerCodecs layer = new LayerCodecs(Optional.of(shaders), Optional.empty(), Optional.empty());
        SoupData.saveToPath(LayerCodecs.CODEC, path, layer);
    }
}
