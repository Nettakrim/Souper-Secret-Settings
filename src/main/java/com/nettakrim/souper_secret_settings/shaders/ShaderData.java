package com.nettakrim.souper_secret_settings.shaders;

import com.mclegoman.luminance.client.shaders.Shader;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.mclegoman.luminance.client.shaders.interfaces.PostEffectPassInterface;
import com.mclegoman.luminance.client.shaders.interfaces.ShaderProgramInterface;
import com.mclegoman.luminance.client.shaders.overrides.LuminanceUniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import com.mclegoman.luminance.client.shaders.overrides.UniformSource;
import com.mclegoman.luminance.client.shaders.uniforms.Uniform;
import com.mclegoman.luminance.mixin.client.shaders.PostEffectProcessorAccessor;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.util.ObjectAllocator;

import java.util.*;

public class ShaderData {
    public Shader shader;

    public final List<Map<String, UniformOverride>> overrides;

    public ShaderData(Shader shader) {
        this.shader = shader;
        this.shader.setPostProcessor();

        List<PostEffectPass> passes = ((PostEffectProcessorAccessor)this.shader.getPostProcessor()).getPasses();
        this.overrides = new ArrayList<>(passes.size());

        for (PostEffectPass pass : passes) {
            Map<String, UniformOverride> override = new HashMap<>(0);

            ShaderProgram program = ((PostEffectPassInterface)pass).luminance$getProgram();
            for (String name : ((ShaderProgramInterface)program).luminance$getUniformNames()) {
                defaultOverride(pass, program.getUniform(name), name, override);
            }

            this.overrides.add(override);
        }
    }

    private void defaultOverride(PostEffectPass pass, GlUniform uniform, String name, Map<String, UniformOverride> overrideMap) {
        if (uniform == null || uniform.getCount() != 1 || !allowUniform(name)) {
            return;
        }

        Uniform override = null;
        if (((PostEffectPassInterface)pass).luminance$getUniformOverride(name) instanceof LuminanceUniformOverride o && o.overrideSources.getFirst() instanceof UniformSource uniformSource) {
            override = uniformSource.getUniform();
        }
        if (override == null && LuminanceUniformOverride.sourceFromString(name) instanceof UniformSource uniformSource) {
            override = uniformSource.getUniform();
        }
        if (override == null) return;

        float a = 0;
        float b = 1;
        Optional<Float> min = override.getMin();
        Optional<Float> max = override.getMax();
        if (min.isPresent() && max.isPresent()) {
            a = min.get();
            b = max.get();
        }

        List<String> list = new ArrayList<>();
        list.add(null);
        LuminanceUniformOverride uniformOverride = new LuminanceUniformOverride(list);
        uniformOverride.overrideSources.set(0, new MixOverrideSource(a, b, ParameterOverrideSource.parameterSourceFromString(name)));

        overrideMap.put(name,uniformOverride);
    }

    public void render(Framebuffer framebuffer, ObjectAllocator objectAllocator) {
        OverrideManager.applyOverrides(this);
        Shaders.renderUsingAllocator(shader.getPostProcessor(), framebuffer, objectAllocator);
        OverrideManager.resetOverrides();
    }

    public static final List<String> uniformsToIgnore = List.of("ProjMat", "InSize", "OutSize");
    public static boolean allowUniform(String uniform) {
        return !uniformsToIgnore.contains(uniform);
    }
}
