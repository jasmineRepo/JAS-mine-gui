package microsim.gui.probe;

import lombok.val;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.Serial;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Not of interest for users. A data model used to show the list of variables into the probe.
 */
public class VariableDataModel extends AbstractTableModel {

    private static final Logger log = Logger.getLogger(VariableDataModel.class.getCanonicalName());

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int COLUMNS = 4;

    private static final int COL_FIELD = 0;
    private static final int COL_NAME = 1;
    private static final int COL_TYPE = 2;
    private static final int COL_VALUE = 3;

    private final Object targetObj;
    private Object[][] data;
    private boolean viewPrivate;
    private List<Object> probeFields;

    private int deepLevel = 0;

    public VariableDataModel(Object objToInspect) {
        targetObj = objToInspect;
        viewPrivate = true;
        try {
            probeFields = ((ProbeFields) objToInspect).getProbeFields();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error creating VariableDataModel: " + e.getMessage());
        }
        updateWithFields();
    }

    public VariableDataModel(Object objToInspect, boolean privateVariables) {
        targetObj = objToInspect;
        viewPrivate = privateVariables;
        probeFields = null;
        update(viewPrivate);
    }

    private void updateWithFields() {
        Class<?> cl = targetObj.getClass();
        Field[] fields;
        int k = 0;

        // Searching for rowCount
        while (cl != null) {
            fields = cl.getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);

            for (Field field : fields) if (probeFields.contains(field.getName())) k++;

            cl = cl.getSuperclass();
        }

        // Now fill up the fields
        data = new Object[k][COLUMNS];
        cl = targetObj.getClass();
        k = 0;
        while (cl != null) {
            fields = cl.getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);

            for (Field field : fields)
                if (probeFields.contains(field.getName())) {
                    fillCell(k, field);
                    k++;
                }

            cl = cl.getSuperclass();
        }
    }

    private void fillCell(int k, Field field) {
        data[k][COL_NAME] = field.getName();
        data[k][COL_TYPE] = field.getType();

        try {
            Object o = field.get(targetObj);
            if (o != null) {
                data[k][COL_FIELD] = field;
                val condition = ProbeReflectionUtils.isCollection(field.getType()) || o.getClass().isArray();
                data[k][COL_VALUE] = condition ? "[...]" : o.toString();
            }
        } catch (Exception e) {
            System.out.println("Error in field :" + e.getMessage());
        }
    }

    public void update() {
        if (probeFields == null) update(viewPrivate);
        else updateWithFields();
    }

    public void setViewPrivate(boolean privateVariables) {
        viewPrivate = privateVariables;
    }

    public void setDeepLevel(int level) {
        deepLevel = level;
    }

    private void update(boolean privateVariables) {
        Class<?> cl = targetObj.getClass();
        for (int i = 0; i < deepLevel; i++) cl = cl.getSuperclass();

        Field[] fields;
        if (viewPrivate) fields = cl.getDeclaredFields();
        else fields = cl.getFields();
        AccessibleObject.setAccessible(fields, true);

        data = new Object[fields.length][COLUMNS];
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            fillCell(i, f);
        }
    }

    public String getHeaderText(int column) {
      return switch (column + 1) {
        case COL_NAME -> "Name";
        case COL_TYPE -> "Type";
        case COL_VALUE -> "Value";
        default -> "";
      };
    }

    public int getColumnCount() {
      return data == null ? 0 : COLUMNS - 1;
    }

    public Object getValueAt(int row, int col) {
        return data[row][col + 1];
    }

    public int getRowCount() {
      return data == null ? 0 : data.length;
    }

    public void setValueAt(Object val, int row, int col) {

        try {
            Field f = (Field) data[row][COL_FIELD];

            if (f == null) {
                JOptionPane.showMessageDialog(null, "The variable is null.\n It is impossible to edit.",
                        "Probe editing variable", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (f.getType().isPrimitive() || f.getType().getName().equals("java.lang.String"))
                setPrimitiveValueToClass(f.getType(), val, f);
            else {
                JOptionPane.showMessageDialog(null, "The variable is not a primitive.\n" +
                                "To edit its value you can open a probe to it. ",
                        "Probe editing variable", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

        } catch (Exception e) {
            System.out.println("Error setting field: " + e.getMessage());
            return;
        }
        // Indicate the change has happened:
        data[row][col + 1] = val;
        fireTableDataChanged();
    }

    private void setPrimitiveValueToClass(Class<?> cl, Object val, Field f) {// fixme find similar code, remove returns
        try {
            if (cl.getName().equals("java.lang.String")) f.set(targetObj, val.toString());
            if (cl == Integer.TYPE) f.setInt(targetObj, Integer.parseInt(val.toString()));
            if (cl == Double.TYPE) f.setDouble(targetObj, Double.parseDouble(val.toString()));
            if (cl == Boolean.TYPE) f.setBoolean(targetObj, Boolean.parseBoolean(val.toString()));
            if (cl == Byte.TYPE) f.setByte(targetObj, Byte.parseByte(val.toString()));
            if (cl == Character.TYPE) f.setChar(targetObj, val.toString().charAt(0));
            if (cl == Float.TYPE) f.setFloat(targetObj, Float.parseFloat(val.toString()));
            if (cl == Long.TYPE) f.setLong(targetObj, Long.parseLong(val.toString()));
            if (cl == Short.TYPE) f.setShort(targetObj, Short.parseShort(val.toString()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Object getObjectAtRow(int row) {
        try {
            return ((Field) data[row][COL_FIELD]).get(targetObj);
        } catch (Exception e) {
            return null;
        }
    }

    public String getObjectNameAtRow(int row) {
        try {
            return ((Field) data[row][COL_FIELD]).getName();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isCellEditable(int row, int col) {
        return (col + 1) == COL_VALUE;
    }
}
