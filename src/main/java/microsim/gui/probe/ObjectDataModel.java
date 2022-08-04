package microsim.gui.probe;

import lombok.extern.java.Log;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.Serial;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * A data model used to contain the list of elements within a collection. It is used by the Probe frame.
 */
@Log
public class ObjectDataModel extends AbstractTableModel {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int COLUMNS = 3;

    private static final int COL_FIELD = 0;
    private static final int COL_VALUE = 1;
    private static final int COL_TYPE = 2;
    Object targetObj;
    Object[][] data;
    private boolean isAnArray; //fixme

    public ObjectDataModel(Object objToInspect) {


        if (objToInspect.getClass().isArray() || ProbeReflectionUtils.isCollection(objToInspect.getClass())) {
            targetObj = objToInspect;
            update();
        } else
            log.log(Level.SEVERE, "You were trying to build an ObjectDataModel passing a wrong object type");
    }

    public void update() {
        if (isAnArray == targetObj.getClass().isArray()) updateAnArray();
        else updateAList();
    }

    private void updateAnArray() {
        data = new Object[Array.getLength(targetObj)][COLUMNS];
        for (int i = 0; i < Array.getLength(targetObj); i++) {
            Object o = Array.get(targetObj, i);
            data[i][COL_VALUE] = o.toString();
            data[i][COL_TYPE] = o.getClass().getName();
            data[i][COL_FIELD] = o;
        }
    }


    private void updateAList() {
        Collection<?> c = (Collection<?>) targetObj;

        data = new Object[c.size()][COLUMNS];
        Iterator<?> itr = c.iterator();
        int i = 0;
        while (itr.hasNext()) {
            Object o = itr.next();
            data[i][COL_VALUE] = o.toString();
            data[i][COL_TYPE] = o.getClass().getName();
            data[i][COL_FIELD] = o;
            i++;
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
                JOptionPane.showMessageDialog(null, "The variable is null.\n It is impossible to edit.",
                        "Probe editing variable", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (ProbeReflectionUtils.isEditable(f.getClass()))
                if (isAnArray) setPrimitiveValueToArray(f, val, row);
                else ProbeReflectionUtils.setValueToObject(f, val);
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

    private void setPrimitiveValueToArray(Object o, Object val, int row) {
        switch (o) {
            case String ignored -> Array.set(targetObj, row, val);
            case Integer ignored -> Array.setInt(targetObj, row, Integer.parseInt(val.toString()));
            case Double ignored -> Array.setDouble(targetObj, row, Double.parseDouble(val.toString()));
            case Boolean ignored -> Array.setBoolean(targetObj, row, Boolean.parseBoolean(val.toString()));
            case Byte ignored -> Array.setByte(targetObj, row, Byte.parseByte(val.toString()));
            case Character ignored -> Array.setChar(targetObj, row, val.toString().charAt(0));
            case Float ignored -> Array.setFloat(targetObj, row, Float.parseFloat(val.toString()));
            case Long ignored -> Array.setLong(targetObj, row, Long.parseLong(val.toString()));
            case Short ignored -> Array.setShort(targetObj, row, Short.parseShort(val.toString()));
            case null, default -> {
            }
        }
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
            return data[row][COL_FIELD].getClass().getName();
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
