package microsim.gui.plot;

import microsim.event.CommonEventType;
import microsim.event.EventListener;
import microsim.reflection.ReflectionUtils;
import microsim.statistics.*;
import microsim.statistics.reflectors.DoubleInvoker;
import microsim.statistics.reflectors.FloatInvoker;
import microsim.statistics.reflectors.IntegerInvoker;
import microsim.statistics.reflectors.LongInvoker;
import org.apache.commons.math3.util.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;

/**
 * A ScatterplotSimulationPlotter is able to trace one or more pairs of data sources over time, creating a scatterplot
 * chart. It is based on JFreeChart library and uses data sources based on the microsim.statistics.* interfaces.<br>
 */
public class ScatterplotSimulationPlotter extends JInternalFrame implements EventListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ArrayList<Pair<Source, Source>> sources;

    private final XYSeriesCollection dataset;

    private int maxSamples;


    /**
     * Constructor for scatterplot chart objects with chart legend displayed by default and all data samples shown,
     * accumulating as time moves forward. If it is desired to turn the legend off, or set a limit to the number of
     * previous time-steps of data displayed in the chart, use the constructor
     * {@link #ScatterplotSimulationPlotter(String, String, String, boolean, int)}.
     *
     * @param title Title of the chart
     * @param xaxis Name of the x-axis
     * @param yaxis Name of the y-axis
     */
    public ScatterplotSimulationPlotter(String title, String xaxis, String yaxis) {
        //Includes legend by default and will accumulate data samples by default (if wanting only the most recent data points, use the other constructor)
        this(title, xaxis, yaxis, true, 0);
    }

    /**
     * Constructor for scatterplot chart objects, featuring a toggle to hide the chart legend
     * and to set the number of previous time-steps of data to display in the chart.
     *
     * @param title         Title of the chart
     * @param xaxis         Name of the x-axis
     * @param yaxis         Name of the y-axis
     * @param includeLegend Toggles whether to include the legend. If displaying a very large number of different series
     *                      in the chart, it may be useful to turn the legend off as it will occupy a lot of space in
     *                      the GUI.
     * @param maxSamples    The number of {@code snapshots} of data displayed in the chart. Only data from the last
     *                      {@code maxSamples} updates will be displayed in the chart,so if the chart is updated at each
     *                      {@code time-step}, then only the most recent {@code maxSamples} time-steps will be shown on
     *                      the chart. If the user wishes to accumulate all data points from the simulation run, i.e. to
     *                      display all available data from all previous time-steps, set this to 0.
     */
    public ScatterplotSimulationPlotter(String title, String xaxis, String yaxis, boolean includeLegend,
                                        int maxSamples) {
        //Can specify whether to include legend and how many samples (updates) to display
        super();
        this.setResizable(true);
        this.setTitle(title);
        this.maxSamples = maxSamples;

        sources = new ArrayList<>();

        dataset = new XYSeriesCollection();

        final JFreeChart chart = ChartFactory.createScatterPlot(
                title,      // chart title
                xaxis,                      // x axis label
                yaxis,                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                includeLegend,           // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        final XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true);   // Shapes only
        plot.setRenderer(renderer);

        final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());

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

    public void update() {
        double x, y;
        for (int i = 0; i < sources.size(); i++) {
            Source source_X = sources.get(i).getFirst();
            Source source_Y = sources.get(i).getSecond();
            XYSeries series = dataset.getSeries(i);
            x = source_X.getDouble();
            y = source_Y.getDouble();
            series.add(x, y);
        }
    }

    /**
     * Build a series of paired values, retrieving data from two DoubleSource objects, using the default variableId.
     *
     * @param legend            The legend name of the series.
     * @param plottableObject_X The data source object implementing the DoubleSource interface to produce values for
     *                          the x-axis (domain).
     * @param plottableObject_Y The data source object implementing the DoubleSource interface to produce values for
     *                          the y-axis (range).
     */
    public void addSeries(String legend, DoubleSource plottableObject_X, DoubleSource plottableObject_Y) {
        DSource sourceX = new DSource(legend, plottableObject_X, DoubleSource.Variables.Default);
        DSource sourceY = new DSource(legend, plottableObject_Y, DoubleSource.Variables.Default);
        sources.add(new Pair<>(sourceX, sourceY));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series of paired values, retrieving data from two DoubleSource objects.
     *
     * @param legend            The legend name of the series.
     * @param plottableObject_X The data source object implementing the DoubleSource interface producing values of the
     *                          x-axis (domain).
     * @param variableID_X      The variable id of the source object producing values of the x-axis (domain).
     * @param plottableObject_Y The data source object implementing the DoubleSource interface producing values of the
     *                          y-axis (range).
     * @param variableID_Y      The variable id of the source object producing values of the y-axis (range).
     */
    public void addSeries(String legend, DoubleSource plottableObject_X, Enum<?> variableID_X,
                          DoubleSource plottableObject_Y, Enum<?> variableID_Y) {
        DSource sourceX = new DSource(legend, plottableObject_X, variableID_X);
        DSource sourceY = new DSource(legend, plottableObject_Y, variableID_Y);
        sources.add(new Pair<>(sourceX, sourceY));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series of paired values from two FloatSource objects, using the default variableId.
     *
     * @param legend            The legend name of the series.
     * @param plottableObject_X The data source object implementing the FloatSource interface to produce values for the
     *                          x-axis (domain).
     * @param plottableObject_Y The data source object implementing the FloatSource interface to produce values for the
     *                          y-axis (range).
     */
    public void addSeries(String legend, FloatSource plottableObject_X, FloatSource plottableObject_Y) {
        FSource sourceX = new FSource(legend, plottableObject_X, FloatSource.Variables.Default);
        FSource sourceY = new FSource(legend, plottableObject_Y, FloatSource.Variables.Default);
        sources.add(new Pair<>(sourceX, sourceY));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series of paired values from two FloatSource objects.
     *
     * @param legend            The legend name of the series.
     * @param plottableObject_X The data source object implementing the FloatSource interface producing values of the
     *                          x-axis (domain).
     * @param variableID_X      The variable id of the source object producing values of the x-axis (domain).
     * @param plottableObject_Y The data source object implementing the FloatSource interface producing values of the
     *                          y-axis (range).
     * @param variableID_Y      The variable id of the source object producing values of the y-axis (range).
     */
    public void addSeries(String legend, FloatSource plottableObject_X, Enum<?> variableID_X,
                          FloatSource plottableObject_Y, Enum<?> variableID_Y) {
        FSource sourceX = new FSource(legend, plottableObject_X, variableID_X);
        FSource sourceY = new FSource(legend, plottableObject_Y, variableID_Y);
        sources.add(new Pair<>(sourceX, sourceY));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series of paired values from two LongSource objects, using the default variableId.
     *
     * @param legend            The legend name of the series.
     * @param plottableObject_X The data source object implementing the LongSource interface producing values of the
     *                          x-axis (domain).
     * @param plottableObject_Y The data source object implementing the LongSource interface producing values of the
     *                          y-axis (range).
     */
    public void addSeries(String legend, LongSource plottableObject_X, LongSource plottableObject_Y) {
        LSource sourceX = new LSource(legend, plottableObject_X, LongSource.Variables.Default);
        LSource sourceY = new LSource(legend, plottableObject_Y, LongSource.Variables.Default);
        sources.add(new Pair<>(sourceX, sourceY));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series of paired values from two LongSource objects
     *
     * @param legend            The legend name of the series.
     * @param plottableObject_X The data source object implementing the LongSource interface producing values of the
     *                          x-axis (domain).
     * @param variableID_X      The variable id of the source object producing values of the x-axis (domain).
     * @param plottableObject_Y The data source object implementing the LongSource interface producing values of the
     *                          y-axis (range).
     * @param variableID_Y      The variable id of the source object producing values of the y-axis (range).
     */
    public void addSeries(String legend, LongSource plottableObject_X, Enum<?> variableID_X,
                          LongSource plottableObject_Y, Enum<?> variableID_Y) {
        LSource sourceX = new LSource(legend, plottableObject_X, variableID_X);
        LSource sourceY = new LSource(legend, plottableObject_Y, variableID_Y);
        sources.add(new Pair<>(sourceX, sourceY));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series of paired values from two IntSource objects, using the default variableId.
     *
     * @param legend            The legend name of the series.
     * @param plottableObject_X The data source object implementing the IntSource interface producing values of the
     *                          x-axis (domain).
     * @param plottableObject_Y The data source object implementing the IntSource interface producing values of the
     *                          y-axis (range).
     */
    public void addSeries(String legend, IntSource plottableObject_X, IntSource plottableObject_Y) {
        ISource sourceX = new ISource(legend, plottableObject_X, IntSource.Variables.Default);
        ISource sourceY = new ISource(legend, plottableObject_Y, IntSource.Variables.Default);
        sources.add(new Pair<>(sourceX, sourceY));

        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series of paired values from two IntSource objects.
     *
     * @param legend            The legend name of the series.
     * @param plottableObject_X The data source object implementing the IntSource interface producing values of the
     *                          x-axis (domain).
     * @param variableID_X      The variable id of the source object producing values of the x-axis (domain).
     * @param plottableObject_Y The data source object implementing the IntSource interface  producing values of the
     *                          y-axis (range).
     * @param variableID_Y      The variable id of the source object producing values of the y-axis (range).
     */
    public void addSeries(String legend, IntSource plottableObject_X, Enum<?> variableID_X,
                          IntSource plottableObject_Y, Enum<?> variableID_Y) {
        ISource sourceX = new ISource(legend, plottableObject_X, variableID_X);
        ISource sourceY = new ISource(legend, plottableObject_Y, variableID_Y);
        sources.add(new Pair<>(sourceX, sourceY));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series of paired values from two generic objects.
     *
     * @param legend          The legend name of the series.
     * @param target_X        The data source object for x-axis values (domain).
     * @param variableName_X  The variable or method name of the source object producing values for the x-axis (domain).
     * @param getFromMethod_X Specifies if the variableName_X is a field or a method.
     * @param target_Y        The data source object for y-axis values (range).
     * @param variableName_Y  The variable or method name of the source object producing values for the y-axis (range).
     * @param getFromMethod_Y Specifies if the variableName_Y is a field or a method.
     */
    public void addSeries(String legend, Object target_X, String variableName_X, boolean getFromMethod_X,
                          Object target_Y, String variableName_Y, boolean getFromMethod_Y) {

        // First, look at X values
        Source sourceX;
        if (ReflectionUtils.isDoubleSource(target_X.getClass(), variableName_X, getFromMethod_X))
            sourceX = new DSource(legend, new DoubleInvoker(target_X, variableName_X, getFromMethod_X), DoubleSource.Variables.Default);
        else if (ReflectionUtils.isFloatSource(target_X.getClass(), variableName_X, getFromMethod_X))
            sourceX = new FSource(legend, new FloatInvoker(target_X, variableName_X, getFromMethod_X), FloatSource.Variables.Default);
        else if (ReflectionUtils.isIntSource(target_X.getClass(), variableName_X, getFromMethod_X))
            sourceX = new ISource(legend, new IntegerInvoker(target_X, variableName_X, getFromMethod_X), IntSource.Variables.Default);
        else if (ReflectionUtils.isLongSource(target_X.getClass(), variableName_X, getFromMethod_X))
            sourceX = new LSource(legend, new LongInvoker(target_X, variableName_X, getFromMethod_X), LongSource.Variables.Default);
        else throw new IllegalArgumentException("The target_X object " + target_X
                    + " does not provide a value of a valid data type.");

        //Now for Y values
        Source sourceY;
        if (ReflectionUtils.isDoubleSource(target_Y.getClass(), variableName_Y, getFromMethod_Y))
            sourceY = new DSource(legend, new DoubleInvoker(target_Y, variableName_Y, getFromMethod_Y), DoubleSource.Variables.Default);
        else if (ReflectionUtils.isFloatSource(target_Y.getClass(), variableName_Y, getFromMethod_Y))
            sourceY = new FSource(legend, new FloatInvoker(target_Y, variableName_Y, getFromMethod_Y), FloatSource.Variables.Default);
        else if (ReflectionUtils.isIntSource(target_Y.getClass(), variableName_Y, getFromMethod_Y))
            sourceY = new ISource(legend, new IntegerInvoker(target_Y, variableName_Y, getFromMethod_Y), IntSource.Variables.Default);
        else if (ReflectionUtils.isLongSource(target_Y.getClass(), variableName_Y, getFromMethod_Y))
            sourceY = new LSource(legend, new LongInvoker(target_Y, variableName_Y, getFromMethod_Y), LongSource.Variables.Default);
        else throw new IllegalArgumentException("The target_Y object " + target_Y
                    + " does not provide a value of a valid data type.");

        sources.add(new Pair<>(sourceX, sourceY));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series of paired values, retrieving x-axis data from an DoubleSource object and y-axis data from a
     * LongSource object, using the default variableId.
     *
     * @param legend            The legend name of the series.
     * @param plottableObject_X The data source object implementing the DoubleSource interface to produce values for the
     *                          x-axis (domain).
     * @param plottableObject_Y The data source object implementing the LongSource interface to produce values for the
     *                          y-axis (range).
     */
    public void addSeries(String legend, DoubleSource plottableObject_X, LongSource plottableObject_Y) {
        DSource sourceX = new DSource(legend, plottableObject_X, DoubleSource.Variables.Default);
        LSource sourceY = new LSource(legend, plottableObject_Y, LongSource.Variables.Default);
        sources.add(new Pair<>(sourceX, sourceY));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series of paired values, retrieving x-axis data from an DoubleSource object and y-axis data from a
     * LongSource object.
     *
     * @param legend            The legend name of the series.
     * @param plottableObject_X The data source object implementing the DoubleSource interface producing values of the
     *                          x-axis (domain).
     * @param variableID_X      The variable id of the source object producing values of the x-axis (domain).
     * @param plottableObject_Y The data source object implementing the LongSource interface producing values of the
     *                          y-axis (range).
     * @param variableID_Y      The variable id of the source object producing values of the y-axis (range).
     */
    public void addSeries(String legend, DoubleSource plottableObject_X, Enum<?> variableID_X,
                          LongSource plottableObject_Y, Enum<?> variableID_Y) {
        DSource sourceX = new DSource(legend, plottableObject_X, variableID_X);
        LSource sourceY = new LSource(legend, plottableObject_Y, variableID_Y);
        sources.add(new Pair<>(sourceX, sourceY));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series of paired values, retrieving x-axis data from an LongSource object and y-axis data from a
     * DoubleSource object, using the default variableId.
     *
     * @param legend            The legend name of the series.
     * @param plottableObject_X The data source object implementing the LongSource interface to produce values for the
     *                          x-axis (domain).
     * @param plottableObject_Y The data source object implementing the DoubleSource interface to produce values for the
     *                          y-axis (range).
     */
    public void addSeries(String legend, LongSource plottableObject_X, DoubleSource plottableObject_Y) {
        LSource sourceX = new LSource(legend, plottableObject_X, LongSource.Variables.Default);
        DSource sourceY = new DSource(legend, plottableObject_Y, DoubleSource.Variables.Default);
        sources.add(new Pair<>(sourceX, sourceY));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series of paired values, retrieving x-axis data from an LongSource object and y-axis data from a
     * DoubleSource object.
     *
     * @param legend            The legend name of the series.
     * @param plottableObject_X The data source object implementing the LongSource interface producing values of the
     *                          x-axis (domain).
     * @param variableID_X      The variable id of the source object producing values of the x-axis (domain).
     * @param plottableObject_Y The data source object implementing the DoubleSource interface producing values of the
     *                          y-axis (range).
     * @param variableID_Y      The variable id of the source object producing values of the y-axis (range).
     */
    public void addSeries(String legend, LongSource plottableObject_X, Enum<?> variableID_X,
                          DoubleSource plottableObject_Y, Enum<?> variableID_Y) {
        LSource sourceX = new LSource(legend, plottableObject_X, variableID_X);
        DSource sourceY = new DSource(legend, plottableObject_Y, variableID_Y);
        sources.add(new Pair<>(sourceX, sourceY));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Max samples parameters allow to define a maximum number of time-steps used in the scatter plot. When set, the
     * oldest data points are removed as time moves forward to maintain the number of samples (time-steps) in the chart.
     */
    public int getMaxSamples() {
        return maxSamples;
    }

    /**
     * Set the max sample parameter.
     *
     * @param maxSamples Maximum number of time-steps rendered on x axis.
     */
    public void setMaxSamples(int maxSamples) {
        this.maxSamples = maxSamples;
    }

    private abstract static class Source {
        public Enum<?> vId;
        protected boolean isUpdatable;

        public abstract double getDouble();
    }

    private static class DSource extends Source {
        public DoubleSource source;

        public DSource(String label, DoubleSource source, Enum<?> varId) {
            this.source = source;
            super.vId = varId;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double getDouble() {
            if (isUpdatable) ((UpdatableSource) source).updateSource();
            return source.getDoubleValue(vId);
        }
    }

    private static class FSource extends Source {
        public FloatSource source;

        public FSource(String label, FloatSource source, Enum<?> varId) {
            this.source = source;
            super.vId = varId;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double getDouble() {
            if (isUpdatable) ((UpdatableSource) source).updateSource();
            return source.getFloatValue(vId);
        }
    }

    private static class ISource extends Source {
        public IntSource source;

        public ISource(String label, IntSource source, Enum<?> varId) {
            this.source = source;
            super.vId = varId;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double getDouble() {
            if (isUpdatable) ((UpdatableSource) source).updateSource();
            return source.getIntValue(vId);
        }
    }

    private static class LSource extends Source {
        public LongSource source;

        public LSource(String label, LongSource source, Enum<?> varId) {
            this.source = source;
            super.vId = varId;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double getDouble() {
            if (isUpdatable) ((UpdatableSource) source).updateSource();
            return source.getLongValue(vId);
        }
    }

}
