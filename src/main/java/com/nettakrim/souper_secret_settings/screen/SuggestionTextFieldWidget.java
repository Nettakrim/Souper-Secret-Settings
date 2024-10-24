package com.nettakrim.souper_secret_settings.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SuggestionTextFieldWidget extends TextFieldWidget {
    protected final Supplier<List<String>> validAdditions;
    protected final Consumer<String> onSubmit;

    private final List<String> currentSuggestions = new ArrayList<>();
    private int currentSuggestionIndex = 0;

    public SuggestionTextFieldWidget(TextRenderer textRenderer, int x, int width, int height, Text message, Supplier<List<String>> validAdditions, Consumer<String> onSubmit) {
        super(textRenderer, x, 0, width, height, message);

        this.validAdditions = validAdditions;
        this.onSubmit = onSubmit;

        setChangedListener(this::onChange);
    }

    protected void onChange(String s) {
        String previousSuggestion = null;
        if (!currentSuggestions.isEmpty()) {
            previousSuggestion = currentSuggestions.get(currentSuggestionIndex);
        }

        currentSuggestions.clear();
        if (s.isEmpty()) {
            setSuggestion(null);
            return;
        }

        List<String> suggestions = validAdditions.get();

        int length = s.length();
        for (String suggestion : suggestions) {
            if (suggestion.length() >= length && s.equals(suggestion.substring(0, length))) {
                currentSuggestions.add(suggestion);
            }
        }

        if (currentSuggestions.isEmpty()) {
            setSuggestion(null);
        } else {
            if (previousSuggestion == null) {
                currentSuggestionIndex = 0;
            } else {
                currentSuggestionIndex = currentSuggestions.indexOf(previousSuggestion);
                if (currentSuggestionIndex == -1) currentSuggestionIndex = 0;
            }
            setSuggestion(currentSuggestions.get(currentSuggestionIndex).substring(length));
        }
    }

    protected void cycleSuggestion(int direction) {
        int suggestions = currentSuggestions.size();
        if (suggestions == 0) return;

        currentSuggestionIndex += direction;
        if (currentSuggestionIndex < 0) {
            currentSuggestionIndex += suggestions;
        } else if (currentSuggestionIndex >= suggestions) {
            currentSuggestionIndex -= suggestions;
        }

        setSuggestion(currentSuggestions.get(currentSuggestionIndex).substring(getText().length()));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isFocused()) {
            if (keyCode == 257) {
                onSubmit.accept(getText());
                setText("");
                return true;
            }
            if (!currentSuggestions.isEmpty()) {
                if (keyCode == 258) {
                    String text = getText();
                    setText(text + currentSuggestions.get(currentSuggestionIndex).substring(text.length()));
                    return true;
                } else if (keyCode == 265) {
                    cycleSuggestion(-1);
                    return true;
                } else if (keyCode == 264) {
                    cycleSuggestion(1);
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
