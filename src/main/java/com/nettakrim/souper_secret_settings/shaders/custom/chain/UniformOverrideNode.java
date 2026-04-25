package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.mclegoman.luminance.client.shaders.interfaces.internal.InternalUniformValueInterface;
import com.mclegoman.luminance.client.shaders.overrides.PerValueOverride;
import com.mclegoman.luminance.client.shaders.overrides.UniformOverride;
import com.mclegoman.luminance.client.shaders.uniforms.config.UniformConfig;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.ValueNode;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UniformOverrideNode extends ValueNode {
    List<Number> template;

    public UniformOverrideNode(List<Number> template, UniformOverride override, UniformConfig config) {
        this.template = template;
        initialisePorts();

        List<String> strings;
        if (override instanceof PerValueOverride perValueOverride) {
            strings = perValueOverride.getStrings();
        }
        else {
            strings = new ArrayList<>(template.size());
            for (Number number : template) {
                strings.add(number.toString());
            }
        }

        for (int i = 0; i < template.size(); i++) {
            inputPorts.get(i).docked = new StringNode(strings.get(i));
        }
    }

    @Override
    protected void initialisePorts() {
        super.initialisePorts();

        // TODO: config, probably through another port type
        for (int i = 0; i < template.size(); i++) {
            addInput("value "+i, PortType.STRING);
        }
    }

    @Override
    protected PortType getType() {
        return null;
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        UniformValue uniformValue = UniformValueNode.getUniformValue(template);

        InternalUniformValueInterface uniformValueInterface = (InternalUniformValueInterface)uniformValue;
        assert uniformValueInterface != null;

        List<String> values = new ArrayList<>(template.size());
        for (Graph.Source source : organisedNode.inputSources) {
            values.add((String) source.getPort().outputData);
        }

        uniformValueInterface.luminance$setOverride(values);
        uniformValueInterface.luminance$setConfig(null);

        outputPorts.getFirst().outputData = uniformValue;
    }

    @Override
    protected Component getTitle() {
        return Component.literal("Uniform");
    }
}
