package pl.spcheats.module;

public class NumberSetting {
    public final String name;
    public final double min;
    public final double max;
    public final double step;
    public final String suffix;
    public final boolean bool;
    public double value;

    public NumberSetting(String name, double min, double max, double value, double step, String suffix) {
        this(name, min, max, value, step, suffix, false);
    }

    private NumberSetting(String name, double min, double max, double value, double step, String suffix, boolean bool) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.value = value;
        this.step = step;
        this.suffix = suffix;
        this.bool = bool;
    }

    public static NumberSetting bool(String name, boolean defaultValue) {
        return new NumberSetting(name, 0, 1, defaultValue ? 1 : 0, 1, "", true);
    }

    public boolean isOn() {
        return value >= 0.5;
    }

    public void toggle() {
        value = isOn() ? 0 : 1;
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

    public void setClamped(double v) {
        value = Math.max(min, Math.min(max, v));
    }

    public String display() {
        if (bool) {
            return isOn() ? "Tak" : "Nie";
        }
        if (step < 1.0) {
            return String.format("%.1f%s", value, suffix);
        }
        return Math.round(value) + suffix;
    }
}
