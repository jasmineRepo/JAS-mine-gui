package microsim.gui.plot;

import microsim.event.CommonEventType;
import microsim.event.EventListener;
import microsim.gui.colormap.FixedColorMap;
import microsim.reflection.ReflectionUtils;
import microsim.statistics.*;
import microsim.statistics.reflectors.DoubleInvoker;
import microsim.statistics.reflectors.FloatInvoker;
import microsim.statistics.reflectors.IntegerInvoker;
import microsim.statistics.reflectors.LongInvoker;
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
 * A bar chart plotter showing elements manually added by user. It is based on JFreeChart library. It is compatible with
 * the microsim.statistics.* classes.
 */
public class IndividualBarSimulationPlotter extends JInternalFrame implements EventListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ArrayList<Source> sources;
    private final ArrayList<String> categories;

    private final DefaultCategoryDataset dataset;

    private final String yaxis;

    private final FixedColorMap colorMap;

    public IndividualBarSimulationPlotter(String title, String yaxis) {
        super();
        this.setResizable(true);
        this.setTitle(title);
        this.yaxis = yaxis;
        colorMap = new FixedColorMap();

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


        // set the range axis to display integers only... Ross: Why???
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());

        // disable bar outlines...
        BarRenderer renderer = new ColoredBarRenderer(colorMap);
        plot.setRenderer(renderer);
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
            Source source = sources.get(i);
            double d = source.getDouble();
            String category = categories.get(i);

            dataset.addValue(d, yaxis, category);
        }
    }

    /**
     * Build a series retrieving data from a IDoubleSource object, using the default variableId.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the IDoubleSource interface.
     */
    public void addSources(String legend, DoubleSource plottableObject) {
        sources.add(new DSource(legend, plottableObject, DoubleSource.Variables.Default));
        // set up gradient paints for series...
        categories.add(legend);
    }

    /**
     * Build a series from a generic object.
     *
     * @param legend        The legend name of the series.
     * @param target        The data source object.
     * @param variableName  The variable or method name of the source object.
     * @param getFromMethod Specifies if the variableName is a field or a method.
     */
    public void addSources(String legend, Object target, String variableName, boolean getFromMethod) {
        Source source = null;
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
        else throw new IllegalArgumentException(("The target object %s " +
                    "does not provide a value of a valid data type.").formatted(target));

    }

    /**
     * Build a series retrieving data from a IDoubleSource object, using the
     * default variableId and specifying the colour.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the IDoubleSource interface.
     * @param color           Specifies the color of the bar
     */
    public void addSources(String legend, DoubleSource plottableObject, Color color) {
        addSources(legend, plottableObject);
        int seriesNum = sources.size() - 1;    //Start with value of 0
        colorMap.addColor(seriesNum, color);
    }

    /**
     * Build a series retrieving data from a IDoubleSource object and specifying the colour.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the IDoubleSource interface.
     * @param variableID      The variable id of the source object.
     * @param color           Specifies the color of the bar
     */
    public void addSources(String legend, DoubleSource plottableObject, Enum<?> variableID, Color color) {
        sources.add(new DSource(legend, plottableObject, variableID));
        categories.add(legend);
        int seriesNum = sources.size() - 1;    //Start with value of 0
        colorMap.addColor(seriesNum, color);
    }

    /**
     * Build a series from a IFloatSource object, using the default variableId and specifying the colour.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the IFloatSource interface.
     * @param color           Specifies the color of the bar
     */
    public void addSources(String legend, FloatSource plottableObject, Color color) {
        sources.add(new FSource(legend, plottableObject, FloatSource.Variables.Default));
        categories.add(legend);
        int seriesNum = sources.size() - 1;    //Start with value of 0
        colorMap.addColor(seriesNum, color);
    }

    /**
     * Build a series from a IFloatSource object and specifying the colour.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the IFloatSource interface.
     * @param variableID      The variable id of the source object.
     * @param color           Specifies the color of the bar
     */
    public void addSources(String legend, FloatSource plottableObject,
                           Enum<?> variableID, Color color) {
        sources.add(new FSource(legend, plottableObject, variableID));
        categories.add(legend);
        int seriesNum = sources.size() - 1;    //Start with value of 0
        colorMap.addColor(seriesNum, color);
    }

    /**
     * Build a series from a ILongSource object, using the default variableId and specifying the colour.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the ILongSource interface.
     * @param color           Specifies the color of the bar
     */
    public void addSources(String legend, LongSource plottableObject, Color color) {
        sources.add(new LSource(legend, plottableObject, LongSource.Variables.Default));
        categories.add(legend);
        int seriesNum = sources.size() - 1;    //Start with value of 0
        colorMap.addColor(seriesNum, color);
    }

    //--------------------------------------------------------------------------
    //
    // Methods to specify colour of each bar (source)
    //
    //--------------------------------------------------------------------------

    /**
     * Build a series from a ILongSource object and specifying the colour.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the IDblSource interface.
     * @param variableID      The variable id of the source object.
     * @param color           Specifies the color of the bar
     */
    public void addSources(String legend, LongSource plottableObject,
                           Enum<?> variableID, Color color) {
        sources.add(new LSource(legend, plottableObject, variableID));
        categories.add(legend);
        int seriesNum = sources.size() - 1;    //Start with value of 0
        colorMap.addColor(seriesNum, color);
    }

    /**
     * Build a series from a IIntSource object, using the default variableId and specifying the colour.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the IIntSource interface.
     * @param color           Specifies the color of the bar
     */
    public void addSources(String legend, IntSource plottableObject, Color color) {
        sources.add(new ISource(legend, plottableObject, IntSource.Variables.Default));
        categories.add(legend);
        int seriesNum = sources.size() - 1;    //Start with value of 0
        colorMap.addColor(seriesNum, color);
    }

    /**
     * Build a series from a IIntSource object and specifying the colour.
     *
     * @param legend          The legend name of the series.
     * @param plottableObject The data source object implementing the IIntSource interface.
     * @param variableID      The variable id of the source object.
     * @param color           Specifies the color of the bar
     */
    public void addSources(String legend, IntSource plottableObject,
                           Enum<?> variableID, Color color) {
        sources.add(new ISource(legend, plottableObject, variableID));
        categories.add(legend);
        int seriesNum = sources.size() - 1;    //Start with value of 0
        colorMap.addColor(seriesNum, color);
    }

    /**
     * Build a series from a generic object and specifying the colour.
     *
     * @param legend        The legend name of the series.
     * @param target        The data source object.
     * @param variableName  The variable or method name of the source object.
     * @param getFromMethod Specifies if the variableName is a field or a method.
     * @param color         Specifies the color of the bar
     */
    public void addSources(String legend, Object target, String variableName,
                           boolean getFromMethod, Color color) {
        addSources(legend, target, variableName, getFromMethod);
        int seriesNum = sources.size() - 1;    //Start with value of 0
        colorMap.addColor(seriesNum, color);
    }

    private abstract static class Source {
        //public String label;
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
            if (isUpdatable)
                ((UpdatableSource) source).updateSource();
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
            if (isUpdatable)
                ((UpdatableSource) source).updateSource();
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
            if (isUpdatable)
                ((UpdatableSource) source).updateSource();
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
            if (isUpdatable)
                ((UpdatableSource) source).updateSource();
            return source.getLongValue(vId);
        }
    }

    static class ColoredBarRenderer extends BarRenderer {

        @Serial
        private static final long serialVersionUID = -7678490515617294057L;

        private final FixedColorMap colormap;

        ColoredBarRenderer(FixedColorMap colormap) {
            this.colormap = colormap;
        }

        public Paint getItemPaint(final int row, final int column) {
            // returns color for each column
            return (colormap.getColor(column));
        }
    }
}
