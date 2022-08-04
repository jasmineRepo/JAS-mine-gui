package microsim.gui.space;

import lombok.Getter;
import lombok.Setter;
import microsim.gui.colormap.ColorMap;
import microsim.space.DoubleSpace;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * It is able to draw a DblGrid on a LayeredSurfaceFrame using a ColorMap to render the values contained by the cell
 * with a specific color.<br> This class builds an image when created and every time is updated it modifies the parts
 * of the images that are changed. It is very fast when images do not change too frequently. In order to let the painter
 * go faster it is useful to reduce the number of color gradients in the ColorMap.
 */
public class LayerDblGridDrawer implements LayerDrawer {
    private final double[] m;
    private final ColorMap color;
    private final int xSize;
    private final int ySize;
    @Getter
    private final String description;
    int[] transparencyColor;
    private int cellSize = 4;
    @Setter
    @Getter
    private boolean Displayed = true;
    private int[] stateBuffer;
    private BufferedImage img;

    private LayerML mouseListener = null;

    /**
     * Create a double layer drawer using values taken from an array of doubles and a given IColorMap.
     *
     * @param name       The string describing the layer.
     * @param matrix     An array of doubles of width * height length.
     * @param width      The width of the grid.
     * @param height     The height of the grid.
     * @param colorRange The IColorMap used to map values to colors.
     */
    public LayerDblGridDrawer(String name, double[] matrix, int width, int height, ColorMap colorRange) {
        m = matrix;
        description = name;
        color = colorRange;
        xSize = width;
        ySize = height;
        transparencyColor = null;

        img = new BufferedImage(xSize * cellSize, ySize * cellSize, BufferedImage.TYPE_INT_RGB);
        buildBufferImage();
    }

    /**
     * Create a double layer drawer using values taken from a DblGrid matrix and a given IColorMap.
     *
     * @param name       The string describing the layer.
     * @param matrix     A DblGrid object.
     * @param colorRange The IColorMap used to map values to colors.
     */
    public LayerDblGridDrawer(String name, DoubleSpace matrix, ColorMap colorRange) {
        this(name, matrix.getMatrix(), matrix.getXSize(), matrix.getYSize(), colorRange);
    }

    /**
     * Create a double layer drawer using values taken from an array of doubles and a given IColorMap. It allows to
     * define a transparency color. Every time the drawer has to plot the transparentColor it stops drawing, so the cell
     * of underneath layer becomes visible.
     *
     * @param name             The string describing the layer.
     * @param matrix           An array of doubles of width * height length.
     * @param width            The width of the grid.
     * @param height           The height of the grid.
     * @param colorRange       The IColorMap used to map values to colors.
     * @param transparentColor A color
     */
    public LayerDblGridDrawer(String name, double[] matrix, int width, int height, ColorMap colorRange,
                              Color transparentColor) {
        m = matrix;
        description = name;
        color = colorRange;
        xSize = width;
        ySize = height;
        transparencyColor = new int[3];
        transparencyColor[0] = transparentColor.getRed();
        transparencyColor[1] = transparentColor.getGreen();
        transparencyColor[2] = transparentColor.getBlue();

        img = new BufferedImage(xSize * cellSize, ySize * cellSize, BufferedImage.TYPE_INT_ARGB);
        buildBufferImage();
    }

    /**
     * Create a double layer drawer using values taken from a DblGrid matrix and a given IColorMap. It allows to define
     * a transparency color. Every time the drawer has to plot the transparentColor it stops drawing, so the cell of
     * underneath layer becomes visible.
     *
     * @param name             The string describing the layer.
     * @param matrix           A DblGrid object.
     * @param colorRange       The IColorMap used to map values to colors.
     * @param transparentColor A color
     */
    public LayerDblGridDrawer(String name, DoubleSpace matrix, ColorMap colorRange, Color transparentColor) {
        this(name, matrix.getMatrix(), matrix.getXSize(), matrix.getYSize(), colorRange, transparentColor);
    }

    private void buildBufferImage() {
        WritableRaster raster = img.getRaster();
        stateBuffer = new int[xSize * ySize];

        int colorDepth = transparencyColor == null ? 3 : 4;

        int[] pixels = new int[colorDepth * cellSize * cellSize];

        int[] currColor;
        int currIndex;

        int k = 0;
        for (int j = 0; j < ySize; j++)
            for (int i = 0; i < xSize; i++) {
                currIndex = color.getColorIndex(m[k]);
                currColor = color.getColorComponents(currIndex);
                stateBuffer[k] = currIndex;
                int XX = i * cellSize;
                int YY = j * cellSize;

                for (int z = 0; z < pixels.length; z += colorDepth) {
                    pixels[z] = currColor[0];
                    pixels[z + 1] = currColor[1];
                    pixels[z + 2] = currColor[2];
                    if (colorDepth == 4)
                        pixels[z + 3] = Arrays.equals(currColor, transparencyColor) ? 0 : 255;
                }

                raster.setPixels(XX, YY, cellSize, cellSize, pixels);
                k++;
            }
    }

    /**
     * Draw the layer using the given cell length.
     *
     * @param g       The graphic context passed by container.
     * @param cellLen The length of a cell in pixels.
     */
    public void paint(Graphics g, int cellLen) {
        if (transparencyColor != null) paintWithTrasparency(g, cellLen);
        else paintWithoutTrasparency(g, cellLen);
    }

    private void setCellLenght(int cellLength) {
        cellSize = cellLength;
        img = new BufferedImage(xSize * cellSize, ySize * cellSize,
                transparencyColor == null ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);

        buildBufferImage();
    }

    private void paintWithoutTrasparency(Graphics g, int cellLen) {
        WritableRaster raster = img.getRaster();// fixme remove duplicates
        int[] pixels = new int[3 * cellLen * cellLen];

        int[] currColor;
        int currIndex;

        if (cellSize != cellLen) setCellLenght(cellLen);

        int k = 0;
        for (int j = 0; j < ySize; j++)
            for (int i = 0; i < xSize; i++) {
                currIndex = color.getColorIndex(m[k]);
                if (currIndex != stateBuffer[k]) {
                    currColor = color.getColorComponents(currIndex);
                    stateBuffer[k] = currIndex;
                    int XX = i * cellLen;
                    int YY = j * cellLen;

                    for (int z = 0; z < pixels.length; z += 3) {
                        pixels[z] = currColor[0];
                        pixels[z + 1] = currColor[1];
                        pixels[z + 2] = currColor[2];
                    }

                    raster.setPixels(XX, YY, cellLen, cellLen, pixels);
                }
                k++;
            }

        g.drawImage(img, 0, 0, null);
    }

    private void paintWithTrasparency(Graphics g, int cellLen) {
        WritableRaster raster = img.getRaster();
        int[] pixels = new int[4 * cellLen * cellLen];
        int[] currColor;
        int currIndex;
        int alpha;

        if (cellSize != cellLen) setCellLenght(cellLen);

        int k = 0;
        for (int j = 0; j < ySize; j++)
            for (int i = 0; i < xSize; i++) {
                currIndex = color.getColorIndex(m[k]);
                if (currIndex != stateBuffer[k]) {
                    currColor = color.getColorComponents(currIndex);
                    stateBuffer[k] = currIndex;
                    int XX = i * cellLen;
                    int YY = j * cellLen;

                    alpha = Arrays.equals(currColor, transparencyColor) ? 0 : 255;

                    for (int z = 0; z < pixels.length; z += 4) {
                        pixels[z] = currColor[0];
                        pixels[z + 1] = currColor[1];
                        pixels[z + 2] = currColor[2];
                        pixels[z + 3] = alpha;
                    }

                    raster.setPixels(XX, YY, cellLen, cellLen, pixels);
                }
                k++;
            }

        g.drawImage(img, 0, 0, null);
    }

    /**
     * Set a manager for mouse events. If not defined, mouse events are managed by the class itself.
     *
     * @param listener A ILayerMouseListener object.
     */
    public void setMouseListener(LayerML listener) {
        mouseListener = listener;
    }

    /**
     * If a mouse listener has been defined the double-click event, it is passed to it, otherwise it is shown a message
     * box with the value contained by the clicked cell.
     *
     * @param atX The x coordinate of the clicked cell.
     * @param atY The y coordinate of the clicked cell.
     * @return true if no mouse listener is defined. This value is used by caller to know if the layer wants to manage
     * the event.
     */
    public boolean performDblClickActionAt(int atX, int atY) {
        if (mouseListener != null) return mouseListener.performDblClickActionAt(atX, atY);

        javax.swing.JOptionPane.showMessageDialog(null, "Value at(" + atX + ", " + atY + "): " + m[atY * xSize + atX],
                "Probing " + getDescription(), javax.swing.JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    /**
     * If a mouse listener has been defined the right-click event, it is passed to it, otherwise it is returned false.
     *
     * @param atX The x coordinate of the clicked cell.
     * @param atY The y coordinate of the clicked cell.
     * @return false if no mouse listener is defined. This value is used by caller to know if the layer wants to manage
     * the event.
     */
    public boolean performRightClickActionAt(int atX, int atY) {
        if (mouseListener != null) return mouseListener.performRightClickActionAt(atX, atY);

        return false;
    }

    /**
     * If a mouse listener has been defined the mouse dragging event, it is passed to it, otherwise it is returned
     * false.
     *
     * @param fromX The x coordinate of the starting cell.
     * @param fromY The y coordinate of the starting cell.
     * @param toX   The x coordinate of the last dragged cell.
     * @param toY   The y coordinate of the last dragged cell.
     * @return false if no mouse listener is defined. This value is used by caller to know if the layer wants to manage
     * the event.
     */
    public boolean performMouseMovedFromTo(int fromX, int fromY, int toX, int toY) {
        if (mouseListener != null) return mouseListener.performMouseMovedFromTo(fromX, fromY, toX, toY);

        return false;
    }
}
