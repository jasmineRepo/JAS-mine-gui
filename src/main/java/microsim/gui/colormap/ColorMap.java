package microsim.gui.colormap;

/**
 * A generic interface for color mappers. This interface is required by Layer<NativeType>Drawer objects to paint values
 * on the screen.
 */
public interface ColorMap {
    /**
     * Return the components of the color stored at given index.
     *
     * @param index The index of the color. It is a 0-based index of the color corresponding to the adding order.
     * @return An array of 3 integers representing the RGB components of the color.
     */
    int[] getColorComponents(int index);

    /**
     * Return the index of the color mapped to the given value.
     *
     * @param value The value mapped to the color.
     * @return The array index of the requested color.
     */
    int getColorIndex(int value);

    /**
     * Return the index of the color mapped to the given value.
     *
     * @param value The value mapped to the color.
     * @return The array index of the requested color.
     */
    int getColorIndex(double value);
}
