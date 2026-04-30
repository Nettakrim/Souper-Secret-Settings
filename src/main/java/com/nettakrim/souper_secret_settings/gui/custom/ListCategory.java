package com.nettakrim.souper_secret_settings.gui.custom;

import com.google.common.collect.ImmutableList;
import dev.dannytaylor.luminance.common.util.Couple;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
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
        private final List<Couple<String, CreationEntry>> list;

        public Builder(String literalText) {
            this(Component.literal(literalText));
        }

        public Builder(Component text) {
            this.text = text;
            list = new ArrayList<>();
        }

        public void add(String sorting, CreationEntry entry) {
            list.add(new Couple<>(sorting, entry));
        }

        public CreationEntry build() {
            list.sort(Comparator.comparing(Couple::getFirst));
            ImmutableList.Builder<CreationEntry> builder = ImmutableList.builder();
            builder.addAll(list.stream().map(Couple::getSecond).iterator());
            return new ListCategory(text, builder.build());
        }
    }
}
