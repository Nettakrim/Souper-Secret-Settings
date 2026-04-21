package com.nettakrim.souper_secret_settings.shaders.custom;

public interface PortType {
    int color();

    class TargetPort implements PortType {
        @Override
        public int color() {
            return -1;
        }
    }

    class StringPort implements PortType {
        @Override
        public int color() {
            return -1;
        }
    }
}
