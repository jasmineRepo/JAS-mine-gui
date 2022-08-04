package microsim.gui.plot;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * A bin for the {@link Weighted_HistogramBin} class.
 */
public class Weighted_HistogramBin implements Cloneable, Serializable {

    /**
     * For serialisation.
     */
    @Serial
    private static final long serialVersionUID = 7614685080015589931L;
    /**
     * The start boundary.
     */
    @Getter
    private final double startBoundary;
    /**
     * The end boundary.
     */
    @Getter
    private final double endBoundary;
    /**
     * The (weighted) number of items in the bin, the weights can be double, meaning that the count can also be double.
     */
    @Getter
    private double count;

    /**
     * Creates a new bin.
     *
     * @param startBoundary the start boundary.
     * @param endBoundary   the end boundary.
     */
    public Weighted_HistogramBin(double startBoundary, double endBoundary) {
        if (startBoundary > endBoundary)
            throw new IllegalArgumentException("HistogramBin():  startBoundary > endBoundary.");
        this.count = 0.;
        this.startBoundary = startBoundary;
        this.endBoundary = endBoundary;
    }

    /**
     * Increments the item count.
     */
    public void incrementCount(double weight) {
        this.count += weight;
    }

    /**
     * Returns the bin width.
     *
     * @return The bin width.
     */
    public double getBinWidth() {
        return this.endBoundary - this.startBoundary;
    }

    /**
     * Tests this object for equality with an arbitrary object.
     *
     * @param obj the object to test against.
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof Weighted_HistogramBin bin) {
            boolean b0 = bin.startBoundary == this.startBoundary;
            boolean b1 = bin.endBoundary == this.endBoundary;
            boolean b2 = bin.count == this.count;
            return b0 && b1 && b2;
        }
        return false;
    }

    /**
     * Returns a clone of the bin.
     *
     * @return A clone.
     * @throws CloneNotSupportedException not thrown by this class.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
