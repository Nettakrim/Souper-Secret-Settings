package com.nettakrim.souper_secret_settings.shaders.custom;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import java.util.Objects;
import java.util.function.BiPredicate;

public enum PortType {
    UNUSED        (0x000000, PortType::vectorCoercion, "ERROR"), // use for inputs that are fine to be empty
    TARGET        (0x00FFFF, Objects::equals, "ERROR"),
    STRING        (0xA050FF, Objects::equals, "ERROR"),
    BLOCK         (0xFF6000, Objects::equals, "ERROR"),
    UNIFORM_VALUE (0xFFB000, Objects::equals, "ERROR"),
    VECN          (0xFFFFFF, PortType::vectorCoercion, "ERROR"),
    VEC1          (0xC0E050, PortType::vectorCoercion, "float"),
    VEC2          (0x80E050, PortType::vectorCoercion, "vec2"),
    VEC3          (0x20D060, PortType::vectorCoercion, "vec3"),
    VEC4          (0x30D0A0, PortType::vectorCoercion, "vec4");

    public final int color; // color for line rendering
    public final BiPredicate<PortType, PortType> canConnect; // connection rule, lhs is the source type and rhs is the destination type
    public final String glType;

    PortType(int color, BiPredicate<PortType, PortType> canConnect, String glType) {
        this.color = 0xFF000000 | color;
        this.canConnect = canConnect;
        this.glType = glType;
    }

    private static boolean vectorCoercion(PortType src, PortType dst) {
        return (src == VEC1 || src == VEC2 || src == VEC3 || src == VEC4 || src == VECN || src == UNUSED) &&
               (dst == VEC1 || dst == VEC2 || dst == VEC3 || dst == VEC4 || dst == VECN || dst == UNUSED);
    }

    public static String getVector(String srcUUID, PortType src, PortType dst) {
        assert src != VECN;
        assert dst != VECN;
        assert src != UNUSED;
        assert dst != UNUSED;

        // type matches
        if (src == dst) {
            return srcUUID;
        }

        if (src.ordinal() > dst.ordinal()) {
            // type narrowing
            if (dst == VEC1) {
                return srcUUID+".x";
            }
            if (dst == VEC2) {
                return srcUUID+".xy";
            }
            if (dst == VEC3) {
                return srcUUID+".xyz";
            }
        } else {
            // type widening
            String zeros = (src == VEC1 ? ","+srcUUID : ",0.0").repeat(dst.ordinal()-src.ordinal());
            if (dst == VEC4) {
                return "vec4("+srcUUID+zeros+")";
            }
            if (dst == VEC3) {
                return "vec3("+srcUUID+zeros+")";
            }
            if (dst == VEC2) {
                return "vec2("+srcUUID+zeros+")";
            }
        }

        // this should never happen
        SouperSecretSettingsClient.log("vector coercion failed for uuid",srcUUID,"with source type",src,"and destination type",dst);
        return srcUUID;
    }
}
