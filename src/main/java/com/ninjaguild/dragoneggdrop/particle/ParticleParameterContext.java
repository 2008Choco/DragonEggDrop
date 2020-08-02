package com.ninjaguild.dragoneggdrop.particle;

public final class ParticleParameterContext {

    private double x, z;
    private double t;
    private double theta;

    public ParticleParameterContext(double x, double z, double t, double theta) {
        this.update(x, z, t, theta);
    }

    public ParticleParameterContext() {
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
