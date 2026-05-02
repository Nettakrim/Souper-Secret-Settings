package com.nettakrim.souper_secret_settings.shaders.custom.shader;

import com.mojang.blaze3d.shaders.ShaderType;
import com.nettakrim.souper_secret_settings.gui.custom.CreationCategory;
import com.nettakrim.souper_secret_settings.gui.custom.ShaderCategory;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import dev.dannytaylor.luminance.client.shaders.Shaders;
import net.minecraft.client.renderer.ShaderManager;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ShaderGraph extends Graph {
    @Override
    public CreationCategory getCreationRoot() {
        return new ShaderCategory();
    }

    public void compile() throws ShaderManager.CompilationException {
        OrganisedGraph organisedGraph = organise();

        AtomicInteger variables = new AtomicInteger();
        Supplier<String> uuid = () -> {
            // a, b, ..., z, aa, ab, ..., az, ba, bb, ..., zz, aaa, aab
            int i = variables.addAndGet(1);

            StringBuilder s = new StringBuilder();
            while (i > 0) {
                s.append('a'+((i-1)%26));
                i /= 26;
            }
            s.reverse();

            return s.toString();
        };

        StringBuilder shaderBuilder = new StringBuilder("#version 330\n");
        StringBuilder fragmentBuilder = new StringBuilder();
        for (OrganisedNode organisedNode : organisedGraph.organisedNodes) {
            organisedNode.calculateOutputData(uuid);

            Object mainObject = organisedNode.getMainObject();

            if (mainObject != null) {
                fragmentBuilder.append(mainObject).append(";\n");
            }
        }

        shaderBuilder.append("// dependencies\n");
        shaderBuilder.append("uniform sampler2D InSampler;\n");
        shaderBuilder.append("in vec2 texCoord;\n");
        shaderBuilder.append("out vec4 fragColor;\n");
        shaderBuilder.append("void main() {\n");
        shaderBuilder.append(fragmentBuilder);
        shaderBuilder.append("\n}");

        Shaders.registerCustomShader(graphId, ShaderType.FRAGMENT, shaderBuilder.toString());
    }
}
