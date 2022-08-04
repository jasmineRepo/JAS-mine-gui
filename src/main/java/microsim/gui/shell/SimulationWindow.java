package microsim.gui.shell;

import lombok.Getter;
import lombok.Setter;

import java.awt.Container;
import java.awt.Rectangle;

/**
 * SimWindow keeps preferred dimensions of a simulation windows.
 */
public class SimulationWindow {
    @Getter
    private final String key;
    @Getter
    private final String model;
    @Setter
    @Getter
    private Container window;
    @Setter
    @Getter
    private Rectangle defaultPosition;

    /**
     * Create a new window container with the given parameters.
     *
     * @param model  The id of the model owner
     * @param key    The key used to store the element in the HashMap
     * @param window The window to be managed
     */
    public SimulationWindow(String model, String key, Container window) {
        this.window = window;
        this.model = model;
        this.key = key;
    }

    /**
     * Return the dimension of the managed window. If the window is not yet created the method
     * returns the default bounds.
     *
     * @return The window dimensions if present or the default ones if not.
     */
    public Rectangle getBounds() {
        if (window == null) return getDefaultPosition();
        else return window.getBounds();
    }

    public String toString() {
        return key;
    }
}
