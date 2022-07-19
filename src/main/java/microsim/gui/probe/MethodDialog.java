package microsim.gui.probe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Serial;
import java.lang.reflect.Method;

/**
 * Not of interest for users.A dialog used by the probe to get input parameters from user.
 */

public class MethodDialog extends JDialog {
    @Serial
    private static final long serialVersionUID = 1L;
    public boolean cancel;
    JPanel jPanelMain = new JPanel();
    JScrollPane jScrollPaneTable = new JScrollPane();
    MethodParameterDataModel parameters;
    JTable jTableMethods = new JTable();
    JButton jBtnCancel = new JButton();
    JButton jBtnExecute = new JButton();

    public MethodDialog(Frame frame, String title, boolean modal, Method m) {
        super(frame, title, modal);
        try {
            parameters = new MethodParameterDataModel(m);
            jTableMethods.setModel(parameters);

            for (int i = 0; i < jTableMethods.getColumnModel().getColumnCount(); i++)
                jTableMethods.getColumnModel().getColumn(i).setHeaderValue(parameters.getHeaderText(i));

            jbInit();
            setSize(200, 200);
            setLocation(200, 200);
            setTitle("Enter parameters for method " + m.toString());
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public MethodDialog(Method m) {
        this(null, "", false, m);
    }

    void jbInit() throws Exception {
        jBtnExecute.setText("Execute");
        jBtnExecute.addActionListener(this::jBtnExecute_actionPerformed);
        jBtnCancel.setText("Cancel");
        jBtnCancel.addActionListener(this::jBtnCancel_actionPerformed);
        this.getContentPane().add(jPanelMain, BorderLayout.SOUTH);
        jPanelMain.add(jBtnCancel, null);
        jPanelMain.add(jBtnExecute, null);
        this.getContentPane().add(jScrollPaneTable, BorderLayout.CENTER);
        jScrollPaneTable.getViewport().add(jTableMethods, null);
    }

    void jBtnCancel_actionPerformed(ActionEvent e) {
        cancel = true;
        dispose();
    }

    void jBtnExecute_actionPerformed(ActionEvent e) {
        cancel = false;
        dispose();
    }

    public Object[] getParameters() {
        return parameters.getParams();
    }
}
