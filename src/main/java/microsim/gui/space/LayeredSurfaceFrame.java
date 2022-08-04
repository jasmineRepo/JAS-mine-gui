package microsim.gui.space;

import microsim.event.CommonEventType;
import microsim.event.EventListener;
import microsim.gui.shell.MicrosimShell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.Serial;

/**
 * It is the Space Viewer window. It draws grid layers using a list of LayerDrawer objects. See LayeredGridDrawer
 * classes of this library. They are wrapper classes for Grid objects of the jasmine.space.* library and are able to
 * plot their contents.
 */
public class LayeredSurfaceFrame extends JInternalFrame implements EventListener {

    @Serial
    private static final long serialVersionUID = 1L;
    private final static int MIN_WIDTH = 10;
    private final static int MIN_HEIGHT = 10;

    private final static int DEFAULT_CELL_LENGTH = 4;

    private final int xSize;
    private final int ySize;
    private final Dimension screenSize;
    BorderLayout borderLayout1 = new BorderLayout();
    JScrollPane jScrollPane = new JScrollPane();
    LayeredSurfacePanel jLayeredPanel;
    JPopupMenu popupMenu = new JPopupMenu();
    private int cellLen;

    /**
     * {@code gridWidth} and {@code gridHeight} default to {@code width} and {@code height}, respectively.
     * The value of {@code cellLength} is {@code DEFAULT_CELL_LENGTH} or 4 pixels.
     *
     * @see #LayeredSurfaceFrame(int, int, int, int, int)
     */
    public LayeredSurfaceFrame(int width, int height) {
        this(width, height, DEFAULT_CELL_LENGTH);
    }

    /**
     * {@code gridWidth} and {@code gridHeight} default to {@code width} and {@code height}, respectively.
     *
     * @see #LayeredSurfaceFrame(int, int, int, int, int)
     */
    public LayeredSurfaceFrame(int width, int height, int cellLength) {
        this(width, height, width, height, cellLength);
    }

    /**
     * Create a new frame with given dimensions, given cell length and given view-port dimensions.
     *
     * @param width      The width of the viewable area in cells.
     * @param height     The height of the viewable area in cells.
     * @param cellLength The length of a grid cell in pixels.
     * @param gridWidth  The real width of the grid to plot.
     * @param gridHeight The real height of the grid to plot.
     * @throws IllegalArgumentException if width <= 0 || height <= 0.
     */
    public LayeredSurfaceFrame(int width, int height, int gridWidth, int gridHeight, int cellLength) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException(
                    "LayeredSurfaceFrame must be created with positive width and height values.");

        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        xSize = width;
        ySize = height;
        cellLen = cellLength;

        jLayeredPanel = new LayeredSurfacePanel(gridWidth, gridHeight, cellLen);

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() {
        this.setResizable(true);
        this.setTitle("Space viewer");
        setLocation(50, 50);
        this.getContentPane().setLayout(borderLayout1);

        jLayeredPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                jLayeredPanel_mouseReleased(e);
            }
        });
        this.getContentPane().add(jScrollPane, BorderLayout.CENTER);
        jScrollPane.getViewport().add(jLayeredPanel, null);

        JMenuItem props = new JMenuItem("Properties");
        props.addActionListener(this::jBtnProperties_actionPerformed);
        popupMenu.add(props);

        adjustSize();
    }

    private void adjustSize() {
        setSize(0, 0);
    }

    /**
     * Change the current cell length.
     *
     * @param cellLength The new cell length in pixels.
     */
    public void setCellLength(int cellLength) {
        cellLen = cellLength;
        jLayeredPanel.setCellLength(cellLength);
    }

    /**
     * Add a LayerDrawer to the layer list.
     *
     * @param layer The LayerDrawer to be plotted.
     */
    public void addLayer(LayerDrawer layer) {
        jLayeredPanel.addLayer(layer);
    }

    /**
     * Repaint the plot area.
     */
    public void update() {
        jLayeredPanel.repaint();
    }

    private void jBtnProperties_actionPerformed(ActionEvent e) {
        LayeredSurfaceProperties dlg = new LayeredSurfaceProperties(MicrosimShell.currentShell,
                "Space viewer properties", cellLen, jLayeredPanel.getMLayers());
        dlg.setVisible(true);

        if (!dlg.modified) return;

        if (dlg.newCellSize > 0) setCellLength(dlg.newCellSize);

        adjustSize();
        this.setVisible(true);
    }

    /**
     * Update the window size according to the parameters passed to the constructor.
     *
     * @param x It is ignored. The width is computed automatically.
     * @param y It is ignored. The height is computed automatically.
     */
    public void setSize(int x, int y) {
        int width = cellLen * xSize + 10;
        int height = cellLen * ySize + 28; // BUTTON_PANEL_HEIGHT;

        if (width > screenSize.getWidth()) width = (int) screenSize.getWidth();
        if (height > screenSize.getHeight()) height = (int) screenSize.getHeight();

        if (width < MIN_WIDTH) width = MIN_WIDTH;
        if (height < MIN_HEIGHT) height = MIN_HEIGHT;

        super.setSize(width, height);
    }

    /**
     * React to system events.
     *
     * @param type Reacts to the Sim.EVENT_UPDATE event repainting the plot area.
     */
    public void onEvent(Enum<?> type) {
        if (type == CommonEventType.Update) update();
    }

    void jLayeredPanel_mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger() || e.getButton() == 3) popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
}
