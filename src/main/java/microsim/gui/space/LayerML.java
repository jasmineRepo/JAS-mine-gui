package microsim.gui.space;

/**
 * An interface used by LayerDrawer to manage the mouse events.
 */
public interface LayerML {
    /**
     * Notify a double click event on a specific cell.
     *
     * @param atX The x coordinate of the clicked cell.
     * @param atY The y coordinate of the clicked cell.
     * @return True if the layer intercepted the event. False if the notify has been ignored.
     */
    boolean performDblClickActionAt(int atX, int atY);

    /**
     * Notify a right button click event on a specific cell.
     *
     * @param atX The x coordinate of the clicked cell.
     * @param atY The y coordinate of the clicked cell.
     * @return True if the layer intercepted the event. False if the notify has been ignored.
     */
    boolean performRightClickActionAt(int atX, int atY);

    /**
     * Notify a mouse dragging action.
     *
     * @param fromX The x coordinate of the starting cell.
     * @param fromY The y coordinate of the starting cell.
     * @param toX   The x coordinate of the target cell.
     * @param toY   The y coordinate of the target cell.
     * @return True if the layer intercepted the event. False if the notify has been ignored.
     */
    boolean performMouseMovedFromTo(int fromX, int fromY, int toX, int toY);
}
