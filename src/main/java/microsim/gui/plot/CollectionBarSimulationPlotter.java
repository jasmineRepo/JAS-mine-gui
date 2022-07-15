package microsim.gui.plot;

import microsim.event.CommonEventType;
import microsim.event.EventListener;
import microsim.statistics.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;

/**
 * A bar chart plotter showing elements manually added by user. It is based on JFreeChart library. It is compatible
 * with the microsim.statistics.* classes.
 */
public class CollectionBarSimulationPlotter extends JInternalFrame implements EventListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ArrayList<ArraySource> sources;
    private final ArrayList<String> categories;

    private final DefaultCategoryDataset dataset;

    private Integer maxBars;

    public CollectionBarSimulationPlotter(String title, String yaxis) {
        super();
        this.setResizable(true);
        this.setTitle(title);

        sources = new ArrayList<>();
        categories = new ArrayList<>();

        dataset = new DefaultCategoryDataset();

        final JFreeChart chart = ChartFactory.createBarChart(
                title,      // chart title
                "Categories",                      // x axis label                
                yaxis,                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                false,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        // set the range axis to display integers only...	Ross: Why???
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());

        // disable bar outlines...
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);

        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
        );
        // OPTIONAL CUSTOMISATION COMPLETED.

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
        for (int i = 0; i < sources.size(); i++) {
            ArraySource cs = sources.get(i);
            final String category = categories.get(i);

            double[] vals = cs.getDoubleArray();
            for (int j = 0; j < vals.length && (j < (maxBars == null ? Integer.MAX_VALUE : maxBars)); j++)
                dataset.addValue(vals[j], category, "" + j);
        }
    }

    /**
     * Add a new series buffer, retrieving value from IDoubleSource objects in a collection.
     *
     * @param name   The name of the series, which is shown in the legend.
     * @param source A collection containing the sources.
     */
    public void addCollectionSource(String name, DoubleArraySource source) {
        DArraySource sequence = new DArraySource(name, source);
        sources.add(sequence);
        categories.add(name);
    }

    /**
     * Add a new series buffer, retrieving value from IDoubleSource objects in a collection.
     *
     * @param name   The name of the series, which is shown in the legend.
     * @param source A collection containing the sources.
     */
    public void addCollectionSource(String name, FloatArraySource source) {
        FArraySource sequence = new FArraySource(name, source);
        sources.add(sequence);
        categories.add(name);
    }

    /**
     * Add a new series buffer, retrieving value from IDoubleSource objects in a collection.
     *
     * @param name   The name of the series, which is shown in the legend.
     * @param source A collection containing the sources.
     */
    public void addCollectionSource(String name, IntArraySource source) {
        IArraySource sequence = new IArraySource(name, source);
        sources.add(sequence);
        categories.add(name);
    }

    /**
     * Add a new series buffer, retrieving value from IDoubleSource objects in a collection.
     *
     * @param name   The name of the series, which is shown in the legend.
     * @param source A collection containing the sources.
     */
    public void addCollectionSource(String name, LongArraySource source) {
        LArraySource sequence = new LArraySource(name, source);
        sources.add(sequence);
        categories.add(name);
    }

    public Integer getMaxBars() {
        return maxBars;
    }

    public void setMaxBars(Integer maxBars) {
        this.maxBars = maxBars;
    }

    private abstract static class ArraySource {
        protected boolean isUpdatable;

        public abstract double[] getDoubleArray();

    }

    private static class DArraySource extends ArraySource {
        public DoubleArraySource source;

        public DArraySource(String label, DoubleArraySource source) {
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
            this.source = source;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double[] getDoubleArray() {
            if (isUpdatable) ((UpdatableSource) source).updateSource();
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
            this.source = source;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double[] getDoubleArray() {
            if (isUpdatable) ((UpdatableSource) source).updateSource();
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
            this.source = source;
            isUpdatable = (source instanceof UpdatableSource);
        }

        public double[] getDoubleArray() {
            if (isUpdatable) ((UpdatableSource) source).updateSource();
            long[] array = source.getLongArray();
            double[] output = new double[array.length];
            for (int i = 0; i < array.length; i++)
                output[i] = array[i];

            return output;
        }
    }
}
