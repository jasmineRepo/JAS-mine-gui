package microsim.gui.space;

/**
 * A generic implementation of the LayerML interface. If you want to manage only one or two mouse events, you can extend
 * this class, overriding only the useful methods. The methods not overridden return always false.
 */
public class LayerMouseListener implements LayerML {
    public boolean performDblClickActionAt(int atX, int atY) {
        return false;
    }

    public boolean performRightClickActionAt(int atX, int atY) {
        return false;
    }

    public boolean performMouseMovedFromTo(int fromX, int fromY, int toX, int toY) {
        return false;
    }
}
