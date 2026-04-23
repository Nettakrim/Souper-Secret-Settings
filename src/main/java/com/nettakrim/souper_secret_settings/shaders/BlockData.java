package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.UniformBlock;
import com.mclegoman.luminance.client.shaders.UniformInstance;
import com.mclegoman.luminance.client.shaders.CustomPassData;
import com.mclegoman.luminance.client.shaders.interfaces.PostPassInterface;
import com.mclegoman.luminance.client.shaders.overrides.*;
import com.mclegoman.luminance.client.shaders.uniforms.Uniform;
import com.mclegoman.luminance.client.shaders.uniforms.UniformVector;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import net.minecraft.resources.Identifier;

import java.util.*;

public class BlockData {
    public final UniformBlock block;
    public final ArrayList<UniformData> uniformDatas;
    public final BitSet uniformExpanded;

    public BlockData(UniformBlock block, PostPassInterface pass, Identifier dataPath) {
        this.block = block;
        uniformDatas = new ArrayList<>(block.uniforms.size());
        uniformExpanded = new BitSet(block.uniforms.size());

        storeDefaults(pass, dataPath);
        readDefaults(pass, dataPath);
    }


    private void readDefaults(PostPassInterface pass, Identifier dataPath) {
        //noinspection OptionalGetWithoutIsPresent
        DefaultData defaultData = (DefaultData)pass.luminance$getCustomData(dataPath).get();

        for (UniformData defaultUniformData : defaultData) {
            PerValueOverride uniformOverride = new PerValueOverride(defaultUniformData.override.getStrings());
            for (int i = 0; i < uniformOverride.overrideSources.size(); i++) {
                OverrideSource overrideSource = uniformOverride.overrideSources.get(i);
                if (overrideSource instanceof UniformSource uniformSource) {
                    uniformOverride.overrideSources.set(i, new ParameterOverrideSource(uniformSource));
                }
            }

            MapConfig configOverride = new MapConfig(Map.of());
            configOverride.mergeWithConfig(defaultUniformData.config);

            uniformDatas.add(new UniformData(defaultUniformData, uniformOverride, configOverride));
        }
    }

    private void storeDefaults(PostPassInterface pass, Identifier dataPath) {
        if (pass.luminance$getCustomData(dataPath).isPresent()) {
            return;
        }

        DefaultData data = new DefaultData(block.uniforms.size());

        for (UniformInstance uniformInstance : block.uniforms) {
            // TODO: some of this maybe doesnt need to create new instances always?
            MapConfig configOverride = new MapConfig(Map.of());
            PerValueOverride defaultOverride = null;

            // copy over config, changing any un-indexed values to be indexed
            if (uniformInstance.config != null) {
                Set<String> names = uniformInstance.config.getNames();
                for (String name : names) {
                    List<Object> objects = uniformInstance.config.getObjects(name);
                    if (objects == null) {
                        continue;
                    }
                    int separator = name.indexOf('_');

                    boolean needsIndexing = true;
                    if (separator > 0) {
                        try {
                            int index = Integer.parseInt(name.substring(0, separator));
                            if (index >= 0 && index <= uniformInstance.length()) {
                                needsIndexing = false;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    if (needsIndexing) {
                        for (int i = 0; i < uniformInstance.length(); i++) {
                            configOverride.config().putIfAbsent(i + "_" + name, objects);
                        }
                    } else {
                        configOverride.config().put(name, objects);
                    }
                }
            }

            // get override as a PerValueOverride
            if (uniformInstance.override instanceof PerValueOverride perValueOverride) {
                defaultOverride = perValueOverride;
            }
            if (defaultOverride == null) {
                defaultOverride = new PerValueOverride(List.of());
                for (Number number : uniformInstance.defaultValue) {
                    defaultOverride.overrideSources.add(ParameterOverrideSource.parameterSourceFromString(number.toString()));
                }
            }

            for (int i = 0; i < uniformInstance.length(); i++) {
                if (i >= defaultOverride.overrideSources.size()) {
                    defaultOverride.overrideSources.add(null);
                    continue;
                }

                // add template overrides if they are needed
                OverrideSource overrideSource = defaultOverride.overrideSources.get(i);
                if (overrideSource instanceof UniformSource uniformSource) {
                    defaultOverride.overrideSources.set(i, new ParameterOverrideSource(uniformSource));
                    // get uniform range, since the template will have it as 0 and 1
                    configOverride.config().putIfAbsent(i + "_range", getRange(uniformSource));
                    // copy template values
                    PerValueConfig perValueConfig = new PerValueConfig(overrideSource.getTemplateConfig(), i);
                    perValueConfig.setIndex(-1);
                    configOverride.mergeWithConfig(perValueConfig);
                }
            }

            data.add(new UniformData(null, defaultOverride, configOverride));
        }

        pass.luminance$putCustomData(dataPath, data);
    }

    private static List<Object> getRange(UniformSource uniformSource) {
        Uniform uniform = uniformSource.getUniform();
        if (uniform == null || uniform.rangeCanChange()) {
            return new ArrayList<>(Collections.nCopies(2, null));
        }

        float a = 0;
        float b = 1;
        Optional<UniformVector> min = uniformSource.getUniform().getMin(null, null);
        Optional<UniformVector> max = uniformSource.getUniform().getMax(null, null);
        if (min.isPresent() && max.isPresent()) {
            a = min.get().values.getFirst();
            b = max.get().values.getFirst();
        }

        return List.of(a,b);
    }

    public static class DefaultData extends ArrayList<UniformData> implements CustomPassData {
        public DefaultData(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public CustomPassData copy() {
            // doesn't need to create a new instance since default values are never changed
            return this;
        }
    }
}
