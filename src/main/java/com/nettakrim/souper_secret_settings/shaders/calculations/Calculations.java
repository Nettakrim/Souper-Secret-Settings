package com.nettakrim.souper_secret_settings.shaders.calculations;

import com.nettakrim.souper_secret_settings.shaders.ShaderStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Calculations {
    private static final Map<String, BiFunction<String, ShaderStack, Calculation>> factories = new HashMap<>();

    public static void register() {
        registerCalculation("mix", MixCalculation::new);
        registerCalculation("multiply", MultiplyCalculation::new);
    }

    public static void registerCalculation(String id, BiFunction<String, ShaderStack, Calculation> supplier) {
        factories.put(id, supplier);
    }

    public static Calculation createCalcultion(String id, ShaderStack stack) {
        if (!factories.containsKey(id)) return null;
        return factories.get(id).apply(id, stack);
    }

    public static Collection<String> getIds() {
        return factories.keySet();
    }
}
