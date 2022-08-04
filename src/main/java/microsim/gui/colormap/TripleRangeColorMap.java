package microsim.gui.colormap;

import java.awt.Color;

/**
 * It builds automatically a color map oscillating from a bottom color to a middle one and from the middle to a top one,
 * on a variable range.
 */
public class TripleRangeColorMap extends FixedColorMap {

    private final double rangeSize;

    public TripleRangeColorMap(int gradients, Color bottomColor, Color middleColor, Color topColor,
                               double minValue, double midValue, double maxValue) {
        super(gradients);

        if (maxValue <= midValue || midValue <= minValue)
            throw new ArrayIndexOutOfBoundsException("ColorTripleRangeMap: range parameters are not corrected.");

        int redStart = bottomColor.getRed();
        int blueStart = bottomColor.getBlue();
        int greenStart = bottomColor.getGreen();

        int redMiddle = middleColor.getRed();
        int blueMiddle = middleColor.getBlue();
        int greenMiddle = middleColor.getGreen();

        int redEnd = topColor.getRed();
        int blueEnd = topColor.getBlue();
        int greenEnd = topColor.getGreen();

        rangeSize = (maxValue - minValue) / gradients;

        int lowGradients = (int) ((midValue - minValue) / (maxValue - minValue) * gradients);

        for (int i = 0; i < lowGradients; i++) {
            int[] c = new int[]{0, 0, 0};
            c[0] = redStart + ((redMiddle - redStart) * i / gradients);
            c[1] = greenStart + ((greenMiddle - greenStart) * i / gradients);
            c[2] = blueStart + ((blueMiddle - blueStart) * i / gradients);

            addColor(i, new Color(c[0], c[1], c[2]));
        }

        for (int i = lowGradients; i < gradients; i++) {
            int[] c = new int[]{0, 0, 0};
            c[0] = redMiddle + ((redEnd - redMiddle) * i / gradients);
            c[1] = greenMiddle + ((greenEnd - greenMiddle) * i / gradients);
            c[2] = blueMiddle + ((blueEnd - blueMiddle) * i / gradients);

            addColor(i, new Color(c[0], c[1], c[2]));
        }
    }

    public int getColorIndex(double value) {
        int i = (int) (value * rangeSize);
        return i < 0 ? 0 : (i >= colorList.length ? colorList.length - 1 : i);
    }
}
