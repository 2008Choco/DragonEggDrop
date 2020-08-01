package com.ninjaguild.dragoneggdrop.utils.math;

public final class ParticleParameterContext {

    double x, z;
    double t;
    double theta;

    ParticleParameterContext(double x, double z, double t, double theta) {
        this.update(x, z, t, theta);
    }

    ParticleParameterContext() {
        this(0.0, 0.0, 0.0, 0.0);
    }

    void update(double x, double z, double t, double theta) {
        this.x = x;
        this.z = z;
        this.t = t;
        this.theta = theta;
    }

    public double get(String name, double defaultValue) {
        switch (name) {
            case "x": return x;
            case "z": return z;
            case "t": return t;
            case "theta": return theta;
            default: return defaultValue;
        }
    }

}
