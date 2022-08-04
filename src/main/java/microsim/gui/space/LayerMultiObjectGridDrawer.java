package microsim.gui.space;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import microsim.gui.colormap.ColorMap;
import microsim.space.MultiObjectSpace;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 * It is able to draw objects contained by a MultiObjGrid on a LayeredSurfaceFrame.<br> When on a cell there is at least
 * one object it is represented by a circle. The objects are drawn using one given color.
 */
@Log
public class LayerMultiObjectGridDrawer implements LayerDrawer {
    @Getter
    private final String description;
    MultiObjectSpace space;
    Color c;
    private ColorMap colorMap;
    private String agentProperty;
    @Setter
    @Getter
    private boolean displayed = true;
    @Setter
    private LayerML mouseListener = null;

    /**
     * Create a new object drawer based on a given MultiObjGrid object. It plots the objects using the given color.
     *
     * @param name   The string describing the layer.
     * @param matrix A MultiObjGrid object.
     * @param color  The default color used to plot objects.
     */
    public LayerMultiObjectGridDrawer(String name, MultiObjectSpace matrix, Color color) {
        description = name;
        space = matrix;
        c = color;
    }

    /**
     * Create a new object drawer based on a given MultiObjGrid object. It plots the objects using the color of the
     * first object found on each cell.
     *
     * @param name   The string describing the layer.
     * @param matrix A MultiObjGrid object.
     */
    public LayerMultiObjectGridDrawer(String name, MultiObjectSpace matrix, String agentProperty, ColorMap map) {
        this(name, matrix, null);
        this.colorMap = map;
        this.agentProperty = agentProperty;
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
            } catch (SecurityException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
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

    private Color getColor(Object agent) throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Class<?> clazz = agent.getClass();
        Field field = clazz.getField(agentProperty);
        field.setAccessible(true);
        int index;
        if (field.getType().equals(Double.class)) index = colorMap.getColorIndex(field.getDouble(agent));
        else index = colorMap.getColorIndex(field.getInt(agent));

        int[] components = colorMap.getColorComponents(index);
        return new Color(components[0], components[1], components[2]);

    }

    private void paintWithoutColor(Graphics g, int cellLen) throws SecurityException, IllegalArgumentException,
            NoSuchFieldException, IllegalAccessException {
        Object[] obj;
        Color cl, currentColor = null;

        for (int i = 0; i < space.getXSize(); i++)
            for (int j = 0; j < space.getYSize(); j++)
                if ((obj = (Object[]) space.get(i, j)) != null)
                    for (int k = 0; k < obj.length; k++)
                        if (obj[k] != null) {
                            cl = getColor(obj[k]);
                            if (cl != currentColor) {
                                currentColor = cl;
                                g.setColor(currentColor);
                            }
                            int XX = i * cellLen;
                            int YY = j * cellLen;
                            g.fillOval(XX, YY, cellLen, cellLen);
                            k = obj.length;
                        }
    }

    /**
     * If a mouse listener has been defined the double-click event, it is passed to it, otherwise it is shown a
     * CellObjectChooser that allows the user to choose which object to be probed.
     *
     * @param atX The x coordinate of the clicked cell.
     * @param atY The y coordinate of the clicked cell.
     * @return always true if no mouse listener is defined. This value is used by caller to know if the layer wants to
     * manage the event.
     */
    public boolean performDblClickActionAt(int atX, int atY) {
        if (mouseListener != null) return mouseListener.performDblClickActionAt(atX, atY);

        if (space.get(atX, atY) == null) return false;

        Object[] p = (Object[]) space.get(atX, atY);
        CellObjectChooser chooser = new CellObjectChooser(p, null, "Objects at %d, %d. ".formatted(atX, atY), true);
        chooser.setVisible(true);
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
