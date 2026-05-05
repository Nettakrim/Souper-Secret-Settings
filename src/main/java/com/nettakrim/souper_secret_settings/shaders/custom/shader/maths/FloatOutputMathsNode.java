package com.nettakrim.souper_secret_settings.shaders.custom.shader.maths;

import com.nettakrim.souper_secret_settings.gui.custom.CreationNode;
import com.nettakrim.souper_secret_settings.shaders.custom.*;
import net.minecraft.network.chat.Component;

import java.util.HashMap;

public class FloatOutputMathsNode extends DynamicOutputMathsNode {
    public static CreationNode get(Component name, String format, String... inputs) {
        return new CreationNode(name, () -> new FloatOutputMathsNode(format, name, inputs));
    }

    public FloatOutputMathsNode(String format, Component name, String... inputs) {
        super(format, name, inputs);
    }

    @Override
    public void updateConnections(HashMap<InputPort, Wire> wires) {
        dynamicType = setInputsToWidestType(wires);
        outputPorts.getFirst().setPortType(PortType.VEC1, wires);
    }
}
