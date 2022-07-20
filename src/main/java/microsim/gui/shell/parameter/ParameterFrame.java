package microsim.gui.shell.parameter;

import microsim.annotation.GUIparameter;
import microsim.annotation.ModelParameter;
import microsim.gui.shell.MicrosimShell;
import org.metawidget.inspector.composite.CompositeInspector;
import org.metawidget.inspector.composite.CompositeInspectorConfig;
import org.metawidget.inspector.impl.BaseObjectInspector;
import org.metawidget.inspector.impl.propertystyle.Property;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.util.CollectionUtils;

import javax.swing.*;
import java.io.Serial;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class ParameterFrame extends JInternalFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Object target;

    private MetawidgetBinder binder;

    private SwingMetawidget metawidget;

    public ParameterFrame(Object target) {
        super();

        this.target = target;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setResizable(true);
        this.setTitle(target.getClass().getSimpleName() + "'s parameters");

        List<Field> fields = ParameterInspector.extractModelParameters(target.getClass());

        metawidget = new DescriptiveSwingMetawidget();
        CompositeInspectorConfig inspectorConfig = new CompositeInspectorConfig().setInspectors(
                new ParameterInspector(),
                new TooltipInspector(fields));

        binder = new MetawidgetBinder();
        metawidget.addWidgetProcessor(binder);

        metawidget.setInspector(new CompositeInspector(inspectorConfig));
        metawidget.setToInspect(target);

        setSize((int) (MicrosimShell.scale * 320),
                Math.min((int) (MicrosimShell.scale * Math.max(30 + 26 * fields.size(), 90)), 500));
        JScrollPane scrollP = new JScrollPane(metawidget);

        if (metawidget.getComponentCount() > 0)
            scrollP.getViewport().setBackground(metawidget.getComponent(0).getBackground());
        getContentPane().add(scrollP);
        setVisible(true);

    }

    public void save() {
        binder.save(metawidget);
    }

    public static class TooltipInspector extends BaseObjectInspector {
        Map<String, String> guiParamDescriptions;

        TooltipInspector(List<Field> fields) {
            guiParamDescriptions = CollectionUtils.newHashMap();
            for (Field f : fields) {
                String description;
                try {
                    description = f.getAnnotation(GUIparameter.class).description(); //fixme
                    if (description == null)
                        description = f.getAnnotation(ModelParameter.class).description();    //Old deprecated version
                    if (description != null) {
                        guiParamDescriptions.put(f.getName(), description);
                    }
                } catch (NullPointerException ignored) {
                }
            }
        }

        protected Map<String, String> inspectProperty(Property property) {
            Map<String, String> attributes = CollectionUtils.newHashMap();
            String description = guiParamDescriptions.get(property.getName());
            if (description != null) attributes.put("tooltip", description);

            return attributes;
        }
    }
}
