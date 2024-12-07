package com.nettakrim.souper_secret_settings.shaders.calculations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Calculations {
    private static final Map<String, Function<String,Calculation>> factories = new HashMap<>();

    public static void register() {
        registerCalculation("mix", MixCalculation::new);
        registerCalculation("multiply", MultiplyCalculation::new);
    }

    public static void registerCalculation(String id, Function<String,Calculation> supplier) {
        factories.put(id, supplier);
    }

    public static Calculation createCalcultion(String id) {
        if (!factories.containsKey(id)) return null;
        return factories.get(id).apply(id);
    }

    public static Collection<String> getIds() {
        return factories.keySet();
    }
}
