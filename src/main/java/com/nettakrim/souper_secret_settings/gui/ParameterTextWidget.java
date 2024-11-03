package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.events.Events;
import com.mclegoman.luminance.client.shaders.ShaderDataloader;
import com.mclegoman.luminance.client.util.LuminanceIdentifier;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParameterTextWidget extends SuggestionTextFieldWidget {
    private final String defaultValue;

    public ParameterTextWidget(TextRenderer textRenderer, int x, int width, int height, Text message, String defaultValue) {
        super(textRenderer, x, width, height, message);
        this.defaultValue = defaultValue;
        if (defaultValue != null) {
            setListeners(this::getParameters, this::setText);
        }
    }

    protected List<String> getParameters() {
        List<String> parameters = new ArrayList<>(ShaderDataloader.registry.size()+1);

        for (LuminanceIdentifier luminanceIdentifier : Events.ShaderUniform.registry.keySet()) {
            parameters.add(luminanceIdentifier.toUnderscoreSeparatedString());
        }
        Collections.sort(parameters);
        parameters.addFirst(defaultValue);

        return parameters;
    }
}
