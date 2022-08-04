package microsim.gui.plot;

import lombok.val;
import microsim.engine.SimulationEngine;
import microsim.event.CommonEventType;
import microsim.event.EventListener;
import microsim.statistics.UpdatableSource;
import microsim.statistics.weighted.WeightedDoubleArraySource;
import microsim.statistics.weighted.WeightedFloatArraySource;
import microsim.statistics.weighted.WeightedIntArraySource;
import microsim.statistics.weighted.WeightedLongArraySource;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.statistics.HistogramType;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;

/**
 * A {@link Weighted_HistogramSimulationPlotter} is able to display a histogram of one or more data sources that each
 * implements the Weight interface, and can be updated during the simulation. It is based on JFreeChart library and uses
 * data sources based on the microsim.statistics.weighted* interfaces. Note that the weights are taken into account by
 * adding the weight to the count of the histogram bin corresponding to the value associated with the weight. E.g, if a
 * weighted object has value of 1.6 and weight of 5.3, the count of 5.3 is placed in the histogram bin appropriate for
 * the value of 1.6.
 */
public class Weighted_HistogramSimulationPlotter extends JInternalFrame implements EventListener {

    @Serial
    private static final long serialVersionUID = 1L;

    final JFreeChart chart;

    private final ArrayList<WeightedArraySource> sources;
    private final HistogramType type;
    private final int bins;
    private final Double minimum;
    private final Double maximum;
    private Weighted_HistogramDataset dataset;

    /**
     * Constructor for histogram chart objects with chart legend displayed by default and all data samples shown,
     * showing only the latest population data as time moves forward. Note - values falling on the boundary of adjacent
     * bins will be assigned to the higher indexed bin. If it is desired set the minimum and maximum values displayed,
     * or to turn the legend off, use the constructor:
     * {@link Weighted_HistogramSimulationPlotter(String, String, HistogramType, int, Double, Double, boolean)}
     *
     * @param title Title of the chart.
     * @param xaxis Name of the x-axis.
     * @param type  The type of the histogram: either FREQUENCY, RELATIVE_FREQUENCY, or SCALE_AREA_TO_1.
     * @param bins  The number of bins in the histogram.
     */
    public Weighted_HistogramSimulationPlotter(String title, String xaxis, HistogramType type, int bins) {
        //Includes legend by default and will accumulate data samples by default (if wanting only the most recent data points, use the other constructor)
        this(title, xaxis, type, bins, null, null, true);
    }

    /**
     * Constructor for scatterplot chart objects, featuring a toggle to hide the chart legend and to set the minimum and
     * maximum values displayed in the chart, with values below the minimum assigned to the first bin, and values above
     * the maximum assigned to the last bin. Note - values falling on the boundary of adjacent bins will be assigned to
     * the higher indexed bin.
     *
     * @param title         Title of the chart.
     * @param xaxis         Name of the x-axis.
     * @param type          The type of the histogram: either FREQUENCY, RELATIVE_FREQUENCY, or SCALE_AREA_TO_1.
     * @param bins          The number of bins in the histogram.
     * @param minimum       Any data value less than minimum will be assigned to the first bin.
     * @param maximum       Any data value greater than maximum will be assigned to the last bin.
     * @param includeLegend Toggles whether to include the legend. If displaying a very large number of different series
     *                      in the chart, it may be useful to turn the legend off as it will occupy a lot of space in
     *                      the GUI.
     */
    public Weighted_HistogramSimulationPlotter(String title, String xaxis, HistogramType type, int bins, Double minimum,
                                               Double maximum, boolean includeLegend) {
        //Can specify whether to include legend
        this.setResizable(true);
        this.setTitle(title);
        this.type = type;
        this.bins = bins;
        this.minimum = minimum;
        this.maximum = maximum;

        sources = new ArrayList<>();

        dataset = new Weighted_HistogramDataset();

        String yaxis;
        if (type.equals(HistogramType.FREQUENCY)) yaxis = "Frequency";
        else if (type.equals(HistogramType.RELATIVE_FREQUENCY))
            throw new IllegalArgumentException("ERROR - RELATIVE_FREQUENCY Histogram Type is not currently available for" +
                    " Weighted_HistogramSimulationPlotter!  Please use FREQUENCY (or possibly SCALE_AREA_TO_1) as the Histogram Type instead.");
        else if (type.equals(HistogramType.SCALE_AREA_TO_1)) {
            System.out.println("WARNING - the SCALE_AREA_TO_1 Weighted_HistogramSimulationPlotter has not been tested and may produce incorrect output!");
            yaxis = "Density (area scaled to 1)";
        } else
            throw new IllegalArgumentException("Incorrect HistogramType argument when calling HistogramSimulationPlotter constructor!");

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
        val plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setForegroundAlpha(0.85f);

        val renderer = new XYBarRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setShadowVisible(false);
        plot.setRenderer(renderer);

        val domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        val rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());

        val chartPanel = new ChartPanel(chart);

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
        dataset = new Weighted_HistogramDataset();
        dataset.setType(type);
        chart.getXYPlot().setDataset(dataset);

        for (WeightedArraySource cs : sources) {
            val vals = cs.getDoubleArray();
            val weights = cs.getWeights();
            if (minimum != null && maximum != null) dataset.addSeries(cs.label, vals, weights, bins, minimum, maximum);
            else dataset.addSeries(cs.label, vals, weights, bins);
        }
        dataset.seriesChanged(new SeriesChangeEvent("Update at time " + SimulationEngine.getInstance().getTime()));
    }

    /**
     * Add a new series buffer, retrieving value from WeightedDoubleSource objects in a collection.
     *
     * @param name   The name of the series, which is shown in the legend.
     * @param source A collection containing the sources.
     */
    public void addCollectionSource(String name, WeightedDoubleArraySource source) {
        DWeightedArraySource sequence = new DWeightedArraySource(name, source);
        sources.add(sequence);
    }

    /**
     * Add a new series buffer, retrieving value from WeightedFloatSource objects in a collection.
     *
     * @param name   The name of the series, which is shown in the legend.
     * @param source A collection containing the sources.
     */
    public void addCollectionSource(String name, WeightedFloatArraySource source) {
        FWeightedArraySource sequence = new FWeightedArraySource(name, source);
        sources.add(sequence);
    }

    /**
     * Add a new series buffer, retrieving value from WeightedIntArraySource objects in a collection.
     *
     * @param name   The name of the series, which is shown in the legend.
     * @param source A collection containing the sources.
     */
    public void addCollectionSource(String name, WeightedIntArraySource source) {
        IWeightedArraySource sequence = new IWeightedArraySource(name, source);
        sources.add(sequence);
    }

    /**
     * Add a new series buffer, retrieving value from WeightedLongSource objects in a collection.
     *
     * @param name   The name of the series, which is shown in the legend.
     * @param source A collection containing the sources.
     */
    public void addCollectionSource(String name, WeightedLongArraySource source) {
        LWeightedArraySource sequence = new LWeightedArraySource(name, source);
        sources.add(sequence);
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
            for (int i = 0; i < array.length; i++) output[i] = array[i]; // fixme memcpy

            return output;
        }

        @Override
        public double[] getWeights() {
            return source.getWeights();
        }
    }

}
