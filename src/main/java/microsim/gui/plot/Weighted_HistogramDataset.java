package microsim.gui.plot;

import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * A weighted dataset that can be used for creating weighted histograms.
 */
public class Weighted_HistogramDataset extends AbstractIntervalXYDataset implements IntervalXYDataset, Cloneable,
        PublicCloneable, Serializable {
    /**
     * For serialization.
     */
    @Serial
    private static final long serialVersionUID = -6341668077370231153L;

    /**
     * A list of maps.
     */
    private List<Object> list;

    /**
     * The histogram type.
     */
    private HistogramType type;

    /**
     * Sum of the total weightings of each value, in order to correctly calculate the chart for RELATIVE_FREQUENCY type.
     */
    private double totalWeight;

    /**
     * Creates a new (empty) dataset with a default type of {@link HistogramType}.FREQUENCY.
     */
    public Weighted_HistogramDataset() {
        this.list = new ArrayList<>();
        this.type = HistogramType.FREQUENCY;
        totalWeight = 0.;
    }

    /**
     * Returns the histogram type.
     *
     * @return The type (never {@code null}).
     */
    public HistogramType getType() {
        return this.type;
    }

    /**
     * Sets the histogram type and sends a {@link DatasetChangeEvent} to all registered listeners.
     *
     * @param type the type ({@code null} not permitted).
     */
    public void setType(HistogramType type) {
        Args.nullNotPermitted(type, "type");
        this.type = type;
        fireDatasetChanged();
    }

    /**
     * Adds a series to the dataset, using the specified number of bins, and sends a {@link DatasetChangeEvent} to all
     * registered listeners.
     *
     * @param key        the series key ({@code null} not permitted).
     * @param values     the values ({@code null} not permitted).
     * @param weightings the weights associated with the values, i.e. weight {@code i} indicates the number of times the
     *                   value {@code i} appears ({@code null} not permitted).
     * @param bins       the number of bins (must be at least 1).
     */
    public void addSeries(Comparable<?> key, double[] values, double[] weightings, int bins) {
        // defer argument checking...
        double minimum = getMinimum(values);
        double maximum = getMaximum(values);
        addSeries(key, values, weightings, bins, minimum, maximum);
    }

    /**
     * Adds a series to the dataset. Any data value less than minimum will be assigned to the first bin, and any data
     * value greater than maximum will be assigned to the last bin. Values falling on the boundary of adjacent bins will
     * be assigned to the higher indexed bin.
     *
     * @param key        the series key ({@code null} not permitted).
     * @param values     the raw observations.
     * @param weightings the weights associated with the values, i.e. weight {@code i} indicates the number of times the
     *                   value {@code i}  appears ({@code null} not permitted).
     * @param bins       the number of bins (must be at least 1).
     * @param minimum    the lower bound of the bin range.
     * @param maximum    the upper bound of the bin range.
     */
    public void addSeries(Comparable<?> key, double[] values, double[] weightings, int bins,
                          double minimum, double maximum) {

        Args.nullNotPermitted(key, "key");
        Args.nullNotPermitted(values, "values");
        Args.nullNotPermitted(weightings, "weightings");
        if (bins < 1)
            throw new IllegalArgumentException("The 'bins' value must be at least 1.");
        if (values.length != weightings.length)
            throw new IllegalArgumentException("The length of weightings array must be the same as the values array!");

        double binWidth = (maximum - minimum) / bins;

        double lower = minimum;
        double upper;
        val binList = new ArrayList<>(bins);
        for (int i = 0; i < bins; i++) {
            Weighted_HistogramBin bin;
            // make sure bins[bins.length]'s upper boundary ends at maximum
            // to avoid the rounding issue. the bins[0] lower boundary is
            // guaranteed start from min
            if (i == bins - 1) {
                bin = new Weighted_HistogramBin(lower, maximum);
            } else {
                upper = minimum + (i + 1) * binWidth;
                bin = new Weighted_HistogramBin(lower, upper);
                lower = upper;
            }
            binList.add(bin);
        }
        // fill the bins
        for (int i = 0; i < values.length; i++) {
            int binIndex = bins - 1;
            if (values[i] < maximum) {
                double fraction = (values[i] - minimum) / (maximum - minimum);
                if (fraction < 0.0) fraction = 0.0;
                binIndex = (int) (fraction * bins);
                // rounding could result in binIndex being equal to bins
                // which will cause an IndexOutOfBoundsException - see bug
                // report 1553088
                if (binIndex >= bins) binIndex = bins - 1;
            }
            Weighted_HistogramBin bin = (Weighted_HistogramBin) binList.get(binIndex);
            bin.incrementCount(weightings[i]);
            totalWeight += weightings[i];
        }
        // generic map for each series
        val map = new LinkedHashMap<>();
        map.put("key", key);
        map.put("bins", binList);
        map.put("values.length", values.length);
        map.put("bin width", binWidth);
        this.list.add(map);
        fireDatasetChanged();
    }

    /**
     * Returns the minimum value in an array of values.
     *
     * @param values the values ({@code null} not permitted and zero-length array not permitted).
     * @return The minimum value.
     */
    private double getMinimum(double[] values) {
        if (values == null || values.length < 1)
            throw new IllegalArgumentException("Null or zero length 'values' argument.");
        double min = Double.MAX_VALUE;
        for (double value : values)
            if (value < min)
                min = value;
        return min;
    }

    /**
     * Returns the maximum value in an array of values.
     *
     * @param values the values ({@code null} not permitted and zero-length array not permitted).
     * @return The maximum value.
     */
    private double getMaximum(double[] values) {
        if (values == null || values.length < 1)
            throw new IllegalArgumentException("Null or zero length 'values' argument.");
        double max = -Double.MAX_VALUE;
        for (double value : values)
            if (value > max)
                max = value;
        return max;
    }

    /**
     * Returns the bins for a series.
     *
     * @param series the series index (in the range {@code 0} to {@code getSeriesCount() - 1}).
     * @return A list of bins.
     * @throws IndexOutOfBoundsException if {@code series} is outside the specified range.
     */
    List getBins(int series) {
        val map = (Map) this.list.get(series);
        return (List) map.get("bins");
    }

    /**
     * Returns the total number of observations for a series.
     *
     * @param series the series index.
     * @return The total.
     */
    private int getTotal(int series) {
        Map map = (Map) this.list.get(series);
        return (Integer) map.get("values.length");
    }

    /**
     * Returns the bin width for a series.
     *
     * @param series the series index (zero based).
     * @return The bin width.
     */
    private double getBinWidth(int series) {
        Map map = (Map) this.list.get(series);
        return (Double) map.get("bin width");
    }

    /**
     * Returns the number of series in the dataset.
     *
     * @return The series count.
     */
    @Override
    public int getSeriesCount() {
        return this.list.size();
    }

    /**
     * Returns the key for a series.
     *
     * @param series the series index (in the range {@code 0} to {@code getSeriesCount() - 1}).
     * @return The series key.
     * @throws IndexOutOfBoundsException if {@code series} is outside the specified range.
     */
    @Override
    public Comparable getSeriesKey(int series) {
        Map map = (Map) this.list.get(series);
        return (Comparable) map.get("key");
    }

    /**
     * Returns the number of data items for a series.
     *
     * @param series the series index (in the range {@code 0} to {@code getSeriesCount() - 1}).
     * @return The item count.
     * @throws IndexOutOfBoundsException if {@code series} is outside the specified range.
     */
    @Override
    public int getItemCount(int series) {
        return getBins(series).size();
    }

    /**
     * Returns the X value for a bin.  This value won't be used for plotting
     * histograms, since the renderer will ignore it.  But other renderers can
     * use it (for example, you could use the dataset to create a line
     * chart).
     *
     * @param series the series index (in the range {@code 0} to {@code getSeriesCount() - 1}).
     * @param item   the item index (zero based).
     * @return The start value.
     * @throws IndexOutOfBoundsException if {@code series} is outside the specified range.
     */
    @Override
    public Number getX(int series, int item) {
        val bins = getBins(series);
        Weighted_HistogramBin bin = (Weighted_HistogramBin) bins.get(item);
        return (bin.getStartBoundary() + bin.getEndBoundary()) / 2.;
    }

    /**
     * Returns the y-value for a bin (calculated to take into account the histogram type).
     *
     * @param series the series index (in the range {@code 0} to {@code getSeriesCount() - 1}).
     * @param item   the item index (zero based).
     * @return The y-value.
     * @throws IndexOutOfBoundsException if {@code series} is outside the specified range.
     */
    @Override
    public Number getY(int series, int item) {
        val bins = getBins(series);
        val bin = (Weighted_HistogramBin) bins.get(item);
        val total = getTotal(series);
        val binWidth = getBinWidth(series);

        if (this.type == HistogramType.FREQUENCY) {
            return bin.getCount();
        } else if (this.type == HistogramType.RELATIVE_FREQUENCY) {
            return bin.getCount() / total;
        } else if (this.type == HistogramType.SCALE_AREA_TO_1) {
            return bin.getCount() / (binWidth * total);
        } else { // pretty sure this shouldn't ever happen
            throw new IllegalStateException();
        }
    }

    /**
     * Returns the start value for a bin.
     *
     * @param series the series index (in the range {@code 0} to {@code getSeriesCount() - 1}).
     * @param item   the item index (zero based).
     * @return The start value.
     * @throws IndexOutOfBoundsException if {@code series} is outside the specified range.
     */
    @Override
    public Number getStartX(int series, int item) {
        val bins = getBins(series);
        Weighted_HistogramBin bin = (Weighted_HistogramBin) bins.get(item);
        return bin.getStartBoundary();
    }

    /**
     * Returns the end value for a bin.
     *
     * @param series the series index (in the range {@code 0} to {@code getSeriesCount() - 1}).
     * @param item   the item index (zero based).
     * @return The end value.
     * @throws IndexOutOfBoundsException if {@code series} is outside the specified range.
     */
    @Override
    public Number getEndX(int series, int item) {
        val bins = getBins(series);
        Weighted_HistogramBin bin = (Weighted_HistogramBin) bins.get(item);
        return bin.getEndBoundary();
    }

    /**
     * Returns the start y-value for a bin (which is the same as the y-value, this method exists only to support the
     * general form of the {@link IntervalXYDataset} interface).
     *
     * @param series the series index (in the range {@code 0} to {@code getSeriesCount() - 1}).
     * @param item   the item index (zero based).
     * @return The y-value.
     * @throws IndexOutOfBoundsException if {@code series} is outside the specified range.
     */
    @Override
    public Number getStartY(int series, int item) {
        return getY(series, item);
    }

    /**
     * Returns the end y-value for a bin (which is the same as the y-value, this method exists only to support the
     * general form of the {@link IntervalXYDataset} interface).
     *
     * @param series the series index (in the range {@code 0} to {@code getSeriesCount() - 1}).
     * @param item   the item index (zero based).
     * @return The Y value.
     * @throws IndexOutOfBoundsException if {@code series} is outside the specified range.
     */
    @Override
    public Number getEndY(int series, int item) {
        return getY(series, item);
    }

    /**
     * Tests this dataset for equality with an arbitrary object.
     *
     * @param obj the object to test against ({@code null} permitted).
     * @return A boolean.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Weighted_HistogramDataset that)) return false;
        if (!Objects.equals(this.type, that.type)) return false;
        return Objects.equals(this.list, that.list);
    }

    /**
     * Returns a clone of the dataset.
     *
     * @return A clone of the dataset.
     * @throws CloneNotSupportedException if the object cannot be cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        val clone = (Weighted_HistogramDataset) super.clone();
        val seriesCount = getSeriesCount();
        clone.list = new ArrayList<>(seriesCount);
        for (int i = 0; i < seriesCount; i++) {
            clone.list.add(new LinkedHashMap((Map) this.list.get(i)));
        }
        return clone;
    }
}
