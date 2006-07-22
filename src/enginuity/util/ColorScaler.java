package enginuity.util;

import enginuity.Settings;
import java.awt.*;

public final class ColorScaler {
    private static final int MAX_COLOR_INTENSITY = 255;

    private ColorScaler() {
    }

    public static Color getScaledColor(double scale, Settings settings) {
        
        Color minColor = settings.getMinColor();
        Color maxColor = settings.getMaxColor();
        
        float[] minColorHSB = new float[3];
        float[] maxColorHSB = new float[3];
        
        Color.RGBtoHSB(minColor.getRed(),
                       minColor.getGreen(),
                       minColor.getBlue(),
                       minColorHSB);
        
        Color.RGBtoHSB(maxColor.getRed(),
                       maxColor.getGreen(),
                       maxColor.getBlue(),
                       maxColorHSB);
        
        float h = minColorHSB[0] + (maxColorHSB[0] - minColorHSB[0]) * (float)scale;
        float s = minColorHSB[1] + (maxColorHSB[1] - minColorHSB[1]) * (float)scale;
        float b = minColorHSB[2] + (maxColorHSB[2] - minColorHSB[2]) * (float)scale;
        
        return Color.getHSBColor(h, s, b);

    }
}
