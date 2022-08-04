// Metawidget
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package microsim.gui.shell.parameter;

import org.apache.commons.beanutils2.BeanUtils;
import org.apache.commons.beanutils2.ConvertUtils;
import org.apache.commons.beanutils2.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.swing.widgetprocessor.binding.BindingConverter;
import org.metawidget.util.CollectionUtils;
import org.metawidget.util.simple.PathUtils;
import org.metawidget.util.simple.StringUtils;
import org.metawidget.widgetprocessor.iface.AdvancedWidgetProcessor;
import org.metawidget.widgetprocessor.iface.WidgetProcessorException;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static org.metawidget.inspector.InspectionResultConstants.*;

/**
 * Property binding implementation based on BeanUtils.
 * <p>
 * This implementation recognizes the following {@code SwingMetawidget.setParameter} parameters:
 * <p>
 * <ul>
 * <li>{@code propertyStyle} - either {@code PROPERTYSTYLE_JAVABEAN} (default) or
 * {@code PROPERTYSTYLE_SCALA} (for Scala-style getters and setters).
 * </ul>
 * <p>
 * Note: {@code BeanUtils} does not bind <em>actions</em>, such as invoking a method when a
 * {@code JButton} is pressed. For that, see {@code ReflectionBindingProcessor} and
 * {@code MetawidgetActionStyle} or {@code SwingAppFrameworkActionStyle}.
 */

public class MetawidgetBinder implements AdvancedWidgetProcessor<JComponent, SwingMetawidget>, BindingConverter {

    public MetawidgetBinder() {

    }

    public void onStartBuild(SwingMetawidget metawidget) {
        metawidget.putClientProperty(MetawidgetBinder.class, null);
    }

    public JComponent processWidget(JComponent component, String elementName, Map<String, String> attributes,
                                    SwingMetawidget metawidget) {

        JComponent componentToBind = component;

        // Unwrap JScrollPanes (for JTextAreas etc)

        if (componentToBind instanceof JScrollPane) {
            componentToBind = (JComponent) ((JScrollPane) componentToBind).getViewport().getView();
        }

        // Nested Metawidgets are not bound, only remembered

        if (componentToBind instanceof SwingMetawidget) {

            State state = getState(metawidget);

            if (state.nestedMetawidgets == null) {
                state.nestedMetawidgets = CollectionUtils.newHashSet();
            }

            state.nestedMetawidgets.add((SwingMetawidget) component);
            return component;
        }

        // Determine value property

        String componentProperty = metawidget.getValueProperty(componentToBind);

        if (componentProperty == null) return component;

        String path = metawidget.getPath();

        if (PROPERTY.equals(elementName)) {
            path += StringUtils.SEPARATOR_FORWARD_SLASH_CHAR + attributes.get(NAME);
        }

        try {
            // Convert 'com.Foo/bar/baz' into BeanUtils notation 'bar.baz'

            String names = PathUtils.parsePath(path, StringUtils.SEPARATOR_FORWARD_SLASH_CHAR)
                    .getNames()
                    .replace(StringUtils.SEPARATOR_FORWARD_SLASH_CHAR, StringUtils.SEPARATOR_DOT_CHAR);

            Object sourceValue;

            try {
                sourceValue = retrieveValueFromObject(metawidget, metawidget.getToInspect(), names);
            } catch (NoSuchMethodException e) {
                throw WidgetProcessorException.newException("Property '" + names + "' has no getter");
            }

            SavedBinding binding = new SavedBinding(componentToBind, componentProperty, names,
                    TRUE.equals(attributes.get(NO_SETTER)));
            saveValueToWidget(binding, sourceValue);

            State state = getState(metawidget);

            if (state.bindings == null) state.bindings = CollectionUtils.newHashSet();

            state.bindings.add(binding);
        } catch (Exception e) {
            throw WidgetProcessorException.newException(e);
        }

        return component;
    }

    /**
     * Rebinds the Metawidget to the given Object.
     * <p>
     * This method is an optimization that allows clients to load a new object into the binding
     * <em>without</em> calling setToInspect, and therefore without reinspecting the object or
     * recreating the components. It is the client's responsbility to ensure the rebound object is
     * compatible with the original setToInspect.
     */

    public void rebind(Object toRebind, SwingMetawidget metawidget) {

        metawidget.updateToInspectWithoutInvalidate(toRebind);
        State state = getState(metawidget);

        // Our bindings

        if (state.bindings != null) {
            try {
                for (SavedBinding binding : state.bindings) {
                    Object sourceValue;
                    String names = binding.getNames();

                    try {
                        sourceValue = retrieveValueFromObject(metawidget, toRebind, names);
                    } catch (NoSuchMethodException e) {
                        throw WidgetProcessorException.newException("Property '" + names + "' has no getter");
                    }

                    saveValueToWidget(binding, sourceValue);
                }
            } catch (Exception e) {
                throw WidgetProcessorException.newException(e);
            }
        }

        // Nested Metawidgets

        if (state.nestedMetawidgets != null) {
            for (SwingMetawidget nestedMetawidget : state.nestedMetawidgets) {
                rebind(toRebind, nestedMetawidget);
            }
        }
    }

    public void save(SwingMetawidget metawidget) {

        State state = getState(metawidget);

        // Our bindings

        if (state.bindings != null) {
            try {
                for (SavedBinding binding : state.bindings) {
                    if (!binding.isSettable()) {
                        continue;
                    }

                    Object componentValue = retrieveValueFromWidget(binding);
                    saveValueToObject(metawidget, binding.getNames(), componentValue);
                }
            } catch (Exception e) {
                throw WidgetProcessorException.newException(e);
            }
        }

        // Nested Metawidgets

        if (state.nestedMetawidgets != null) {
            for (SwingMetawidget nestedMetawidget : state.nestedMetawidgets) {
                save(nestedMetawidget);
            }
        }
    }

    public Object convertFromString(String value, Class<?> expectedType) {

        return ConvertUtils.convert(value, expectedType);
    }

    public void onEndBuild(SwingMetawidget metawidget) {
    }

    /**
     * Retrieve value identified by the given names from the given source.
     * <p>
     * Clients may override this method to incorporate their own getter convention.
     *
     * @param metawidget Metawidget to retrieve value from
     */

    protected Object retrieveValueFromObject(SwingMetawidget metawidget, Object source, String names) throws Exception {
        return BeanUtils.getProperty(source, names);
    }

    /**
     * Save the given value into the given source at the location specified by the given names.
     * <p>
     * Clients may override this method to incorporate their own setter convention.
     *
     * @param componentValue the raw value from the {@code JComponent}
     */

    protected void saveValueToObject(SwingMetawidget metawidget, String names, Object componentValue) throws Exception {

        Object source = metawidget.getToInspect();

        Field field = source.getClass().getDeclaredField(names);
        field.setAccessible(true);

        if (field.getType().isEnum()) {
            if (componentValue != null) {
                var c = field.getType();
                var ce = (Class<? extends Enum>) c;
                var value1 = Enum.valueOf(ce, componentValue.toString());
                BeanUtils.setProperty(source, names, value1);
            } else {
                BeanUtils.setProperty(source, names, null);
            }
        } else {
            BeanUtils.setProperty(source, names, componentValue);
        }
    }

    protected Object retrieveValueFromWidget(SavedBinding binding) throws Exception {
        return PropertyUtils.getProperty(binding.getComponent(), binding.getComponentProperty());
    }

    protected void saveValueToWidget(SavedBinding binding, Object sourceValue) throws Exception {
        BeanUtils.setProperty(binding.getComponent(), binding.getComponentProperty(),
                sourceValue.getClass().isEnum() ? sourceValue.toString() : sourceValue);
    }

    private State getState(SwingMetawidget metawidget) {
        State state = (State) metawidget.getClientProperty(MetawidgetBinder.class);

        if (state == null) {
            state = new State();
            metawidget.putClientProperty(MetawidgetBinder.class, state);
        }

        return state;
    }


    /**
     * Simple, lightweight structure for saving state.
     */

    static class State {

        Set<SavedBinding> bindings;

        Set<SwingMetawidget> nestedMetawidgets;
    }

    static class SavedBinding {

        private final Component mComponent;

        private final String mComponentProperty;

        private final String mNames;

        private final boolean mNoSetter;

        public SavedBinding(Component component, String componentProperty, String names, boolean noSetter) {

            mComponent = component;
            mComponentProperty = componentProperty;
            mNames = names;
            mNoSetter = noSetter;
        }

        public Component getComponent() {
            return mComponent;
        }

        public String getComponentProperty() {
            return mComponentProperty;
        }

        /**
         * Property names into the source object.
         * <p>
         * Stored in BeanUtils style {@code foo.bar.baz}.
         */

        public String getNames() {
            return mNames;
        }

        public boolean isSettable() {// fixme replace with nice getters
            return !mNoSetter;
        }
    }
}
