package enginuity.util;

import java.awt.*;

public final class ColorScaler {
    private static final int MAX_COLOR_INTENSITY = 200;

    private ColorScaler() {
    }

    public static Color getScaledColor(double scale) {
        int r = (int) ((Math.cos(Math.toRadians(180 + scale * 180)) + 1) * MAX_COLOR_INTENSITY / 2) + 30;
        int g = (int) ((Math.cos(Math.toRadians(180 + scale * 360)) + 1) * MAX_COLOR_INTENSITY / 2) + 60;
        int b = (int) ((Math.cos(Math.toRadians(scale * 180)) + 1) * MAX_COLOR_INTENSITY / 2) + 20;

        if (r > MAX_COLOR_INTENSITY) r = MAX_COLOR_INTENSITY;
        if (g > MAX_COLOR_INTENSITY) g = MAX_COLOR_INTENSITY;
        if (b > MAX_COLOR_INTENSITY) b = MAX_COLOR_INTENSITY;

        if (r < 0) r = 0;
        if (g < 0) g = 0;
        if (b < 0) b = 0;

        return new Color(r, g, b);
    }
}
