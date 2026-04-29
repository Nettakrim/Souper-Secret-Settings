package com.nettakrim.souper_secret_settings.gui.custom;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ListCategory extends CreationCategory {
    private final Component text;
    private final ImmutableList<CreationEntry> children;

    private ListCategory(Component text, ImmutableList<CreationEntry> children) {
        this.text = text;
        this.children = children;
    }

    @Override
    public List<CreationEntry> getChildren() {
        return children;
    }

    @Override
    public Component getText() {
        return text;
    }

    public static class Builder {
        private final Component text;
        private final ImmutableList.Builder<CreationEntry> builder;

        public Builder(String literalText) {
            this(Component.literal(literalText));
        }

        public Builder(Component text) {
            this.text = text;
            builder = new ImmutableList.Builder<>();
        }

        public void add(CreationEntry entry) {
            builder.add(entry);
        }

        public CreationEntry build() {
            return new ListCategory(text, builder.build());
        }
    }
}
