package com.nettakrim.souper_secret_settings.shaders.custom;

import java.util.Objects;
import java.util.function.BiPredicate;

public enum PortType {
    TARGET(0xFF00FFFF, Objects::equals),
    STRING(0xFF8000FF, Objects::equals),
    BLOCK(0xFFFF0000, Objects::equals),
    UNIFORM_VALUE(0xFFFF8000, Objects::equals);

    public final int color; // color for line rendering
    public final BiPredicate<PortType, PortType> canConnect; // connection rule, lhs is the source type and rhs is the destination type

    PortType(int color, BiPredicate<PortType, PortType> canConnect) {
        this.color = color;
        this.canConnect = canConnect;
    }
}
