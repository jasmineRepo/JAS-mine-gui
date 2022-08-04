package microsim.gui.space;

import java.awt.Graphics;

/**
 * An interface used by LayeredSurfaceFrame to delegate the rendering of a layer.
 */
public interface LayerDrawer extends LayerML {
    /**
     * Return the description of the layer.
     *
     * @return The string describing the layer.
     */
    String getDescription();

    /**
     * Return if the layer is currently displayed.
     *
     * @return True if the layer is displayed.
     */
    boolean isDisplayed();

    /**
     * Set the display status.
     *
     * @param display If true the layer will be displayed.
     */
    void setDisplayed(boolean display);

    /**
     * Paint the layer on the screen.
     *
     * @param g       The current graphics device.
     * @param cellLen The length of a cell in pixels.
     */
    void paint(Graphics g, int cellLen);
}
