package microsim.gui.probe;

import javax.swing.*;
import javax.swing.table.*;

import java.io.Serial;
import java.lang.reflect.*;

/**
 * A data model used to collect input parameters of a method. It is used by the table of the MethodDialog frame.
 */
public class MethodParameterDataModel extends AbstractTableModel {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int COLUMNS = 3;

    private static final int COL_FIELD = 0;
    private static final int COL_VALUE = 2;
    private static final int COL_TYPE = 1;

    Method targetObj;
    Object[][] data;

    public MethodParameterDataModel(Method objToInspect) {
        targetObj = objToInspect;
        if (ProbeReflectionUtils.isAnExecutableMethod(targetObj)) update();
    }

    public void update() {
        Class<?>[] params = targetObj.getParameterTypes();
        data = new Object[params.length][COLUMNS];

        for (int i = 0; i < params.length; i++) {
            data[i][COL_VALUE] = "";        //Modification by Ross	 (See J. Bloch "Effective Java" 2nd Edition, Item 5)
            data[i][COL_TYPE] = params[i].getName();
            data[i][COL_FIELD] = params[i];
        }
    }

    public String getHeaderText(int column) {
        return switch (column + 1) {
            case COL_VALUE -> "Value";
            case COL_TYPE -> "Type";
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
            Object f = data[row][COL_FIELD];
            if (f == null) {
                JOptionPane.showMessageDialog(null, "The argument is of unknown type.\n It is impossible to edit.",
                        "Probe editing variable", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            data[row][col + 1] = val;

        } catch (Exception e) {
            System.out.println("Error setting field: " + e.getMessage());
            return;
        }
        // Indicate the change has happened:
        fireTableDataChanged();
    }

    public Object[] getParams() {
        Object[] obs = new Object[getRowCount()];
        Object o;

        try {
            for (int i = 0; i < obs.length; i++) {
                o = getWrapper(data[i][COL_TYPE].toString(), data[i][COL_VALUE].toString());
                if (o == null) {
                    Class<?> cc = Class.forName(data[i][COL_TYPE].toString());
                    Constructor<?> c = cc.getDeclaredConstructor(String.class);
                    o = c.newInstance(data[i][COL_VALUE].toString());
                }
                obs[i] = o;
            }
        } catch (Exception e) {
            System.out.println("Err in getParams: " + e.getMessage());
        }

        return obs;

    }

    private Object getWrapper(String s, String o) {
        if (s.equals(Integer.TYPE.getName()))
            return Integer.parseInt(o);
        if (s.equals(Double.TYPE.getName()))
            return Double.parseDouble(o);
        if (s.equals(Boolean.TYPE.getName()))
            return Boolean.valueOf(o);
        if (s.equals(Byte.TYPE.getName()))
            return Byte.parseByte(o);
        if (s.equals(Character.TYPE.getName()))
            return o.charAt(0);
        if (s.equals(Float.TYPE.getName()))
            return Float.parseFloat(o);
        if (s.equals(Long.TYPE.getName()))
            return Long.parseLong(o);
        if (s.equals(Short.TYPE.getName()))
            return Short.parseShort(o);

        return null;
    }

    public Object getObjectAtRow(int row) {
        try {
            return data[row][COL_FIELD];
        } catch (Exception e) {
            return null;
        }
    }

    public String getObjectNameAtRow(int row) {
        try {
            return ((Class<?>) data[row][COL_FIELD]).getName();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isCellEditable(int row, int col) {
        return (col + 1) == COL_VALUE;
    }

    public Object getProbedObject() {
        return targetObj;
    }
}