package com.nettakrim.souper_secret_settings.shaders.custom;

import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;

import java.util.Objects;
import java.util.function.BiPredicate;

public enum PortType {
    TARGET        (0x00FFFF, Objects::equals),
    STRING        (0xA050FF, Objects::equals),
    BLOCK         (0xFF0000, Objects::equals),
    UNIFORM_VALUE (0xFF8000, Objects::equals),
    VEC1          (0xC0E050, PortType::vectorCoercion),
    VEC2          (0x80E050, PortType::vectorCoercion),
    VEC3          (0x20D060, PortType::vectorCoercion),
    VEC4          (0x30D0A0, PortType::vectorCoercion);

    public final int color; // color for line rendering
    public final BiPredicate<PortType, PortType> canConnect; // connection rule, lhs is the source type and rhs is the destination type

    PortType(int color, BiPredicate<PortType, PortType> canConnect) {
        this.color = 0xFF000000 | color;
        this.canConnect = canConnect;
    }

    private static boolean vectorCoercion(PortType src, PortType dst) {
        return (src == VEC1 || src == VEC2 || src == VEC3 || src == VEC4) &&
               (dst == VEC1 || dst == VEC2 || dst == VEC3 || dst == VEC4);
    }

    public static String getVector(String srcUUID, PortType src, PortType dst) {
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
