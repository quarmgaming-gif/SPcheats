package pl.spcheats.module;

public class NumberSetting {
    public final String name;
    public final double min;
    public final double max;
    public final double step;
    public final String suffix;
    public double value;

    public NumberSetting(String name, double min, double max, double value, double step, String suffix) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.value = value;
        this.step = step;
        this.suffix = suffix;
    }

    public double ratio() {
        return (value - min) / (max - min);
    }

    public void setFromRatio(double ratio) {
        double r = Math.max(0.0, Math.min(1.0, ratio));
        double raw = min + r * (max - min);
        double stepped = Math.round(raw / step) * step;
        value = Math.max(min, Math.min(max, stepped));
    }

    public String display() {
        return Math.round(value) + suffix;
    }
}
