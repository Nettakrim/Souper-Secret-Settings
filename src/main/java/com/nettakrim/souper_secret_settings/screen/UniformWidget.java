package com.nettakrim.souper_secret_settings.screen;

import net.minecraft.client.gl.GlUniform;
import net.minecraft.text.Text;

public class UniformWidget extends ParameterWidget {
    public GlUniform uniform;

    public UniformWidget(GlUniform uniform, Text name, int x, int width, CollapseScreen collapseScreen) {
        super(uniform.getCount(), name, x, width, collapseScreen);
        this.uniform = uniform;
        initValues(collapseScreen);
    }

    @Override
    String[] getValues() {
        String[] values = new String[uniform.getCount()];

        if (uniform.getDataType() <= 3) {
            int[] arr = new int[uniform.getCount()];
            uniform.getIntData().position(0);
            uniform.getIntData().get(arr);
            for (int i = 0; i < arr.length; i++) {
                values[i] = Integer.toString(arr[i]);
            }
        } else {
            float[] arr = new float[uniform.getCount()];
            uniform.getFloatData().position(0);
            uniform.getFloatData().get(arr);
            for (int i = 0; i < arr.length; i++) {
                values[i] = Float.toString(arr[i]);
            }
        }

        return values;
    }

    @Override
    protected void onValueChanged(int i, String s) {
        super.onValueChanged(i, s);
        updateUniform();
    }

    protected void updateUniform() {
        float[] arr = new float[uniform.getCount()];
        for (int i = 0; i < arr.length; i++) {
            try {
                arr[i] = Float.parseFloat(values[i]);
            } catch (Exception ignored) {}
        }

        if (uniform.getDataType() <= 3) {
            switch (arr.length) {
                case 1: uniform.set(Math.round(arr[0]));
                case 2: uniform.set(Math.round(arr[0]), Math.round(arr[1]));
                case 3: uniform.set(Math.round(arr[0]), Math.round(arr[1]), Math.round(arr[2]));
                case 4: uniform.set(Math.round(arr[0]), Math.round(arr[1]), Math.round(arr[2]), Math.round(arr[3]));
            }
        } else {
            uniform.set(arr);
        }
    }
}
