package com.nettakrim.souper_secret_settings.actions;

import dev.dannytaylor.luminance.client.shaders.overrides.PerValueOverride;
import dev.dannytaylor.luminance.client.shaders.overrides.OverrideSource;
import dev.dannytaylor.luminance.client.shaders.uniforms.config.MapConfig;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniformChangeAction implements Action {
    private final int uniform;
    private final int valueIndex;

    private final PerValueOverride uniformOverride;
    private final MapConfig uniformConfig;

    private OverrideSource sourceBackup;
    private Map<String, List<Object>> mapBackup;

    // TODO: replace string uniform with int index
    public UniformChangeAction(int uniform, int valueIndex, PerValueOverride uniformOverride, MapConfig uniformConfig) {
        this.uniform = uniform;
        this.valueIndex = valueIndex;
        this.uniformOverride = uniformOverride;
        this.uniformConfig = uniformConfig;
    }

    @Override
    public boolean undo() {
        swap();
        return true;
    }

    @Override
    public void redo() {
        swap();
    }

    protected void swap() {
        sourceBackup = uniformOverride.overrideSources.set(valueIndex, sourceBackup);

        Map<String, List<Object>> prev = backup();

        String prefix = valueIndex +"_";
        uniformConfig.config().keySet().removeIf((s) -> s.startsWith(prefix));
        uniformConfig.config().putAll(mapBackup);

        mapBackup = prev;
    }

    @Override
    public boolean mergeWith(Action other) {
        UniformChangeAction o = (UniformChangeAction)other;
        return uniformOverride == o.uniformOverride && uniform == o.uniform && valueIndex == o.valueIndex;
    }

    @Override
    public void addToHistory() {
        if (SouperSecretSettingsClient.actions.addToHistory(this)) {
            sourceBackup = uniformOverride.overrideSources.get(valueIndex);
            mapBackup = backup();
        }
    }

    private Map<String, List<Object>> backup() {
        String prefix = valueIndex +"_";
        Map<String, List<Object>> map = new HashMap<>();
        uniformConfig.config().forEach((key, value) -> {
            if (key.startsWith(prefix)) {
                map.put(key, new ArrayList<>(value));
            }
        });
        return map;
    }
}
