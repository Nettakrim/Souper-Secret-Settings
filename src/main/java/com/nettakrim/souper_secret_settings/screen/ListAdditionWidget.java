package com.nettakrim.souper_secret_settings.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ListAdditionWidget extends TextFieldWidget {
    protected Supplier<List<String>> validAdditions;
    protected Consumer<String> onSubmit;

    private String currentSuggestion = null;

    public ListAdditionWidget(TextRenderer textRenderer, int x, int width, int height, Text message, Supplier<List<String>> validAdditions, Consumer<String> onSubmit) {
        super(textRenderer, x, 0, width, height, message);

        this.validAdditions = validAdditions;
        this.onSubmit = onSubmit;

        setChangedListener(this::onChange);
    }

    protected void onChange(String s) {
        if (s.isEmpty()) {
            setSuggestion(null);
            return;
        }

        List<String> suggestions = validAdditions.get();
        int length = s.length();
        for (String suggestion : suggestions) {
            if (suggestion.length() >= length && s.equals(suggestion.substring(0, length))) {
                setSuggestion(suggestion.substring(length));
                return;
            }
        }

        setSuggestion(null);
    }

    @Override
    public void setSuggestion(@Nullable String suggestion) {
        currentSuggestion = suggestion;
        super.setSuggestion(suggestion);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isFocused()) {
            if (keyCode == 257) {
                onSubmit.accept(getText());
                setText("");
                return true;
            }
            if (keyCode == 258 && !currentSuggestion.isEmpty()) {
                setText(getText()+currentSuggestion);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
