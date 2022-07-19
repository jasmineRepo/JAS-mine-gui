package microsim.gui.probe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.Serial;

/**
 * Not of interest for users. Its the panel containing the table using ObjectDataModel.
 */
public class PanelObjectCollection extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;
    BorderLayout borderLayout = new BorderLayout();
    ObjectDataModel dataModel;

    JButton jBtnNewProbe = new JButton();
    JScrollPane jScrollPaneObjects = new JScrollPane();
    JTable jTableObjects = new JTable();

    public PanelObjectCollection(Object o) {
        try {
            dataModel = new ObjectDataModel(o);
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        this.setLayout(borderLayout);
        jTableObjects.setModel(dataModel);
        jTableObjects.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                jTableObjects_mouseClicked(e);
            }
        });

        jBtnNewProbe.setText("Open probe on selected object");
        jBtnNewProbe.addActionListener(this::jBtnNewProbe_actionPerformed);

        for (int i = 0; i < jTableObjects.getColumnModel().getColumnCount(); i++)
            jTableObjects.getColumnModel().getColumn(i).setHeaderValue(dataModel.getHeaderText(i));

        this.add(jBtnNewProbe, BorderLayout.SOUTH);
        this.add(jScrollPaneObjects, BorderLayout.CENTER);
        jScrollPaneObjects.getViewport().add(jTableObjects, null);
    }

    void jBtnNewProbe_actionPerformed(ActionEvent e) {
        openNewProbe();
    }

    private void openNewProbe() {
        if (jTableObjects.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(null, "Please select an element to probe first.",
                    "Probe an element of a list", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object o = dataModel.getObjectAtRow(jTableObjects.getSelectedRow());
        if (o == null) {
            JOptionPane.showMessageDialog(null, "The selected element is null.", "Probe an element of a list",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String s = dataModel.getObjectNameAtRow(jTableObjects.getSelectedRow());
        ProbeFrame pF = new ProbeFrame(o, this + "." + s);
        pF.setVisible(true);
    }

    public void updateList() {
        dataModel.update();
        updateUI();
    }

    public Object getProbedObject() {
        return dataModel.getProbedObject();
    }

    void jTableObjects_mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) openNewProbe();
    }
}
