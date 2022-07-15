package microsim.gui.plot;

import microsim.engine.SimulationEngine;
import microsim.event.CommonEventType;
import microsim.event.EventListener;
import microsim.statistics.*;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;

import static org.jfree.data.statistics.HistogramType.*;

/**
 * A HistogramSimulationPlotter is able to display a histogram of one or more data sources, which can be updated during
 * the simulation. It is based on JFreeChart library and uses data sources based on the microsim.statistics.*
 * interfaces.
 */
public class HistogramSimulationPlotter extends JInternalFrame implements EventListener {

    @Serial
    private static final long serialVersionUID = 1L;

    final JFreeChart chart;

    private final ArrayList<ArraySource> sources;
    private final HistogramType type;
    private final int bins;
    private final Double minimum;
    private final Double maximum;
    private HistogramDataset dataset;

    /**
     * Constructor for histogram chart objects with chart legend displayed by default and
     * all data samples shown, showing only the latest population data as time moves forward.
     * Note - values falling on the boundary of adjacent bins will be assigned to the higher
     * indexed bin.  If it is desired set the minimum and maximum values displayed, or to turn
     * the legend off, use the constructor:
     * {@link HistogramSimulationPlotter(String, String, HistogramType, int, double, double, boolean)}
     *
     * @param title Title of the chart.
     * @param xaxis Name of the x-axis.
     * @param type  The type of the histogram: either FREQUENCY, RELATIVE_FREQUENCY, or SCALE_AREA_TO_1
     * @param bins  The number of bins in the histogram.
     */
    public HistogramSimulationPlotter(String title, String xaxis, HistogramType type, int bins) {
        //Includes legend by default and will accumulate data samples by default (if wanting only the most recent data points, use the other constructor)
        this(title, xaxis, type, bins, null, null, true);
    }

    /**
     * Constructor for scatterplot chart objects, featuring a toggle to hide the chart legend and to set the minimum and
     * maximum values displayed in the chart, with values below the minimum assigned to the first bin, and values above
     * the maximum assigned to the last bin. Note - values falling on the boundary of adjacent bins will be assigned to
     * the higher indexed bin.
     *
     * @param title         Title of the chart
     * @param xaxis         Name of the x-axis
     * @param type          The type of the histogram: either FREQUENCY, RELATIVE_FREQUENCY, or SCALE_AREA_TO_1
     * @param bins          The number of bins in the histogram
     * @param minimum       Any data value less than minimum will be assigned to the first bin
     * @param maximum       Any data value greater than maximum will be assigned to the last bin
     * @param includeLegend Toggles whether to include the legend.  If displaying a very large number of different
     *                      series in the chart, it may be useful to turn the legend off as it will occupy a lot of
     *                      space in the GUI.
     */
    public HistogramSimulationPlotter(String title, String xaxis, @NotNull HistogramType type, int bins, Double minimum,
                                      Double maximum, boolean includeLegend) {
        //Can specify whether to include legend and how many samples (updates) to display
        super();
        this.setResizable(true);
        this.setTitle(title);
        this.type = type;
        this.bins = bins;
        this.minimum = minimum;
        this.maximum = maximum;

        sources = new ArrayList<>();

        dataset = new HistogramDataset();

        String yaxis;

        if (type.equals(FREQUENCY)) yaxis = "Frequency";
        else if (type.equals(RELATIVE_FREQUENCY)) yaxis = "Relative Frequency";
        else if (type.equals(SCALE_AREA_TO_1)) yaxis = "Density (area scaled to 1)";
        else throw new IllegalArgumentException("Incorrect HistogramType argument " +
                    "when calling HistogramSimulationPlotter constructor!");

        chart = ChartFactory.createHistogram(
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
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setForegroundAlpha(0.85f);

        final XYBarRenderer renderer = new XYBarRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setShadowVisible(false);
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

        dataset = new HistogramDataset();
        dataset.setType(type);
        chart.getXYPlot().setDataset(dataset);

        for (ArraySource source : sources) {
            double[] vals = source.getDoubleArray();

            if (minimum != null && maximum != null) {
                dataset.addSeries(source.label, vals, bins, minimum, maximum);
            } else dataset.addSeries(source.label, vals, bins);


        }
        dataset.seriesChanged(new SeriesChangeEvent("Update at time " + SimulationEngine.getInstance().getTime()));

    }

    /**
     * Add a new series buffer, retrieving value from objects of a proper type in a collection.
     *
     * @param name   The name of the series, which is shown in the legend.
     * @param source A collection containing the sources.
     * @throws IllegalArgumentException When {@code source} is not a DoubleArraySource, FloatArraySource,
     *                                  IntArraySource, or LongArraySource.
     */
    public void addCollectionSource(String name, @NotNull Object source) {
        ArraySource sequence = switch (source) {
            case DoubleArraySource s -> new DArraySource(name, s);
            case FloatArraySource s -> new FArraySource(name, s);
            case IntArraySource s -> new IArraySource(name, s);
            case LongArraySource s -> new LArraySource(name, s);
            default -> throw new IllegalArgumentException("Source object is not of valid type.");
        };
        sources.add(sequence);
    }

    private abstract static class ArraySource {
        public String label;
        protected boolean isUpdatable;

        public abstract double[] getDoubleArray();

    }

    private static class DArraySource extends ArraySource {
        public DoubleArraySource source;

        public DArraySource(String label, DoubleArraySource source) {
            super.label = label;
            this.source = source;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double[] getDoubleArray() {
            if (isUpdatable)
                ((UpdatableSource) source).updateSource();
            return source.getDoubleArray();
        }
    }

    private static class FArraySource extends ArraySource {
        public FloatArraySource source;

        public FArraySource(String label, FloatArraySource source) {
            super.label = label;
            this.source = source;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double[] getDoubleArray() {
            if (isUpdatable)
                ((UpdatableSource) source).updateSource();
            float[] array = source.getFloatArray();
            double[] output = new double[array.length];
            for (int i = 0; i < array.length; i++)
                output[i] = array[i];

            return output;
        }
    }

    private static class IArraySource extends ArraySource {
        public IntArraySource source;

        public IArraySource(String label, IntArraySource source) {
            super.label = label;
            this.source = source;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double[] getDoubleArray() {
            if (isUpdatable)
                ((UpdatableSource) source).updateSource();
            int[] array = source.getIntArray();
            double[] output = new double[array.length];
            for (int i = 0; i < array.length; i++)
                output[i] = array[i];

            return output;
        }
    }

    private static class LArraySource extends ArraySource {
        public LongArraySource source;

        public LArraySource(String label, LongArraySource source) {
            super.label = label;
            this.source = source;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double[] getDoubleArray() {
            if (isUpdatable)
                ((UpdatableSource) source).updateSource();
            long[] array = source.getLongArray();
            double[] output = new double[array.length];
            for (int i = 0; i < array.length; i++)
                output[i] = array[i];

            return output;
        }
    }
}
