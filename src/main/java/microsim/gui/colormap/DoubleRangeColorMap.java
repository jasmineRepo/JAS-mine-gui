package microsim.gui.colormap;

import java.awt.Color;

/**
 * It builds automatically a color map varying between two colors on a variable range.
 */
public class DoubleRangeColorMap extends FixedColorMap {
    private final double rangeSize;

    public DoubleRangeColorMap(int gradients, Color bottomColor, Color topColor, double minValue, double maxValue) {
        super(gradients);

        if (maxValue <= minValue)
            throw new ArrayIndexOutOfBoundsException("ColorDualRangeMap: range parameters are not corrected.");

        int redStart = bottomColor.getRed();
        int blueStart = bottomColor.getBlue();
        int greenStart = bottomColor.getGreen();

        int redEnd = topColor.getRed();
        int blueEnd = topColor.getBlue();
        int greenEnd = topColor.getGreen();

        rangeSize = (maxValue - minValue) / gradients;

        for (int i = 0; i < gradients; i++) {
            int[] c = new int[]{0, 0, 0};
            c[0] = redStart + ((redEnd - redStart) * i / gradients);
            c[1] = greenStart + ((greenEnd - redStart) * i / gradients);
            c[2] = blueStart + ((blueEnd - redStart) * i / gradients);

            addColor(i, new Color(c[0], c[1], c[2]));
        }
    }

    public int getColorIndex(double value) {
        int i = (int) (value * rangeSize);
        return i < 0 ? 0 : (i >= colorList.length ? colorList.length - 1 : i);
    }
}
