package com.nettakrim.souper_secret_settings.shaders.custom.shader;

import com.mojang.blaze3d.shaders.ShaderType;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.custom.CreationCategory;
import com.nettakrim.souper_secret_settings.gui.custom.ShaderCategory;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import dev.dannytaylor.luminance.client.shaders.Shaders;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ShaderGraph extends Graph<Identifier> {
    @Override
    public CreationCategory getCreationRoot() {
        return new ShaderCategory();
    }

    @Override
    protected String getType() {
        return "shader";
    }

    @Override
    protected Identifier compile() throws ShaderManager.CompilationException {
        long start = System.nanoTime();
        OrganisedGraph organisedGraph = organise();
        long organised = System.nanoTime();

        AtomicInteger variables = new AtomicInteger();
        Supplier<String> uuid = () -> {
            // a, b, ..., z, aa, ab, ..., az, ba, bb, ..., zz, aaa, aab
            int i = variables.addAndGet(1);

            StringBuilder s = new StringBuilder();
            while (i > 0) {
                s.append((char)('a'+((i-1)%26)));
                i /= 26;
            }
            s.reverse();

            return s.toString();
        };

        HashMap<String, String> functions = new HashMap<>();
        StringBuilder fragmentBuilder = new StringBuilder();
        for (OrganisedNode organisedNode : organisedGraph.organisedNodes) {
            organisedNode.calculateOutputData(uuid);

            Object mainObject = organisedNode.getMainObject();

            if (mainObject != null) {
                fragmentBuilder.append(mainObject).append(";\n");
            }

            if (organisedNode.node instanceof FunctionDependency functionDependency) {
                functions.putIfAbsent(functionDependency.functionName(), functionDependency.functionImplementation());
            }
        }

        StringBuilder shaderBuilder = new StringBuilder("#version 330\n");
        shaderBuilder.append("uniform sampler2D InSampler;\n");
        shaderBuilder.append("in vec2 texCoord;\n");
        shaderBuilder.append("out vec4 fragColor;\n\n");

        for (String functionBody : functions.values()) {
            shaderBuilder.append(functionBody).append("\n");
        }

        shaderBuilder.append("void main() {\n");
        shaderBuilder.append(fragmentBuilder);
        shaderBuilder.append("}");

        String shader = shaderBuilder.toString();
        Shaders.registerCustomShader(graphId, ShaderType.FRAGMENT, shader);

        long constructed = System.nanoTime();
        SouperSecretSettingsClient.log("Compiled shader in",getElapsedTime(start, constructed),"- Organising:",getElapsedTime(start,organised),"| Constructing:",getElapsedTime(organised,constructed));
        //SouperSecretSettingsClient.log(shader);
        return graphId;
    }

    @Override
    public void makeChange() {
        super.makeChange();
        getOrCompile();
    }
}
