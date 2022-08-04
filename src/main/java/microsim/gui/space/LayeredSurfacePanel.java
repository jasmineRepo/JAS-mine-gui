package microsim.gui.space;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * It is the panel drawing the Layer<type>Drawer objects added to the LayeredSurfaceFrame. It manages mouse events, too.
 */
public class LayeredSurfacePanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;
    @Getter
    private final List<LayerDrawer> mLayers;
    private int xSize;
    private int ySize;
    private int cellLen;

    private int virtualWidth, virtualHeight;

    @Setter
    @Getter
    private Color background;

    //Used for dragging
    private int lastX, lastY;

    /**
     * Create a panel with dimensions of (100, 100) and a cell length of 4 pixels.
     */
    public LayeredSurfacePanel() {
        this(100, 100, 4);
    }

    /**
     * Create a panel with given dimensions and given cell length.
     *
     * @param width      The width of the grid to plot.
     * @param height     The height of the grid to plot.
     * @param cellLength The lenght of a grid cell in pixels.
     */
    public LayeredSurfacePanel(int width, int height, int cellLength) {
        xSize = width;
        ySize = height;
        cellLen = cellLength;
        setVirtualDimensions();
        mLayers = new ArrayList<>();
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() {
        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                this_mouseDragged(e);
            }
        });
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                this_mouseClicked(e);
            }

            public void mousePressed(MouseEvent e) {
                this_mousePressed(e);
            }

            public void mouseReleased(MouseEvent e) {
                this_mouseReleased(e);
            }
        });
    }

    private void setVirtualDimensions() {
        virtualWidth = xSize * cellLen;
        virtualHeight = ySize * cellLen;

        this.setSize(virtualWidth, virtualHeight);
        this.setPreferredSize(new Dimension(virtualWidth, virtualHeight));
    }

    /**
     * Change the size of the grid.
     *
     * @param width  The width of the grid to plot.
     * @param height The height of the grid to plot.
     */
    public void setVirtualSize(int width, int height) {
        xSize = width;
        ySize = height;
        setVirtualDimensions();
    }

    /**
     * Add a LayerDrawer to the layer list.
     *
     * @param layer The LayerDrawer to be plotted.
     */
    public void addLayer(LayerDrawer layer) {
        mLayers.add(layer);
    }

    /**
     * Change the current cell length.
     *
     * @param cellLength The new cell length in pixels.
     */
    public void setCellLength(int cellLength) {
        cellLen = cellLength;
        setVirtualDimensions();
    }

    /**
     * Draw the panel.
     *
     * @param g The graphic context passed by container.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (background != null) {
            g.setColor(background);
            g.fillRect(0, 0, virtualWidth, virtualHeight);
        }

        LayerDrawer lay;
        for (LayerDrawer mLayer : mLayers) {
            lay = mLayer;
            if (lay.isDisplayed()) lay.paint(g, cellLen);
        }

    }

    private void this_mouseClicked(MouseEvent e) {
        LayerDrawer lay;

        if (e.getClickCount() != 2) return;

        int x = e.getX() / cellLen;
        int y = e.getY() / cellLen;

        for (int i = mLayers.size() - 1; i >= 0; i--) {
            lay = mLayers.get(i);
            if (lay.isDisplayed()) if (lay.performDblClickActionAt(x, y)) return;
        }

    }

    private void this_mousePressed(MouseEvent e) {
        lastX = e.getX() / cellLen;
        lastY = e.getY() / cellLen;
    }

    private void this_mouseDragged(MouseEvent e) {
    }

    private void this_mouseReleased(MouseEvent e) {
        LayerDrawer lay;
        if (lastX < 0 || lastX > virtualWidth || lastY < 0 || lastY > virtualHeight) return;

        int x = e.getX() / cellLen;
        int y = e.getY() / cellLen;

        for (int i = mLayers.size() - 1; i >= 0; i--) {
            lay = mLayers.get(i);
            if (lay.isDisplayed()) if (lay.performMouseMovedFromTo(lastX, lastY, x, y)) return;
        }
    }
}
