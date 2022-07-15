package microsim.gui.colormap;

import java.awt.Color;

/**
 * It builds automatically a color map using a variable range.<br>
 * There are three types of range:<br>
 * <item>From black color to a given color, based on a linear range.
 * <item>From given color to a given color, based on a linear range.
 * <item>From given color to a given color, based on dual range, with a middle color.
 */
public class RangeColorMap extends FixedColorMap {
    protected double rangeSize;
    protected double minValue;

    /**
     * Create a color range map from black to given color.
     *
     * @param gradients The number of color gradients that are added to the map.
     * @param minValue  The lower bound of the range.
     * @param maxValue  The upper bound of the range.
     * @param color     The highest color. It will correspond to maxValue.
     * @throws ArrayIndexOutOfBoundsException if maxValue <= minValue.
     */
    public RangeColorMap(int gradients, double minValue, double maxValue, Color color) {
        super(gradients);

        if (maxValue <= minValue)
            throw new ArrayIndexOutOfBoundsException("ColorRangeMap: range parameters are not corrected.");

        this.rangeSize = gradients / (maxValue - minValue);
        this.minValue = minValue;

        double redGap = (double) color.getRed() / (double) gradients;
        double greenGap = (double) color.getGreen() / (double) gradients;
        double blueGap = (double) color.getBlue() / (double) gradients;

        for (int i = 0; i < gradients; i++)
            addColor(i, new Color(getBoundedCol(redGap * i),
                    getBoundedCol(greenGap * i), getBoundedCol(blueGap * i)));

    }

    /**
     * Create a color range map from given color to given color.
     *
     * @param gradients   The number of color gradients that are added to the map.
     * @param minValue    The lower bound of the range.
     * @param maxValue    The upper bound of the range.
     * @param bottomColor The lowest color. It will correspond to minValue.
     * @param topColor    The highest color. It will correspond to maxValue.
     * @throws ArrayIndexOutOfBoundsException if maxValue <= minValue.
     */
    public RangeColorMap(int gradients, double minValue, double maxValue, Color bottomColor, Color topColor) {
        super(gradients);

        if (maxValue <= minValue)
            throw new ArrayIndexOutOfBoundsException("ColorRangeMap: range parameters are not corrected.");

        this.minValue = minValue;
        int redStart, blueStart, greenStart;
        int redGap, blueGap, greenGap;

        redStart = bottomColor.getRed();
        blueStart = bottomColor.getBlue();
        greenStart = bottomColor.getGreen();

        redGap = topColor.getRed() - redStart;
        blueGap = topColor.getBlue() - blueStart;
        greenGap = topColor.getGreen() - greenStart;

        rangeSize = (double) gradients / (maxValue - minValue);

        int[] c = new int[]{0, 0, 0};
        for (int i = 0; i < gradients; i++) {
            double delta = (double) i / (double) gradients;
            c[0] = getBoundedCol(redStart + (int) (redGap * delta));
            c[1] = getBoundedCol(greenStart + (int) (greenGap * delta));
            c[2] = getBoundedCol(blueStart + (int) (blueGap * delta));

            addColor(i, new Color(c[0], c[1], c[2]));
        }
    }

    /**
     * Create a color range map from lower given color to middle given color and from middle to the highest given one.
     *
     * @param gradients   The number of color gradients that are added to the map.
     * @param minValue    The lower bound of the range.
     * @param midValue    The value at which color changes the range.
     * @param maxValue    The upper bound of the range.
     * @param bottomColor The lowest color. It will correspond to minValue.
     * @param middleColor The middle color. It will correspond to midValue.
     * @param topColor    The highest color. It will correspond to maxValue.
     * @throws ArrayIndexOutOfBoundsException if maxValue <= midValue || midValue <= minValue.
     */
    public RangeColorMap(int gradients, double minValue, double midValue, double maxValue, Color bottomColor,
                         Color middleColor, Color topColor) {
        super(gradients);

        if (maxValue <= midValue || midValue <= minValue)
            throw new ArrayIndexOutOfBoundsException("ColorRangeMap: range parameters are not corrected.");

        int redStart, blueStart, greenStart;
        int redGap, blueGap, greenGap;
        int[] c = new int[]{0, 0, 0};

        this.minValue = minValue;

        rangeSize = (double) gradients / (maxValue - minValue);
        // LOW PART
        int lowGradients = (int) (gradients * ((midValue - minValue) / (maxValue - minValue)));

        redStart = bottomColor.getRed();
        blueStart = bottomColor.getBlue();
        greenStart = bottomColor.getGreen();

        redGap = middleColor.getRed() - redStart;
        blueGap = middleColor.getBlue() - blueStart;
        greenGap = middleColor.getGreen() - greenStart;

        for (int i = 0; i < lowGradients; i++) {
            double delta = (double) i / (double) lowGradients;
            c[0] = getBoundedCol(redStart + (int) (redGap * delta));
            c[1] = getBoundedCol(greenStart + (int) (greenGap * delta));
            c[2] = getBoundedCol(blueStart + (int) (blueGap * delta));

            addColor(i, new Color(c[0], c[1], c[2]));
        }

        // HIGH PART
        int highGradients = gradients - lowGradients;

        redStart = middleColor.getRed();
        blueStart = middleColor.getBlue();
        greenStart = middleColor.getGreen();

        redGap = topColor.getRed() - redStart;
        blueGap = topColor.getBlue() - blueStart;
        greenGap = topColor.getGreen() - greenStart;

        for (int i = lowGradients; i < gradients; i++) {
            double delta = (double) (i - lowGradients) / (double) highGradients;
            c[0] = getBoundedCol(redStart + (int) (redGap * delta));
            c[1] = getBoundedCol(greenStart + (int) (greenGap * delta));
            c[2] = getBoundedCol(blueStart + (int) (blueGap * delta));

            addColor(i, new Color(c[0], c[1], c[2]));
        }
    }

    private int getBoundedCol(double d) {
        return Math.min(255, (int) d);
    }

    /**
     * Return the color index.
     *
     * @param value The value to be mapped. If it is outside the range bounds, the method returns its nearest bound.
     * @return The index of the color list mapping the value.
     */
    public int getColorIndex(int value) {
        int i = (int) ((value - minValue) * rangeSize);
        return i < 0 ? 0 : (i >= colorList.length ? colorList.length - 1 : i);
    }

    /**
     * Return the color index.
     *
     * @param value The value to be mapped. If it is outside the range bounds, the method returns its nearest bound.
     * @return The index of the color list mapping the value.
     */
    public int getColorIndex(double value) {
        int i = (int) ((value - minValue) * rangeSize);
        return i < 0 ? 0 : (i >= colorList.length ? colorList.length - 1 : i);
    }
}
