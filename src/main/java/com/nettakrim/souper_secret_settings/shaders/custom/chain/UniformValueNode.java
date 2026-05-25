package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.google.common.collect.ImmutableList;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.DraggableEditBoxWidget;
import com.nettakrim.souper_secret_settings.gui.ParameterTextWidget;
import com.nettakrim.souper_secret_settings.shaders.custom.GraphCompilationException;
import dev.dannytaylor.luminance.client.shaders.IVec2Uniform;
import dev.dannytaylor.luminance.client.shaders.IVec4Uniform;
import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import com.nettakrim.souper_secret_settings.shaders.custom.PortType;
import com.nettakrim.souper_secret_settings.shaders.custom.ValueNode;
import dev.dannytaylor.luminance.client.shaders.UniformInstance;
import dev.dannytaylor.luminance.client.shaders.interfaces.internal.InternalUniformValueInterface;
import dev.dannytaylor.luminance.client.shaders.overrides.PerValueOverride;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class UniformValueNode extends ValueNode {
    private final ImmutableList<Number> template;
    private final List<Object> values;

    UniformValueNode(UniformInstance uniformInstance) {
        this.template = uniformInstance.defaultValue;
        values = new ArrayList<>(template.size());

        // TODO: config
        List<String> strings;
        if (uniformInstance.override instanceof PerValueOverride perValueOverride) {
            strings = perValueOverride.getStrings();
        }
        else {
            strings = new ArrayList<>(template.size());
            for (Number number : template) {
                strings.add(number.toString());
            }
        }

        for (int i = 0; i < template.size(); i++) {
            String string = strings.get(i);

            // disable alpha, since chain graphs add it automatically
            if (string.equals("luminance:alpha/smooth")) {
                string = "1.0";
            }

            values.add(null);
            onSetValue(i, string);
        }

        initialisePorts();
    }

    @Override
    protected PortType getType() {
        return PortType.UNIFORM_VALUE;
    }

    @Override
    protected List<String> getValues() {
        return values.stream().map(Objects::toString).toList();
    }

    @Override
    protected void onSetValue(int index, String value) {
        try {
            float f = Float.parseFloat(value);
            boolean isInt = template.get(index) instanceof Integer;
            values.set(index, isInt ? Math.round(f) : f);
        } catch (Exception ignored) {
            values.set(index, value);
        }
    }

    @Override
    public void putOutputData(Graph.OrganisedNode organisedNode, Supplier<String> uuid) throws GraphCompilationException {
        UniformValue uniformValue = getUniformValue();

        // add overrides only if needed
        if (values.stream().anyMatch(object -> object instanceof String)) {
            List<String> strings = new ArrayList<>(template.size());
            for (Object object : values) {
                strings.add(object.toString());
            }

            ((InternalUniformValueInterface)uniformValue).luminance$setOverride(strings);
        }

        outputPorts.getFirst().outputData = uniformValue;
    }

    @Override
    protected @NotNull Component getTitle() {
        return Component.literal("Uniform");
    }

    private @NotNull UniformValue getUniformValue() throws GraphCompilationException {
        int size = template.size();
        boolean isInt = template.getFirst() instanceof Integer;

        if (size == 1) {
            return isInt ? new UniformValue.IntUniform(getInt(0))
                         : new UniformValue.FloatUniform(getFloat(0));
        }
        else if (size == 2) {
            return isInt ? new              IVec2Uniform(new Vector2i(getInt(0), getInt(1)))
                         : new UniformValue.Vec2Uniform(new Vector2f(getFloat(0), getFloat(1)));
        }
        else if (size == 3) {
            return isInt ? new UniformValue.IVec3Uniform(new Vector3i(getInt(0), getInt(1), getInt(2)))
                         : new UniformValue.Vec3Uniform(new Vector3f(getFloat(0), getFloat(1), getFloat(2)));
        }
        else if (size == 4) {
            return isInt ? new              IVec4Uniform(new Vector4i(getInt(0), getInt(1), getInt(2), getInt(3)))
                         : new UniformValue.Vec4Uniform(new Vector4f(getFloat(0), getFloat(1), getFloat(2), getFloat(3)));
        } else {
            throw new GraphCompilationException("Invalid Uniform Size", this);
        }
    }

    private int getInt(int index) {
        return getNumber(index).intValue();
    }

    private float getFloat(int index) {
        return getNumber(index).floatValue();
    }

    private Number getNumber(int index) {
        if (values.get(index) instanceof Number number) {
            return number;
        }
        return template.get(index);
    }

    @Override
    protected EditBox createWidget(String value) {
        DraggableEditBoxWidget widget = new ParameterTextWidget(1, 1, widgetHeight, Component.empty(), SouperSecretSettingsClient.soupRenderer.activeLayer, value);
        widget.setValue(value);
        return widget;
    }
}
