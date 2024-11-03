package com.nettakrim.souper_secret_settings.gui.shaders;

import com.mclegoman.luminance.client.shaders.ShaderDataloader;
import com.mclegoman.luminance.client.shaders.ShaderRegistry;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.ListScreen;
import com.nettakrim.souper_secret_settings.gui.ListWidget;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.ShaderStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StackScreen extends ListScreen<ShaderData> {
    public final ShaderStack stack;

    public StackScreen(ShaderStack stack) {
        super(Text.literal(""));
        this.stack = stack;
    }

    @Override
    protected List<ShaderData> getListValues() {
        return stack.shaderDatas;
    }

    @Override
    protected ListWidget createListWidget(ShaderData value) {
        return new ShaderWidget(value, this, listGap, listWidth);
    }

    @Override
    public List<String> getAdditions() {
        List<String> shaders = new ArrayList<>(ShaderDataloader.registry.size()+1);

        for (ShaderRegistry shaderRegistry : ShaderDataloader.registry) {
            shaders.add(shaderRegistry.getID().toString());
        }
        if (shaders.size() > 1) {
            shaders.add("random");
        }

        Collections.sort(shaders);

        return shaders;
    }

    @Override
    public void addAddition(String addition) {
        SouperSecretSettingsClient.soupRenderer.addShader(Identifier.of(addition), 1);
        SouperSecretSettingsClient.client.setScreen(new StackScreen(SouperSecretSettingsClient.soupRenderer.getActiveStack()));
    }
}
