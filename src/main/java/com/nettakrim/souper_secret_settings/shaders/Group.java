package com.nettakrim.souper_secret_settings.shaders;

import dev.dannytaylor.luminance.client.shaders.ShaderRegistryEntry;
import dev.dannytaylor.luminance.client.shaders.Shaders;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import net.minecraft.resources.Identifier;

public class Group {
    public final List<String> entries;

    private List<ShaderRegistryEntry> computed = null;
    private boolean needsUpdate = true;

    public boolean changed;

    @Nullable
    public File file = null;

    public Group() {
        this.entries = new ArrayList<>();
    }

    public Group(List<String> entries) {
        this.entries = new ArrayList<>(entries);
    }

    public static final Codec<Group> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.STRING.listOf().fieldOf("entries").forGetter((group -> group.entries))).apply(instance, Group::new));

    public List<Integer> getStepAmounts(Identifier registry, String name) {
        int previous = 0;
        List<Integer> steps = new ArrayList<>(entries.size());
        Set<ShaderRegistryEntry> shadersSet = new HashSet<>(computed == null ? 16 : computed.size());

        for (String entry : entries) {
            String id = entry.substring(1);
            if (id.startsWith("random_")) {
                Group group = getGroup(registry, id);
                if (group != null && group != this && group.hasRecursion(registry, Set.of(this))) {
                    steps.add(null);
                    continue;
                }
            }

            computationStep(registry, name, shadersSet, entry);

            int next = shadersSet.size();
            steps.add(next-previous);
            previous = next;
        }

        return steps;
    }

    protected boolean hasRecursion(Identifier registry, Set<Group> visited) {
        Set<Group> next = new HashSet<>(visited);
        next.add(this);


        for (String entry : entries) {
            String id = entry.substring(1);
            if (id.startsWith("random_")) {
                Group group = getGroup(registry, id);
                if (group != null && group != this) {
                    if (visited.contains(group) || group.hasRecursion(registry, next)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public List<ShaderRegistryEntry> getComputed(Identifier registry, String name) {
        if (needsUpdate) {
            needsUpdate = false;

            if (hasRecursion(registry, Set.of())) {
                computed = List.of();
                return computed;
            }

            Set<ShaderRegistryEntry> shadersSet = new HashSet<>();
            for (String entry : entries) {
                computationStep(registry, name, shadersSet, entry);
            }
            computed = shadersSet.stream().toList();
        }
        return computed;
    }

    private void computationStep(Identifier registry, String name, Set<ShaderRegistryEntry> shadersSet, String entry) {
        boolean remove = entry.charAt(0) == '-';
        String id = entry.substring(1);
        if (id.startsWith("random_")) {
            Group group = getGroup(registry, id);
            if (group != null) {
                List<ShaderRegistryEntry> groupRegistries;

                if (group == this) {
                    groupRegistries = SouperSecretSettingsClient.soupRenderer.shaderGroupRegistries.get(registry).get(name);
                    if (groupRegistries == null) {
                        return;
                    }
                } else {
                    groupRegistries = group.getComputed(registry, id.substring(7));
                }

                if (remove) {
                    groupRegistries.forEach(shadersSet::remove);
                } else {
                    shadersSet.addAll(groupRegistries);
                }
            }
            return;
        }

        if (id.equals("all")) {
            shadersSet.clear();
            if (!remove) {
                shadersSet.addAll(Shaders.getRegistry(registry));
            }
            return;
        }

        Shaders.guessPostShader(registry, id).ifPresent(registryEntry -> {
            if (remove) {
                shadersSet.remove(registryEntry);
            } else {
                shadersSet.add(registryEntry);
            }
        });
    }

    protected Group getGroup(Identifier registry, String randomID) {
        Map<String, Group> registryGroups = SouperSecretSettingsClient.soupRenderer.getShaderGroups(registry);
        return registryGroups.get(randomID.substring(7));
    }

    public void requestUpdate() {
        needsUpdate = true;
    }

    public static String getNextName(Map<String, Group> map) {
        String name;
        int i = 1;
        do {
            name = "user/group_"+i;
            i++;
        } while (map.containsKey(name));
        return name;
    }

    public void deleteFile() {
        if (file != null) {
            if (file.delete()) {
                file = null;
            } else {
                SouperSecretSettingsClient.log("Failed to delete file", file);
            }
        }
    }
}
