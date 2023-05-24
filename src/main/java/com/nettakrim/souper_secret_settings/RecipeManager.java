package com.nettakrim.souper_secret_settings;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class RecipeManager {
    private final File data;

    private final HashMap<String,String> recipies = new HashMap<>();

    private boolean changesMade = false;

    private boolean stopOverwrite = true;

    public RecipeManager() {
        data = new File(SouperSecretSettingsClient.client.runDirectory+"/souper_secret_settings.txt");
        try {
            if (data.exists()) {
                Scanner scanner = new Scanner(data);
                if (scanner.hasNextLine()) scanner.nextLine();
                while (scanner.hasNextLine()) {
                    String s = scanner.nextLine();
                    String[] sections = s.split(": ");
                    if (sections.length != 2) continue;
                    setRecipe(sections[0], sections[1]);
                }
                scanner.close();
            }
        } catch (IOException e) {
            SouperSecretSettingsClient.LOGGER.info("Failed to load data");
        }
    }

    public void save() {
        if (!changesMade) return;
        try {
            if (!data.exists()) data.createNewFile();
            FileWriter writer = new FileWriter(data);
            StringBuilder s = new StringBuilder("- Souper Secret Recipes -");
            for (Map.Entry<String,String> entry : recipies.entrySet()) {
                s.append("\n").append(entry.getKey()).append(": ").append(entry.getValue());
            }
            writer.write(s.toString());
            writer.close();
            changesMade = false;
        } catch (IOException e) {
            SouperSecretSettingsClient.LOGGER.info("Failed to save data");
        }
    }

    public void setCurrentRecipe(String name) {
        setRecipe(name, getCurrentRecipeData());
    }

    public void setRecipe(String name, String data) {
        if (name.contains(": ") || name.equals("random")) {
            return;
        }
        if (stopOverwrite && recipies.containsKey(name)) {
            SouperSecretSettingsClient.client.player.sendMessage(Text.translatable(SouperSecretSettingsClient.MODID+".confirm_overwrite", name));
            stopOverwrite = false;
            return;
        }
        stopOverwrite = true;
        changesMade = true;
        if (data.equals("")) {
            recipies.remove(name);
        } else {
            recipies.put(name, data);
        }
        save();
    }

    public void loadFromName(String name) {
        if (name.equals("random")) {
            int index = SouperSecretSettingsClient.getGameRendererAccessor().getRandom().nextInt(recipies.size());
            loadFromRecipeData(recipies.get((String)recipies.keySet().toArray()[index]));
        } else {
            String data = recipies.get(name);
            if (data == null) {
                SouperSecretSettingsClient.client.player.sendMessage(Text.translatable(SouperSecretSettingsClient.MODID+".no_recipe", name).setStyle(Style.EMPTY.withColor(0xFF5555)));
            } else {
                loadFromRecipeData(data);
            }
        }
    }

    public static boolean loadFromRecipeData(String data) {
        String[] idArray = data.split("(?<=\\D)(?=\\d)");

        if (idArray.length == 1 && !Character.isDigit(idArray[0].charAt(0))) {
            String id = idArray[0];
            ShaderData shader = SouperSecretSettingsClient.getShaderFromID(id);
            if (shader != null) {
                SouperSecretSettingsClient.getGameRendererAccessor().invokeLoadPostProcessor(shader.shader);
            } else {
                SouperSecretSettingsClient.client.gameRenderer.disablePostProcessor();
            }
            return false;
        } else {
            SouperSecretSettingsClient.clearShaders();
            for (String id : idArray) {
                String[] recipeArray = id.split("(?<=\\d)(?=\\D)");
                ShaderData shaderData = SouperSecretSettingsClient.getShaderFromID(recipeArray[1]);
                if (shaderData != null) {
                    SouperSecretSettingsClient.stackShaderData(shaderData, Integer.parseInt(recipeArray[0]));
                }
            }
            return true;
        }
    }

    public static String getCurrentRecipeData() {
        StringBuilder recipe = new StringBuilder();
        if (SouperSecretSettingsClient.postProcessorStack.size() == 0) return "";
        String lastId = SouperSecretSettingsClient.postProcessorStack.get(0).data().id;
        int stacks = 0;
        for (StackData stackData : SouperSecretSettingsClient.postProcessorStack) {
            if (stackData.data().id.equals(lastId)) {
                stacks++;
            } else {
                recipe.append(stacks).append(lastId);
                lastId = stackData.data().id;
                stacks = 1;
            }
        }
        recipe.append(stacks).append(lastId);
        return recipe.toString();
    }

    public void removeRecipe(String name) {
        recipies.remove(name);
    }

    public boolean getSuggestions(SuggestionsBuilder builder) {
        for (String key : recipies.keySet()) {
            builder.suggest(key);
        }
        return recipies.size() > 1;
    }
}
