package microsim.gui.probe;

import java.util.List;

/**
 * This interface allows the object implementing it to show only some properties and methods when probed.
 */

public interface ProbeFields {
    /**
     * Return a list contaning strings corresponding to the properties and the methods shown by a probe.
     *
     * @return A list of String objects.
     */
    List<Object> getProbeFields();
}
