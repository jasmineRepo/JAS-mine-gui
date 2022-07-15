package microsim.gui.plot;

import lombok.val;
import microsim.engine.SimulationEngine;
import microsim.event.CommonEventType;
import microsim.event.EventListener;
import microsim.reflection.ReflectionUtils;
import microsim.statistics.*;
import microsim.statistics.reflectors.DoubleInvoker;
import microsim.statistics.reflectors.FloatInvoker;
import microsim.statistics.reflectors.IntegerInvoker;
import microsim.statistics.reflectors.LongInvoker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.Serial;
import java.util.ArrayList;

/**
 * A time series plotter is able to trace one or more data sources over time. It is based on JFreeChart library and uses
 * data sources based on the microsim.statistics.* interfaces.
 */
public class TimeSeriesSimulationPlotter extends JInternalFrame implements EventListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ArrayList<Source> sources;

    private final XYSeriesCollection dataset;

    private final XYPlot plot;

    private int maxSamples;

    public TimeSeriesSimulationPlotter(String title, String yaxis) {
        //Include legend by default
        this(title, yaxis, true, 0);
    }

    public TimeSeriesSimulationPlotter(String title, String yaxis, boolean includeLegend, int maxSamples) {
        //Can specify whether to include legend
        super();
        this.setResizable(true);
        this.setTitle(title);
        this.maxSamples = maxSamples;

        sources = new ArrayList<>();

        dataset = new XYSeriesCollection();

        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,      // chart title
                "Simulation time",                      // x axis label
                yaxis,                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                includeLegend,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(renderer);


        // change the auto tick unit selection to integer units only...
        val rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        //Ross - made this change to allow units on Y axis for finer ticks, which is especially important for timeseries with values < 1.

        val chartPanel = new ChartPanel(chart);

        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        setContentPane(chartPanel);

        this.setSize(400, 400);
    }


    public void onEvent(Enum<?> type) {
        if (type instanceof CommonEventType && type.equals(CommonEventType.Update)) {
            double d;
            for (int i = 0; i < sources.size(); i++) {
                Source source = sources.get(i);
                XYSeries series = dataset.getSeries(i);
                d = source.getDouble();
                series.add(SimulationEngine.getInstance().getTime(), d);
            }
        }
    }

    /**
     * Build a series retrieving data from a DoubleSource object, using the default variableId.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the DoubleSource interface.
     */
    public void addSeries(String legend, DoubleSource plottableObject) {
        sources.add(new DSource(legend, plottableObject, DoubleSource.Variables.Default));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    public void addSeries(String legend, DoubleSource plottableObject, Color lineColor, boolean shapesFilled, boolean isDashed, Shape shape) {
        sources.add(new DSource(legend, plottableObject, DoubleSource.Variables.Default));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);

        int seriesIndex = dataset.getSeriesIndex(series.getKey()); //Get int Index of series using its key
        Stroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{10.0f}, 0.0f);
        getRenderer().setSeriesPaint(seriesIndex, lineColor); //Set color of the series in the renderer to what was requested
        getRenderer().setSeriesShapesFilled(seriesIndex, shapesFilled); //Set if shapes should be filled or not
        if (isDashed) getRenderer().setSeriesStroke(seriesIndex, dashed);
        getRenderer().setSeriesShape(seriesIndex, shape);
    }

    /**
     * Build a series retrieving data from a DoubleSource object.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the DoubleSource interface.
     * @param variableID      The variable id of the source object.
     */
    public void addSeries(String legend, DoubleSource plottableObject, Enum<?> variableID) {
        sources.add(new DSource(legend, plottableObject, variableID));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    public void addSeries(String legend, DoubleSource plottableObject, Enum<?> variableID, Color lineColor, boolean shapesFilled, boolean isDashed, Shape shape) {
        sources.add(new DSource(legend, plottableObject, variableID));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
        int seriesIndex = dataset.getSeriesIndex(series.getKey()); //Get int Index of series using its key

        Stroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{10.0f}, 0.0f);
        getRenderer().setSeriesPaint(seriesIndex, lineColor); //Set color of the series in the renderer to what was requested
        getRenderer().setSeriesShapesFilled(seriesIndex, shapesFilled); //Set if shapes should be filled or not
        if (isDashed) getRenderer().setSeriesStroke(seriesIndex, dashed);
        getRenderer().setSeriesShape(seriesIndex, shape);

    }

    public void addSeries(String legend, DoubleSource plottableObject, Enum<?> variableID, Color lineColor, boolean validation) {
        if (validation) {
            Shape myRectangle = new Rectangle2D.Float(-3, -3, 6, 6);
            addSeries(legend, plottableObject, variableID, lineColor, false, true, myRectangle);
        } else {
            Shape myCircle = new Ellipse2D.Float(-3, -3, 6, 6);
            addSeries(legend, plottableObject, lineColor, true, false, myCircle);

        }

    }

    /**
     * Build a series from a FloatSource object, using the default variableId.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the FloatSource interface.
     */
    public void addSeries(String legend, FloatSource plottableObject) {
        sources.add(new FSource(legend, plottableObject, FloatSource.Variables.Default));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series from a FloatSource object.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the FloatSource interface.
     * @param variableID      The variable id of the source object.
     */
    public void addSeries(String legend, FloatSource plottableObject,
                          Enum<?> variableID) {
        sources.add(new FSource(legend, plottableObject, variableID));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series from a LongSource object, using the default variableId.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the LongSource interface.
     */
    public void addSeries(String legend, LongSource plottableObject) {
        sources.add(new LSource(legend, plottableObject, LongSource.Variables.Default));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series from a LongSource object.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the IDblSource interface.
     * @param variableID      The variable id of the source object.
     */
    public void addSeries(String legend, LongSource plottableObject,
                          Enum<?> variableID) {
        sources.add(new LSource(legend, plottableObject, variableID));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series from a IntSource object, using the default variableId.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the IntSource interface.
     */
    public void addSeries(String legend, IntSource plottableObject) {
        sources.add(new ISource(legend, plottableObject, IntSource.Variables.Default));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series from a IntSource object.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the IntSource interface.
     * @param variableID      The variable id of the source object.
     */
    public void addSeries(String legend, IntSource plottableObject,
                          Enum<?> variableID) {
        sources.add(new ISource(legend, plottableObject, variableID));
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Build a series from a generic object.
     *
     * @param legend        The legend name of the series.
     * @param target        The data source object.
     * @param variableName  The variable or method name of the source object.
     * @param getFromMethod Specifies if the variableName is a field or a method.
     */
    public void addSeries(String legend, Object target, String variableName,
                          boolean getFromMethod) {
        Source source;
        if (ReflectionUtils.isDoubleSource(target.getClass(), variableName, getFromMethod))
            source = new DSource(legend, new DoubleInvoker(target, variableName, getFromMethod),
                    DoubleSource.Variables.Default);
        else if (ReflectionUtils.isFloatSource(target.getClass(), variableName, getFromMethod))
            source = new FSource(legend, new FloatInvoker(target, variableName, getFromMethod),
                    FloatSource.Variables.Default);
        else if (ReflectionUtils.isIntSource(target.getClass(), variableName, getFromMethod))
            source = new ISource(legend, new IntegerInvoker(target, variableName, getFromMethod),
                    IntSource.Variables.Default);
        else if (ReflectionUtils.isLongSource(target.getClass(), variableName, getFromMethod))
            source = new LSource(legend, new LongInvoker(target, variableName, getFromMethod),
                    LongSource.Variables.Default);
        else throw new IllegalArgumentException("The target object " + target
                    + " does not provide a value of a valid data type.");

        sources.add(source);
        XYSeries series = new XYSeries(legend);
        if (maxSamples > 0) series.setMaximumItemCount(maxSamples);
        dataset.addSeries(series);
    }

    /**
     * Max samples parameters allow to define a maximum number of points.
     * When set the plotting window shifts automatically along with time.
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

    public XYLineAndShapeRenderer getRenderer() {
        return (XYLineAndShapeRenderer) plot.getRenderer();
    }

    public void setRenderer(XYLineAndShapeRenderer renderer) {
        plot.setRenderer(renderer);
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
