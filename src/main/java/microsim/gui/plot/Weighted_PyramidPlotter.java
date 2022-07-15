package microsim.gui.plot;

import lombok.val;
import microsim.event.CommonEventType;
import microsim.event.EventListener;
import microsim.statistics.UpdatableSource;
import microsim.statistics.weighted.WeightedDoubleArraySource;
import microsim.statistics.weighted.WeightedFloatArraySource;
import microsim.statistics.weighted.WeightedIntArraySource;
import microsim.statistics.weighted.WeightedLongArraySource;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.general.DatasetUtils;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.text.DecimalFormat;
import java.util.Arrays;

import static java.lang.StrictMath.*;

/**
 * A {@link Weighted_PyramidPlotter} is able to display a pyramid using two weighted cross-sections of a variable (e.g.
 * age of males/females for a population pyramid). It can be updated during the simulation. It is based on JFreeChart
 * library and uses data sources based on the microsim.statistics.weighted* interfaces. Note that the weights are taken
 * into account by adding the weight to the count of each group. Groups can be optionally provided by the caller.
 */
public class Weighted_PyramidPlotter extends JInternalFrame implements EventListener {


    @Serial
    private static final long serialVersionUID = 1L;
    private static final int MAXIMUM_VISIBLE_CATEGORIES = 20;
    public static String DEFAULT_TITLE = "Population Chart";
    public static String DEFAULT_XAXIS = "Age Group";
    public static String DEFAULT_YAXIS = "Population";
    public static String DEFAULT_LEFT_CAT = "Males";
    public static String DEFAULT_RIGHT_CAT = "Females";
    public static Boolean DEFAULT_REVERSE_ORDER = false;
    public static String DEFAULT_YAXIS_FORMAT = "#.##";
    private final String[] catNames = new String[2];
    private JFreeChart chart;
    private WeightedArraySource[] sources;
    private String xaxis;
    private String yaxis;
    private String yaxisFormat = DEFAULT_YAXIS_FORMAT;
    private GroupName[] groupNames;

    private double[][] groupRanges;    // These need to be doubles for the DatasetUtilities.createCategoryDataset method

    private double scalingFactor;    // This scales the sample (e.g. to the whole population) 


    /**
     * Constructor for pyramid objects, showing only the latest data as time moves forward.
     * Default values are used for all parameters: title, x-axis, y-axis, category names, age group names/ranges, reverseOrder
     * It generates one age group per unique age, whose title is that age.
     */
    public Weighted_PyramidPlotter() {
        this(DEFAULT_TITLE, DEFAULT_XAXIS, DEFAULT_YAXIS, DEFAULT_LEFT_CAT, DEFAULT_RIGHT_CAT);
    }

    /**
     * Constructor for pyramid objects, showing only the latest data as time moves forward.
     * Default values are used for the following parameters: x-axis, y-axis, category names, age group names/ranges, reverseOrder
     * It generates one age group per unique age, whose title is that age.
     *
     * @param title - title of the chart
     */
    public Weighted_PyramidPlotter(String title) {
        this(title, DEFAULT_XAXIS, DEFAULT_YAXIS, DEFAULT_LEFT_CAT, DEFAULT_RIGHT_CAT);
    }

    /**
     * Constructor for pyramid objects, showing only the latest data as time moves forward. Default values are used for
     * the following parameters: category names, age group names/ranges, reverseOrder. It generates one age group per
     * unique age, whose title is that age.
     *
     * @param title Title of the chart
     * @param xaxis Name of the x-axis
     * @param yaxis Name of the y-axis
     */
    public Weighted_PyramidPlotter(String title, String xaxis, String yaxis) {
        this(title, xaxis, yaxis, DEFAULT_LEFT_CAT, DEFAULT_RIGHT_CAT);
    }


    /**
     * Constructor for pyramid objects, showing only the latest data as time moves forward. Default values are used for
     * the following parameters: age group names/ranges, reverseOrder. It generates one age group per unique age, whose
     * title is that age.
     *
     * @param title    Title of the chart
     * @param xaxis    Name of the x-axis
     * @param yaxis    Name of the y-axis
     * @param leftCat  The name of the left category
     * @param rightCat The name of the right category
     */
    public Weighted_PyramidPlotter(String title, String xaxis, String yaxis, String leftCat, String rightCat) {
        // fix the titles and prepare the plotter, leaving the groups null
        fixTitles(title, xaxis, yaxis, leftCat, rightCat);
        preparePlotter();
    }

    /**
     * Constructor for pyramid objects, showing only the latest data as time moves forward. It generates groups names
     * and ranges using the start/end/step values provided. Default values are used for the following parameters:
     * x-axis, y-axis, category names, age group names/ranges, reverseOrder
     *
     * @param start The minimum accepted value in groups
     * @param end   The maximum accepted value in groups
     * @param step  The step used to separate value into groups
     */
    public Weighted_PyramidPlotter(int start, int end, int step) {
        this(DEFAULT_TITLE, DEFAULT_XAXIS, DEFAULT_YAXIS, DEFAULT_LEFT_CAT, DEFAULT_RIGHT_CAT, start, end, step,
                DEFAULT_REVERSE_ORDER, DEFAULT_YAXIS_FORMAT);
    }

    /**
     * Constructor for pyramid objects, showing only the latest data as time moves forward. It generates groups names
     * and ranges using the start/end/step and order values provided. Default values are used for the following
     * parameters: x-axis, y-axis, category names, age group names/ranges
     *
     * @param start        The minimum accepted value in groups
     * @param end          The maximum accepted value in groups
     * @param step         The step used to separate value into groups
     * @param reverseOrder Tf true, it will reverse the groups
     */
    public Weighted_PyramidPlotter(int start, int end, int step, Boolean reverseOrder) {
        this(DEFAULT_TITLE, DEFAULT_XAXIS, DEFAULT_YAXIS, DEFAULT_LEFT_CAT, DEFAULT_RIGHT_CAT, start, end, step,
                reverseOrder, DEFAULT_YAXIS_FORMAT);
    }

    /**
     * Constructor for pyramid objects, showing only the latest data as time moves forward. It generates groups names
     * and ranges using the start/end/step values provided. Descending order is used by default.
     *
     * @param title        Title of the chart
     * @param xaxis        Name of the x-axis
     * @param yaxis        Name of the y-axis
     * @param leftCat      The name of the left category
     * @param rightCat     The name of the right category
     * @param start        The minimum accepted value in groups
     * @param end          The maximum accepted value in groups
     * @param step         The step used to separate value into groups
     * @param reverseOrder If true, it will reverse the groups
     */
    public Weighted_PyramidPlotter(String title, String xaxis, String yaxis, String leftCat, String rightCat, int start,
                                   int end, int step, Boolean reverseOrder, String format) {
        if (step == 0) return;
        fixTitles(title, xaxis, yaxis, leftCat, rightCat);
        yaxisFormat = format;

        // Create the groups based on the range, and save them to "this" 
        GroupDetails gd = makeGroupsFromRange(start, end, step, reverseOrder, format);
        this.groupNames = gd.groupNames;
        this.groupRanges = gd.groupRanges;

        preparePlotter();
    }

    /**
     * Constructor for pyramid objects, showing only the latest data as time moves forward. It generates groups based on
     * the names and ranges provided. Default values are used for the following parameters: title, x-axis, y-axis, category names
     *
     * @param groupNames  An array of the name of each group
     * @param groupRanges An array of the min/max values of each group
     */
    public Weighted_PyramidPlotter(String[] groupNames, double[][] groupRanges) {
        this(DEFAULT_TITLE, DEFAULT_XAXIS, DEFAULT_YAXIS, DEFAULT_LEFT_CAT, DEFAULT_RIGHT_CAT, groupNames, groupRanges,
                DEFAULT_YAXIS_FORMAT);
    }

    /**
     * Constructor for pyramid objects, showing only the latest data as time moves forward. It generates groups based on
     * the names and ranges provided.
     *
     * @param title       Title of the chart
     * @param xaxis       Name of the x-axis
     * @param yaxis       Name of the y-axis
     * @param leftCat     The name of the left category
     * @param rightCat    The name of the right category
     * @param groupNames  An array of the name of each group
     * @param groupRanges An array of the min/max values of each group
     */
    public Weighted_PyramidPlotter(String title, String xaxis, String yaxis, String leftCat, String rightCat,
                                   String[] groupNames, double[][] groupRanges, String format) {
        fixTitles(title, xaxis, yaxis, leftCat, rightCat);
        yaxisFormat = format;

        // Fix names
        this.groupNames = groupNames == null ? null : getGroupNamesFromStrings(groupNames);
        this.groupRanges = groupRanges;

        preparePlotter();
    }

    private static GroupName[] getGroupNamesFromStrings(String @NotNull [] groupStrings) {
        GroupName[] groupNames = new GroupName[groupStrings.length];

        int stepShow = (int) Math.ceil((double) groupStrings.length / (double) MAXIMUM_VISIBLE_CATEGORIES);

        // Show only every Nth string and always the first & last
        for (int i = 0; i < groupStrings.length; i++)
            groupNames[i] = new GroupName(groupStrings[i], (i % stepShow == 0 || i == groupStrings.length - 1));

        return groupNames;
    }

    // The function that prepares the titles
    private void fixTitles(String title, String xaxis, String yaxis, String leftCat, String rightCat) {
        this.setTitle(title);
        this.xaxis = xaxis;
        this.yaxis = yaxis;
        this.catNames[0] = leftCat;
        this.catNames[1] = rightCat;
    }

    // the function that calculates groups from a range
    private GroupDetails makeGroupsFromRange(int start, int end, int step, Boolean reverseOrder, String format) {
        // First we calculate the optimal (visually at least!) number of groups, so that   
        // the last group ends with "max" and its size is "(0.5 * step) < size < (1.5*step)"
        int noOfGroups = (int) max(round((double) (end - start) / (double) step) + (abs(step) == 1 ? 1 : 0), 1);
        // *Note: should we enforce equal groups sizes? 

        // Then, if required, we reverse the order
        if (reverseOrder) {
            int temp = start;
            start = end;
            end = temp;
            step = -step;
        }

        // Then we calculate the group ranges & names
        val groupNames = new String[noOfGroups];
        val groupRanges = new double[noOfGroups][2];

        // asc checks whether we are ascending or descending
        val asc = start <= end;
        for (int i = 0; i < noOfGroups; i++) {
            // The range needs to always be stored in ascending order, hence the extended use of "asc" here. Sorry! :)
            // <from> is calculated based on the current step value
            groupRanges[i][asc ? 0 : 1] = start + i * step;
            // <to> is equal to the next group's  "<from> - 1", but for the last group it is equal to "end"
            groupRanges[i][asc ? 1 : 0] = (i == noOfGroups - 1) ? end : (start + (i + 1) * step) - (asc ? 1 : -1);
            // for the name, if step=1 use the step value, else show as "from - to" (inclusive)
            val f = new DecimalFormat(format);
            groupNames[i] = groupRanges[i][0] == groupRanges[i][1] ?
                    f.format(groupRanges[i][0]) :
                    f.format(groupRanges[i][asc ? 0 : 1]) + " - " + f.format(groupRanges[i][asc ? 1 : 0]);
        }

        return new GroupDetails(getGroupNamesFromStrings(groupNames), groupRanges);
    }

    private void preparePlotter() {
        this.setResizable(true);
        sources = new WeightedArraySource[2];

        chart = ChartFactory.createStackedBarChart(
                title,      // chart title
                this.xaxis,                      // x axis label
                this.yaxis,                      // y axis label
                DatasetUtils.createCategoryDataset(this.catNames, new String[]{""}, new double[][]{{0}, {0}}),
                PlotOrientation.HORIZONTAL,
                true,         // include legend
                true,
                true
        );

        setChartProperties();

        chart.getCategoryPlot().getRangeAxis().setVisible(false);

        final ChartPanel chartPanel = new ChartPanel(chart);

        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        setContentPane(chartPanel);

        this.setSize(400, 400);
    }


    public void onEvent(Enum<?> type) {
        if (type instanceof CommonEventType && type.equals(CommonEventType.Update)) {
            update();
        }
    }

    // This function generates a new chart based on the latest data  
    public void update() {
        if (sources.length != 2 || catNames.length != 2) return;
        GroupName[] groupNames;
        double[][] groupRanges;

        // Get the source data
        WeightedArraySource leftData = sources[0];
        WeightedArraySource rightData = sources[1];
        val vals = new double[][]{leftData.getDoubleArray(), rightData.getDoubleArray()};
        val weights = new double[][]{leftData.getWeights(), rightData.getWeights()};

        // If there are no groups defined, create one for each age between the min/max found in the data
        // *Note: do we want this done in every repetition, or should we save to "this"? 
        if (this.groupNames == null || this.groupRanges == null) {
            int min = (int) min(Arrays.stream(vals[0]).min().orElse(0), Arrays.stream(vals[1]).min().orElse(0));    // if there is no data, set min to 0
            int max = (int) min(Arrays.stream(vals[0]).max().orElse(100), Arrays.stream(vals[1]).max().orElse(100));    // if there is no data, set max to 100
            // Create the groups based on the range, and save them to the local variables 
            GroupDetails gd = makeGroupsFromRange(min, max, 1, true, yaxisFormat);
            groupNames = gd.groupNames;
            groupRanges = gd.groupRanges;
        } else {
            // else, just use the existing groups
            groupNames = this.groupNames;
            groupRanges = this.groupRanges;
        }

        // Create the dataset and add the data
        Weighted_PyramidDataset dataset = new Weighted_PyramidDataset(groupNames, groupRanges, scalingFactor);
        dataset.addSeries(this.catNames, vals, weights);


        chart = ChartFactory.createStackedBarChart(
                this.title,      // chart title
                this.xaxis,                      // x axis label
                this.yaxis,                      // y axis label
                DatasetUtils.createCategoryDataset(
                        dataset.getSeriesKeys(),
                        groupNames,
                        dataset.getDataArray()),      // data
                PlotOrientation.HORIZONTAL,
                true,         // include legend
                true,
                true
        );

        setChartProperties();

        final ChartPanel chartPanel = new ChartPanel(chart);

        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        setContentPane(chartPanel);

    }

    /**
     * This function sets the default Chart Properties.
     */
    private void setChartProperties() {
        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.white);
        plot.setForegroundAlpha(0.85f);
        plot.setShadowGenerator(null);
        final StackedBarRenderer renderer = new StackedBarRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        plot.setRenderer(renderer);

        // hide the sign for negative numbers in yAxis  
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setNumberFormatOverride(new DecimalFormat("0; 0 "));

    }

    public void setScalingFactor(double scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    /**
     * Add a new series buffer, retrieving value from WeightedDoubleSource objects in a collection.
     *
     * @param source A collection containing the sources.
     */
    public void addCollectionSource(WeightedDoubleArraySource[] source) {
        if (source.length != 2) return;
        if (catNames.length != 2) return;
        sources[0] = new DWeightedArraySource(catNames[0], source[0]);
        sources[1] = new DWeightedArraySource(catNames[1], source[1]);
    }

    /**
     * Add a new series buffer, retrieving value from WeightedFloatSource objects in a collection.
     *
     * @param source A collection containing the sources.
     */
    public void addCollectionSource(WeightedFloatArraySource[] source) {
        if (source.length != 2) return;
        if (catNames.length != 2) return;
        sources[0] = new FWeightedArraySource(catNames[0], source[0]);
        sources[1] = new FWeightedArraySource(catNames[1], source[1]);
    }

    /**
     * Add a new series buffer, retrieving value from WeightedIntArraySource objects in a collection.
     *
     * @param source A collection containing the sources.
     */
    public void addCollectionSource(WeightedIntArraySource[] source) {
        if (source.length != 2) return;
        if (catNames.length != 2) return;
        sources[0] = new IWeightedArraySource(catNames[0], source[0]);
        sources[1] = new IWeightedArraySource(catNames[1], source[1]);
    }

    /**
     * Add a new series buffer, retrieving value from WeightedLongSource objects in a collection.
     *
     * @param source A collection containing the sources.
     */
    public void addCollectionSource(WeightedLongArraySource[] source) {
        if (source.length != 2) return;
        if (catNames.length != 2) return;
        sources[0] = new LWeightedArraySource(catNames[0], source[0]);
        sources[1] = new LWeightedArraySource(catNames[1], source[1]);
    }

    public static class GroupName implements Comparable<GroupName> {
        String value;
        Boolean show;

        GroupName(String val, Boolean sh) {
            value = val;
            show = sh;
        }

        public int compareTo(GroupName key) {
            return value.compareTo(key.value);
        }

        public String toString() {
            return show ? value : "";
        }
    }

    private static class GroupDetails {
        public GroupName[] groupNames;
        public double[][] groupRanges;

        public GroupDetails(GroupName[] groupNames, double[][] groupRanges) {
            this.groupNames = groupNames;
            this.groupRanges = groupRanges;
        }
    }

    private abstract static class WeightedArraySource {
        public String label;
        protected boolean isUpdatable;

        public abstract double[] getDoubleArray();

        public abstract double[] getWeights();
    }

    private static class DWeightedArraySource extends WeightedArraySource {
        public WeightedDoubleArraySource source;

        public DWeightedArraySource(String label, WeightedDoubleArraySource source) {
            super.label = label;
            this.source = source;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double[] getDoubleArray() {
            if (isUpdatable) ((UpdatableSource) source).updateSource();
            return source.getDoubleArray();
        }

        @Override
        public double[] getWeights() {
            return source.getWeights();
        }
    }

    private static class FWeightedArraySource extends WeightedArraySource {
        public WeightedFloatArraySource source;

        public FWeightedArraySource(String label, WeightedFloatArraySource source) {
            super.label = label;
            this.source = source;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double[] getDoubleArray() {
            if (isUpdatable) ((UpdatableSource) source).updateSource();
            val array = source.getFloatArray();
            val output = new double[array.length];
            for (int i = 0; i < array.length; i++) output[i] = array[i];

            return output;
        }

        @Override
        public double[] getWeights() {
            return source.getWeights();
        }
    }

    private static class IWeightedArraySource extends WeightedArraySource {
        public WeightedIntArraySource source;

        public IWeightedArraySource(String label, WeightedIntArraySource source) {
            super.label = label;
            this.source = source;
            isUpdatable = (source instanceof UpdatableSource);
        }

        /*
         * (non-Javadoc)
         *
         * @see jas.plot.TimePlot.Source#getDouble()
         */
        public double[] getDoubleArray() {
            if (isUpdatable) ((UpdatableSource) source).updateSource();
            val array = source.getIntArray();
            val output = new double[array.length];
            for (int i = 0; i < array.length; i++) output[i] = array[i];

            return output;
        }

        @Override
        public double[] getWeights() {
            return source.getWeights();
        }
    }

    private static class LWeightedArraySource extends WeightedArraySource {
        public WeightedLongArraySource source;

        public LWeightedArraySource(String label, WeightedLongArraySource source) {
            super.label = label;
            this.source = source;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double[] getDoubleArray() {
            if (isUpdatable) ((UpdatableSource) source).updateSource();
            val array = source.getLongArray();
            val output = new double[array.length];
            for (int i = 0; i < array.length; i++) output[i] = array[i];// fixme memcpy

            return output;
        }

        @Override
        public double[] getWeights() {
            return source.getWeights();
        }
    }

}
