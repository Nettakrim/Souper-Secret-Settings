package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import dev.dannytaylor.luminance.client.shaders.interfaces.internal.InternalUniformValueInterface;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.Node;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class WriteTargetNode extends Node {
    public WriteTargetNode() {
        initialisePorts();
    }

    @Override
    protected void initialisePorts() {
        addInput("input", PortType.TARGET);
        addInput("target", PortType.STRING).docked = new StringNode("minecraft:main");
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {

    }

    @Override
    public boolean isEnd() {
        return true;
    }

    public PostChainConfig.Pass getPass(Graph.OrganisedNode organisedNode) {
        UniformValue uniformValue = new UniformValue.Vec4Uniform(new Vector4f(1f, 1f, 1f, 1f));
        ((InternalUniformValueInterface)uniformValue).luminance$setName("ColorModulate");

        return new PostChainConfig.Pass(
                Identifier.parse("core/screenquad"),
                Identifier.parse("post/blit"),
                List.of(new PostChainConfig.TargetInput("In", Identifier.parse((String)organisedNode.inputSources[0].getPort().outputData), false, false)),
                Identifier.parse((String)organisedNode.inputSources[1].getPort().outputData),
                Map.of("BlitConfig", List.of(uniformValue))
        );
    }

    @Override
    protected Component getTitle() {
        return Component.literal("Write Target");
    }
}
