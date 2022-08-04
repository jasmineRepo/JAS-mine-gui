package microsim.gui.shell.parameter;

import org.metawidget.swing.SwingMetawidget;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.Map;

public class DescriptiveSwingMetawidget extends SwingMetawidget {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void layoutWidget(Component component, String elementName, Map<String, String> attributes) {
        super.layoutWidget(component, elementName, attributes);
        if (component == null) return;
        ((JComponent) component).setToolTipText(attributes.get("tooltip"));
    }
}
