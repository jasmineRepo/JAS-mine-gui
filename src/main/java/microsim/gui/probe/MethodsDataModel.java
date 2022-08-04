package microsim.gui.probe;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A data model used to show the list of methods into the probe.
 */
public class MethodsDataModel implements ListModel<Method> {

    private static final Logger log = Logger.getLogger(MethodsDataModel.class.getCanonicalName());

    private final List<Method> methods;
    private final Object targetObj;
    private boolean viewPrivate;
    private List<Object> probeFields;

    private int deepLevel = 0;

    public MethodsDataModel(Object o) {
        methods = new ArrayList<>();
        targetObj = o;
        viewPrivate = true;
        try {
            probeFields = ((ProbeFields) o).getProbeFields();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error creating MethodsDataModel: " + e.getMessage());
        }
        updateWithFields();
    }

    public MethodsDataModel(Object o, boolean privateVariables) {
        methods = new ArrayList<>();
        targetObj = o;
        viewPrivate = privateVariables;
        update(viewPrivate);
    }

    public void update() {
        if (probeFields == null) update(viewPrivate);
        else updateWithFields();
    }

    public void setViewPrivate(boolean privateVariables) {
        viewPrivate = privateVariables;
    }

    private void update(boolean privateVariables) {
        viewPrivate = privateVariables;
        methods.clear();

        Class<?> cl = targetObj.getClass();
        for (int i = 0; i < deepLevel; i++) cl = cl.getSuperclass();

        Method[] meth = cl.getDeclaredMethods();
        AccessibleObject.setAccessible(meth, true);

        for (Method method : meth)
            if (viewPrivate || Modifier.isPublic(method.getModifiers()))
                methods.add(method);
    }

    public void setDeepLevel(int level) {
        deepLevel = level;
    }

    private void updateWithFields() {
        methods.clear();

        Class<?> cl = targetObj.getClass();

        while (cl != null) {
            Method[] meth = cl.getDeclaredMethods();
            AccessibleObject.setAccessible(meth, true);

            for (Method method : meth)
                if (probeFields.contains(method.getName()))
                    methods.add(method);
            cl = cl.getSuperclass();
        }
    }

    public int getSize() {
        return methods.size();
    }

    public Method getElementAt(int index) {
        return methods.get(index);
    }

    public void invokeMethodAt(int index) {
        Method m = methods.get(index);

        if (m.getParameterTypes().length > 0) {
            JOptionPane.showMessageDialog(null, "Method requires parameters", "Method result",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Object o = m.invoke(targetObj, (Object) null);

            if (o == null) return;

            JOptionPane.showMessageDialog(null, o.toString(), "Method result", JOptionPane.PLAIN_MESSAGE);

        } catch (Exception e) {
            System.out.println("Error in method.invoke:" + e.getMessage());
        }
    }

    public void invokeMethodAt(int index, Object[] params) {
        Method m = methods.get(index);

        try {
            Object o = m.invoke(targetObj, params);

            if (o == null) return;

            JOptionPane.showMessageDialog(null, o.toString(), "Method result", JOptionPane.PLAIN_MESSAGE);

        } catch (Exception e) {
            System.out.println("Error in method.invoke:" + e.getMessage());
        }
    }

    public void addListDataListener(ListDataListener l) {
    }

    public void removeListDataListener(ListDataListener l) {
    }
}