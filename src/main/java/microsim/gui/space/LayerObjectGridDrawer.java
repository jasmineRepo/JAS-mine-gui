package microsim.gui.space;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import microsim.gui.colormap.ColorMap;
import microsim.gui.probe.ProbeFrame;
import microsim.reflection.ReflectionUtils;
import microsim.space.ObjectSpace;
import microsim.statistics.reflectors.DoubleInvoker;
import microsim.statistics.reflectors.IntegerInvoker;

import java.awt.*;
import java.util.logging.Level;

/**
 * It is able to draw objects contained by an ObjGrid on a LayeredSurfaceFrame.<br> An object is represented by a
 * circle. The objects could be drawn using one given color, or implementing the IColored interface inside them, each
 * object returns to the LayerObjGridDrawer which color to use.
 */
@Log
public class LayerObjectGridDrawer implements LayerDrawer {
    @Getter
    private final String description;
    ObjectSpace space;
    Color c;
    private ColorMap colorMap;
    @Setter
    @Getter
    private boolean displayed = true;
    private Object invoker = null;

    @Setter
    private LayerML mouseListener = null;

    /**
     * Create a new object drawer based on a given Grid object. It plots the objects using the given color. NOTICE that
     * the matrix parameter accepts a generic Grid, so also classes like IntGrid could be drawn.
     *
     * @param name   The string describing the layer.
     * @param matrix A generic Grid object.
     * @param color  The default color used to plot objects.
     */
    public LayerObjectGridDrawer(String name, ObjectSpace matrix, Color color) {
        description = name;
        space = matrix;
        c = color;
    }

    /**
     * Create a new object drawer based on a given Grid object. It plots the objects using the color they return. NOTICE
     * that the matrix parameter accepts a generic Grid, so also classes like IntGrid could be drawn.
     *
     * @param name   The string describing the layer.
     * @param matrix A generic Grid object containing IColored objects.
     */
    public LayerObjectGridDrawer(String name, ObjectSpace matrix, Class<?> targetClass, String variableName,
                                 boolean isMethod, ColorMap map) {
        this(name, matrix, null);
        this.colorMap = map;
        if (ReflectionUtils.isDoubleSource(targetClass, variableName, isMethod))
            invoker = new DoubleInvoker(targetClass, variableName, isMethod);
        else if (ReflectionUtils.isIntSource(targetClass, variableName, isMethod))
            invoker = new IntegerInvoker(targetClass, variableName, isMethod);
        else
            throw new IllegalArgumentException("Supported field type: double, int");
    }

    private void paintWithColor(Graphics g, int cellLen) {
        g.setColor(c);

        for (int i = 0; i < space.getXSize(); i++)
            for (int j = 0; j < space.getYSize(); j++)
                if (space.countObjectsAt(i, j) > 0) {
                    int XX = i * cellLen;
                    int YY = j * cellLen;
                    g.fillOval(XX, YY, cellLen, cellLen);
                }
    }

    private Color getColor(Object agent) throws SecurityException, IllegalArgumentException {

        int level = switch (invoker) {
            case DoubleInvoker doubleInvoker -> (int) doubleInvoker.getDouble(agent);
            case null, default -> {
                assert invoker != null;
                yield ((IntegerInvoker) invoker).getInt(agent);
            }
        };

        int index = colorMap.getColorIndex(level);

        int[] components = colorMap.getColorComponents(index);
        return new Color(components[0], components[1], components[2]);

    }

    private void paintWithoutColor(Graphics g, int cellLen) throws SecurityException, IllegalArgumentException,
            NoSuchFieldException, IllegalAccessException {
        Object obj;
        Color cl, currentColor = null;

        for (int i = 0; i < space.getXSize(); i++)
            for (int j = 0; j < space.getYSize(); j++)
                if ((obj = space.get(i, j)) != null) {
                    cl = getColor(obj);
                    if (cl != currentColor) {
                        currentColor = cl;
                        g.setColor(currentColor);
                    }
                    int XX = i * cellLen;
                    int YY = j * cellLen;
                    g.fillOval(XX, YY, cellLen, cellLen);
                }
    }

    /**
     * Draw the layer using the given cell length.
     *
     * @param g       The graphic context passed by container.
     * @param cellLen The length of a cell in pixels.
     */
    public void paint(Graphics g, int cellLen) {
        if (c != null)
            paintWithColor(g, cellLen);
        else
            try {
                paintWithoutColor(g, cellLen);
            } catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
    }

    /**
     * If a mouse listener has been defined the double-click event, it is passed to it, otherwise it is shown a message
     * box with the value contained by the clicked cell.
     *
     * @param atX The x coordinate of the clicked cell.
     * @param atY The y coordinate of the clicked cell.
     * @return always true if no mouse listener is defined. This value is used by caller to know if the layer wants to
     * manage the event.
     */
    public boolean performDblClickActionAt(int atX, int atY) {
        if (mouseListener != null) return mouseListener.performDblClickActionAt(atX, atY);

        if (space.get(atX, atY) == null) return false;

        Object p = space.get(atX, atY);
        ProbeFrame pf = new ProbeFrame(p, p.toString());
        pf.setVisible(true);
        return true;
    }

    /**
     * If a mouse listener has been defined the right-click event, it is passed to it, otherwise it is returned false.
     *
     * @param atX The x coordinate of the clicked cell.
     * @param atY The y coordinate of the clicked cell.
     * @return always false if no mouse listener is defined. This value is used by caller to know if the layer wants to
     * manage the event.
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
     * @return always false if no mouse listener is defined. This value is used by caller to know if the layer wants to
     * manage the event.
     */
    public boolean performMouseMovedFromTo(int fromX, int fromY, int toX, int toY) {
        if (mouseListener != null) return mouseListener.performMouseMovedFromTo(fromX, fromY, toX, toY);

        return false;
    }
}
// FIXME looks like too many duplicates, go through this section again