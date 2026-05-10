package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.shaders.custom.*;
import dev.dannytaylor.luminance.client.shaders.interfaces.internal.InternalUniformValueInterface;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class WriteTargetNode extends Node {
    public WriteTargetNode() {
        initialisePorts();
    }

    private TargetInputInfo swapIdentifier;

    private static final Map<String, List<UniformValue>> blitConfig;
    private static final Map<String, List<UniformValue>> mergeConfig;

    static {
        UniformValue blitUniform = new UniformValue.Vec4Uniform(new Vector4f(1f, 1f, 1f, 1f));
        ((InternalUniformValueInterface)blitUniform).luminance$setName("ColorModulate");
        blitConfig = Map.of("BlitConfig", List.of(blitUniform));

        UniformValue mergeUniform = new UniformValue.FloatUniform(1f);
        //noinspection DataFlowIssue
        InternalUniformValueInterface mergeInterface = (InternalUniformValueInterface)mergeUniform;
        mergeInterface.luminance$setName("Alpha");
        mergeInterface.luminance$setOverride(List.of("luminance:alpha/smooth"));
        mergeConfig = Map.of("MergeConfig", List.of(mergeUniform));
    }

    @Override
    protected void initialisePorts() {
        addInput("In", PortType.TARGET);
        addInput("Target", PortType.STRING).setDock(new StringNode("main"));
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) {
        Identifier identifier = Identifier.parse((String)organisedNode.getInputData(1));

        if (identifier.equals(Identifier.withDefaultNamespace("main"))) {
            swapIdentifier = new TargetInputInfo(uuid.get());
        } else {
            swapIdentifier = null;
        }
    }

    @Override
    public @Nullable Object getMainObject(Graph.OrganisedNode organisedNode) {
        // insert luminance merge
        if (swapIdentifier != null) {
            return List.of(
                    new PostChainConfig.Pass(
                            Identifier.parse("core/screenquad"),
                            Identifier.parse("luminance:post/merge"),
                            List.of(
                                    ((InputInfo)organisedNode.getInputData(0)).getInput("In"),
                                    new PostChainConfig.TargetInput("Merge", Identifier.withDefaultNamespace("main"), false, false)
                            ),
                            swapIdentifier.targetId,
                            mergeConfig
                    ),
                    new PostChainConfig.Pass(
                            Identifier.parse("core/screenquad"),
                            Identifier.parse("post/blit"),
                            List.of(swapIdentifier.getInput("In")),
                            Identifier.withDefaultNamespace("main"),
                            blitConfig
                    )
            );
        }

        // otherwise blit, which will sometimes waste a pass, but other times will be needed
        return List.of(new PostChainConfig.Pass(
                Identifier.parse("core/screenquad"),
                Identifier.parse("post/blit"),
                List.of(((InputInfo)organisedNode.getInputData(0)).getInput("In")),
                Identifier.parse((String)organisedNode.getInputData(1)),
                blitConfig
        ));
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Write Target");
    }

    @Override
    public RootType rootType(HashMap<InputPort, Wire> wires) {
        // only count as a main output if the output is main
        InputPort port = inputPorts.get(1);
        Node inputSource = port.docked;

        Wire wire = wires.get(port);
        if (wire != null) {
            inputSource = wire.source.node;
        }

        if (inputSource instanceof StringNode stringNode) {
            Identifier identifier = Identifier.parse(stringNode.value);
            if (identifier.equals(Identifier.withDefaultNamespace("main"))) {
                return RootType.MAIN;
            }
        }

        return RootType.ALTERNATE;
    }

}
