package com.nettakrim.souper_secret_settings.data;

import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.UniformInstance;
import com.mclegoman.luminance.client.shaders.overrides.PerValueOverride;
import com.mclegoman.luminance.client.shaders.overrides.OverrideSource;
import com.mclegoman.luminance.client.shaders.uniforms.config.MapConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.shaders.*;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.Calculations;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public record LayerCodecs(Optional<List<Shader>> shaders, Optional<List<Shader>> modifiers, Optional<List<CalculationData>> parameters) implements DeletableCodec {
    public static final Codec<LayerCodecs> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Shader.CODEC.listOf().optionalFieldOf("shaders").forGetter(LayerCodecs::shaders),
            Shader.CODEC.listOf().optionalFieldOf("modifiers").forGetter(LayerCodecs::modifiers),
            CalculationData.CODEC.listOf().optionalFieldOf("parameters").forGetter(LayerCodecs::parameters)
    ).apply(instance, LayerCodecs::new));

    public static LayerCodecs from(ShaderLayer layer) {
        List<Shader> shaders = Shader.fromList(layer.shaders);
        List<Shader> modifiers = Shader.fromList(layer.modifiers);
        List<CalculationData> calculations = CalculationData.fromList(layer.calculations);

        return new LayerCodecs(
                shaders.isEmpty() ? Optional.empty() : Optional.of(shaders),
                modifiers.isEmpty() ? Optional.empty() : Optional.of(modifiers),
                calculations.isEmpty() ? Optional.empty() : Optional.of(calculations)
        );
    }

    public void apply(ShaderLayer layer) {
        if (shaders.isPresent()) {
            loadingError = "load.error.shader";
            loadingIndex = 0;
            for (Shader shader : shaders.get()) {
                shader.apply(layer, Shaders.getMainRegistryId());
                loadingIndex++;
            }
        }
        if (modifiers.isPresent()) {
            loadingError = "load.error.modifier";
            loadingIndex = 0;
            for (Shader shader : modifiers.get()) {
                shader.apply(layer, SoupRenderer.modifierRegistry);
                loadingIndex++;
            }
        }
        if (parameters.isPresent()) {
            loadingError = "load.error.parameter";
            loadingIndex = 0;
            for (CalculationData calculationData : parameters.get()) {
                calculationData.apply(layer);
                loadingIndex++;
            }
        }
    }

    public boolean isEmpty() {
        return shaders.isEmpty() && modifiers.isEmpty() && parameters.isEmpty();
    }

    protected record Shader(String id, Optional<Map<String, List<Pass>>> passes, Boolean active) {
        public static final Codec<Shader> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Shader::id),
                Codec.unboundedMap(Codec.STRING, Pass.CODEC.listOf()).optionalFieldOf("passes").forGetter(Shader::passes),
                Codec.BOOL.optionalFieldOf("active", true).forGetter(Shader::active)
        ).apply(instance, Shader::new));

        public static List<Shader> fromList(List<ShaderData> shaderDatas) {
            List<Shader> shaders = new ArrayList<>(shaderDatas.size());
            for (ShaderData shaderData : shaderDatas) {
                shaders.add(from(shaderData));
            }
            return shaders;
        }

        public static Shader from(ShaderData shader) {
            Map<String, List<Pass>> passes = new HashMap<>(shader.chainDatas.size());
            shader.chainDatas.forEach((identifier, passData) -> {
                List<Pass> pass = Pass.from(passData);
                if (!pass.isEmpty()) {
                    passes.put(identifier == null ? "default" : identifier.toString(), pass);
                }
            });
            return new Shader(shader.shader.getShaderId().toString(), passes.isEmpty() ? Optional.empty() : Optional.of(passes), shader.active);
        }

        public void apply(ShaderLayer layer, Identifier registry) {
            // backwards compatibility
            Identifier shaderID = Identifier.parse(id);
            if (shaderID.getPath().equals("color_convolve") && shaderID.getNamespace().equals("minecraft")) {
                shaderID = Identifier.fromNamespaceAndPath("luminance", "saturate");
            }

            List<ShaderData> shaderDatas = SouperSecretSettingsClient.soupRenderer.getShaderAdditions(layer, registry, shaderID, 1, -1, false);
            if (shaderDatas == null || shaderDatas.isEmpty()) {
                sayError("shader.missing", id);
                return;
            }
            ShaderData shaderData = shaderDatas.getFirst();
            passes.ifPresent(stringListMap -> stringListMap.forEach((name, passes) -> {
                ChainData chainData = null;
                Identifier identifier = Identifier.tryParse(name);
                if (identifier != null) {
                    chainData = shaderData.chainDatas.get(name.equals("default") ? null : identifier);
                }
                if (chainData == null) {
                    sayError("shader.error.pass_id", name);
                    return;
                }

                for (int i = 0; i < passes.size(); i++) {
                    if (i >= chainData.passBlocks.size()) {
                        sayError("shader.error.pass", i, chainData.passBlocks.size()-1);
                        break;
                    }
                    passes.get(i).apply(chainData.passBlocks.get(i));
                }
            }));
            shaderData.active = active;
            layer.getList(registry).add(shaderData);
        }
    }

    protected record Pass(Optional<Map<String,List<Uniform>>> blocks, Optional<Map<String,Uniform>> uniformsOld) {
        public static final Codec<Pass> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.unboundedMap(Codec.STRING, Uniform.CODEC.listOf()).optionalFieldOf("blocks").forGetter(Pass::blocks),
                // backwards compatibility
                Codec.unboundedMap(Codec.STRING, Uniform.CODEC).optionalFieldOf("uniforms").forGetter(Pass::uniformsOld)
        ).apply(instance, Pass::new));

        public static List<Pass> from(ChainData chainData) {
            List<Pass> passes = new ArrayList<>(chainData.passBlocks.size());

            for (int passIndex = 0; passIndex < chainData.passBlocks.size(); passIndex++) {
                Map<String,List<Uniform>> blocks = getBlocks(chainData, passIndex);
                passes.add(new Pass(blocks.isEmpty() ? Optional.empty() : Optional.of(blocks), Optional.empty()));
            }

            for (int passIndex = passes.size()-1; passIndex >= 0; passIndex--) {
                Pass pass = passes.get(passIndex);
                if (pass.blocks.isEmpty()) {
                    passes.removeLast();
                } else {
                    break;
                }
            }

            return passes;
        }

        private static @NotNull Map<String, List<Uniform>> getBlocks(ChainData chainData, int passIndex) {
            Map<String, List<Uniform>> blocks = new HashMap<>();

            Map<String, BlockData> blockDataMap = chainData.passBlocks.get(passIndex);
            blockDataMap.forEach((blockName, blockData) -> {
                List<Uniform> uniforms = new ArrayList<>(blockData.uniformDatas.size());
                for (UniformData uniformData : blockData.uniformDatas) {
                    if (uniformData.isChanged()) {
                        uniforms.add(Uniform.from(uniformData.override, uniformData.config));
                    } else {
                        uniforms.add(Uniform.EMPTY);
                    }
                }
                for (int i = uniforms.size()-1; i >= 0; i--) {
                    if (uniforms.get(i) != Uniform.EMPTY) {
                        break;
                    }
                    uniforms.removeLast();
                }
                blocks.put(blockName, uniforms);
            });

            return blocks;
        }

        public void apply(Map<String,BlockData> blockDataMap) {
            // backwards compatibility
            uniformsOld.ifPresent(uniformMap -> uniformMap.forEach((uniform, uniformCodec) -> {
                // convert a uniform luminance_alpha_smooth to Alpha
                if (uniform.startsWith("luminance_")) {
                    uniform = uniform.substring(10);
                    int second = uniform.indexOf('_');
                    if (second < 0) second = uniform.length();
                    uniform = String.valueOf(uniform.charAt(0)).toUpperCase(Locale.ROOT) + uniform.substring(1, second);
                }

                for (BlockData blockData : blockDataMap.values()) {
                    for (int i = 0; i < blockData.uniformDatas.size(); i++) {
                        UniformInstance uniformInstance = blockData.block.uniforms.get(i);
                        if (uniformInstance.name.equals(uniform)) {
                            UniformData uniformData = blockData.uniformDatas.get(i);
                            uniformCodec.apply(uniformData.override, uniformData.config, true);
                            return;
                        }
                    }
                }
                sayError("shader.error.uniform_legacy", uniform, Arrays.toString(uniformCodec.values.orElse(List.of()).toArray()));
            }));

            blocks.ifPresent(blockMap -> blockMap.forEach((blockName, uniformCodecs) -> {
                BlockData blockData = blockDataMap.get(blockName);
                for (int i = 0; i < uniformCodecs.size(); i++) {
                    UniformData data = blockData.uniformDatas.get(i);
                    uniformCodecs.get(i).apply(data.override, data.config, false);
                }
            }));
        }
    }

    protected record Uniform(Optional<List<String>> values, Optional<Map<String, List<Object>>> config) {
        public static final Codec<Uniform> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.STRING.listOf().optionalFieldOf("values").forGetter(Uniform::values),
                Codec.unboundedMap(Codec.STRING, ExtraCodecs.JAVA.listOf()).optionalFieldOf("config").forGetter(Uniform::config)
        ).apply(instance, Uniform::new));

        public static Uniform from(PerValueOverride override, MapConfig config) {
            return new Uniform(Optional.of(override.getStrings()), config.config().isEmpty() ? Optional.empty() : Optional.of(config.config()));
        }

        public static Uniform EMPTY = new Uniform(Optional.empty(), Optional.empty());

        public void apply(PerValueOverride override, MapConfig config, boolean isOld) {
            if (values().isEmpty()) {
                return;
            }

            List<String> currentValues = values().get();

            for (int i = 0; i < currentValues.size(); i++) {
                if (i >= override.overrideSources.size()) {
                    sayError("shader.error.value", i, override.overrideSources.size()-1);
                    break;
                }
                String value = currentValues.get(i);
                // backwards compatibility
                if (isOld) {
                    // convert an override of luminance_alpha_smooth to luminance:alpha/smooth
                    if (value.startsWith("luminance_")) {
                        value = value.replaceFirst("_", ":").replace('_','/');
                    }
                }

                override.overrideSources.set(i, ParameterOverrideSource.parameterSourceFromString(value));
            }

            this.config.ifPresent(c -> {
                config.config().clear();
                config.config().putAll(c);
            });
        }
    }

    protected record CalculationData(String id, List<String> outputs, List<String> inputs, Boolean active) {
        public static final Codec<CalculationData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(CalculationData::id),
                Codec.STRING.listOf().fieldOf("outputs").forGetter(CalculationData::outputs),
                Codec.STRING.listOf().fieldOf("inputs").forGetter(CalculationData::inputs),
                Codec.BOOL.optionalFieldOf("active", true).forGetter(CalculationData::active)
        ).apply(instance, CalculationData::new));

        public static List<CalculationData> fromList(List<Calculation> calculations) {
            List<CalculationData> calculationDatas = new ArrayList<>(calculations.size());
            for (Calculation calculation : calculations) {
                calculationDatas.add(from(calculation));
            }
            return calculationDatas;
        }

        public static CalculationData from(Calculation calculation) {
            List<String> inputs = new ArrayList<>(calculation.inputs.length);
            for (OverrideSource overrideSource : calculation.inputs) {
                inputs.add(overrideSource.getString());
            }
            return new CalculationData(calculation.getID(), Arrays.stream(calculation.outputs).toList(), inputs, calculation.active);
        }

        public void apply(ShaderLayer layer) {
            Calculation calculation = Calculations.createCalculation(id);
            if (calculation == null) {
                sayError("parameter.missing", id);
                return;
            }

            for (int i = 0; i < outputs.size(); i++) {
                if (i >= calculation.outputs.length) {
                    sayError("parameter.error.output", i, calculation.outputs.length-1);
                    break;
                }
                calculation.outputs[i] = outputs().get(i);
            }

            for (int i = 0; i < inputs.size(); i++) {
                if (i >= calculation.inputs.length) {
                    sayError("parameter.error.input", i, calculation.inputs.length-1);
                    break;
                }
                calculation.inputs[i] = ParameterOverrideSource.parameterSourceFromString(inputs.get(i));
            }

            calculation.active = active;

            layer.calculations.add(calculation);
        }
    }

    private static int loadingIndex;
    private static String loadingError;

    private static void sayError(String key, Object... args) {
        SouperSecretSettingsClient.sayStyled(SouperSecretSettingsClient.translate(loadingError, loadingIndex).append(SouperSecretSettingsClient.translate(key, args)), 1);
    }
}
