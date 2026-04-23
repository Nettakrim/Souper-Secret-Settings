package com.nettakrim.souper_secret_settings.shaders.custom;

import java.util.Objects;
import java.util.function.BiPredicate;

public enum PortType {
    TARGET(-1, Objects::equals),
    STRING(-1, Objects::equals),
    BLOCK(-1, Objects::equals),
    UNIFORM_VALUE(-1, Objects::equals);

    public final int color; // color for line rendering
    public final BiPredicate<PortType, PortType> canConnect; // connection rule, lhs is the source type and rhs is the destination type

    PortType(int color, BiPredicate<PortType, PortType> canConnect) {
        this.color = color;
        this.canConnect = canConnect;
    }
}
