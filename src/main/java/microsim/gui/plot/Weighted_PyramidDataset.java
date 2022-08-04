package microsim.gui.plot;

import lombok.val;
import microsim.gui.plot.Weighted_PyramidPlotter.GroupName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractSeriesDataset;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A weighted dataset that can be used for creating weighted pyramids.
 */
public class Weighted_PyramidDataset extends AbstractSeriesDataset implements CategoryDataset, Cloneable,
        PublicCloneable, Serializable {

    @Serial
    private static final long serialVersionUID = -6875925093485823495L;

    private final Map<String, Map<GroupName, Double>> dataMap;
    private final double[][] groupRanges;
    private final GroupName[] groupNames;
    private final double scalingFactor;

    /**
     * Creates a new dataset using the provided groupNames and
     * groupRanges to build a HashMap of total group weight.
     * The weights are adjusted by the provided scalingFactor.
     *
     * @param groupNames    the names of each group to be generated ({@code null} not permitted).
     * @param groupRanges   the ranges of each group to be generated ({@code null} not permitted).
     * @param scalingFactor the scaling factor for the weights ({@code null} not permitted).
     */
    public Weighted_PyramidDataset(@NotNull GroupName[] groupNames, double[] @NotNull [] groupRanges,
                                   double scalingFactor) {
        Args.nullNotPermitted(groupNames, "groupNames");
        Args.nullNotPermitted(groupRanges, "groupRanges");
        this.dataMap = new HashMap<>();
        this.groupNames = groupNames;
        this.groupRanges = groupRanges;
        this.scalingFactor = scalingFactor;
    }

    /**
     * Adds the couple of series to the dataMap. Each value is assigned to a group when it matches the group's min/max
     * limits.
     *
     * @param keys       the series key ({@code null} not permitted).
     * @param values     the raw observations. ({@code null} not permitted).
     * @param weightings the weights associated with the values, i.e. weight {@code i} indicates the number of times the
     *                   value {@code i} appears ({@code null} not permitted).
     */
    public void addSeries(@NotNull String[] keys, double[] @NotNull [] values, double[] @NotNull [] weightings) {
        Args.nullNotPermitted(keys, "key");
        Args.nullNotPermitted(values, "values");
        Args.nullNotPermitted(weightings, "weightings");
        if (values.length != 2 || weightings.length != 2)
            throw new IllegalArgumentException("You must provide a pair of series!");
        if (values[0].length != weightings[0].length || values[1].length != weightings[1].length)
            throw new IllegalArgumentException(
                    "The length of weightings array must be the same as the values array for each series!");

        // Create and add the two series to the dataMap
        for (int s = 0; s < 2; s++) {
            // for each series create a new bucket to store the variable sums
            Map<GroupName, Double> bucket = new HashMap<>();

            for (int v = 0; v < values[s].length; v++) {    // for each value
                for (int g = 0; g < this.groupNames.length; g++) {    // for each group
                    // if the value matches the group, add to the correct bucket element
                    if (values[s][v] >= this.groupRanges[g][0] && values[s][v] <= this.groupRanges[g][1]) {
                        // if the element does not exist, create it
                        if (!bucket.containsKey(this.groupNames[g])) bucket.put(this.groupNames[g], 0.);
                        // multiply the weight by the scaling factor and add to the existing sum (negate if this is the left side),
                        val sf = s == 1 ? scalingFactor : -scalingFactor;
                        bucket.put(this.groupNames[g], bucket.get(this.groupNames[g]) + weightings[s][v] * sf);
                        // do not check any more groups for this value
                        break;
                    }
                }
            }
            // store the series bucket
            dataMap.put(keys[s], bucket);
        }
    }

    /**
     * Returns the minimum value in an array of values.
     *
     * @param values the values ({@code null} not permitted and zero-length array not permitted).
     * @return The minimum value.
     */
    private double getMinimum(double @Nullable [] values) {
        if (values == null || values.length < 1)
            throw new IllegalArgumentException("Null or zero length 'values' argument.");
        double min = Double.MAX_VALUE;
        for (double value : values) if (value < min) min = value;
        return min;
    }

    /**
     * Returns the maximum value in an array of values.
     *
     * @param values the values ({@code null} not permitted and zero-length array not permitted).
     * @return The maximum value.
     */
    private double getMaximum(double @Nullable [] values) {
        if (values == null || values.length < 1)
            throw new IllegalArgumentException("Null or zero length 'values' argument.");
        double max = -Double.MAX_VALUE;
        for (double value : values) if (value > max) max = value;
        return max;
    }

    /**
     * Tests this dataset for equality with an arbitrary object.
     *
     * @param obj the object to test against ({@code null} permitted).
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Weighted_PyramidDataset;
    }


    /**
     * Returns a clone of the dataset.
     *
     * @return A clone of the dataset.
     * @throws CloneNotSupportedException if the object cannot be cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    public double[][] getDataArray() {
        double[][] data = new double[dataMap.keySet().size()][groupNames.length];
        int i = 0;
        for (Map<GroupName, Double> v : dataMap.values()) {
            int j = 0;
            for (GroupName entry : groupNames)
                // make sure that if either side of the dataset is missing a value, this is filled with 0
                data[i][j++] = v.containsKey(entry) ? v.get(entry) : 0;
            i++;
        }

        return data;
    }

    @Override
    public List<Object> getColumnKeys() {
        return Arrays.asList(this.groupNames);
    }

    @Override
    public Comparable<GroupName> getColumnKey(int column) {
        return this.groupNames[column];
    }

    public String[] getSeriesKeys() {
        return dataMap.keySet().toArray(new String[]{});
    }


    @Override
    public Comparable<Object> getRowKey(int row) {
        return null;
    }

    @Override
    public int getRowIndex(Comparable key) {
        return 0;
    }

    @Override
    public List<Object> getRowKeys() {
        return null;
    }

    @Override
    public int getColumnIndex(Comparable key) {
        return 0;
    }


    @Override
    public Number getValue(Comparable rowKey, Comparable columnKey) {
        return null;
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public Number getValue(int row, int column) {
        return null;
    }

    @Override
    public void addChangeListener(DatasetChangeListener listener) {
    }

    @Override
    public void removeChangeListener(DatasetChangeListener listener) {
    }

    @Override
    public DatasetGroup getGroup() {
        return null;
    }

    @Override
    public void setGroup(DatasetGroup group) {
    }

    @Override
    public int getSeriesCount() {
        return 0;
    }

    @Override
    public Comparable<Object> getSeriesKey(int series) {
        return null;
    }
}
