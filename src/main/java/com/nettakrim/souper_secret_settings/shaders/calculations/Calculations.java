package com.nettakrim.souper_secret_settings.shaders.calculations;

import com.nettakrim.souper_secret_settings.shaders.calculations.logic.CompareCalculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.logic.NearCalculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.mix.LinearCalculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.mix.MultiplyCalculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.oscillator.SawCalculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.oscillator.SineCalculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.oscillator.SquareCalculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.oscillator.TriangleCalculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.remap.ClampCalculation;
import com.nettakrim.souper_secret_settings.shaders.calculations.remap.LimitCalculation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Calculations {
    private static final Map<String, Function<String, Calculation>> factories = new HashMap<>();

    public static void register() {
        registerCalculation("logic_compare", CompareCalculation::new);
        registerCalculation("logic_near", NearCalculation::new);

        registerCalculation("mix_linear", LinearCalculation::new);
        registerCalculation("mix_multiply", MultiplyCalculation::new);

        registerCalculation("oscillator_saw", SawCalculation::new);
        registerCalculation("oscillator_sine", SineCalculation::new);
        registerCalculation("oscillator_square", SquareCalculation::new);
        registerCalculation("oscillator_triangle", TriangleCalculation::new);

        registerCalculation("remap_clamp", ClampCalculation::new);
        registerCalculation("remap_limit", LimitCalculation::new);
    }

    public static void registerCalculation(String id, Function<String, Calculation> supplier) {
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
