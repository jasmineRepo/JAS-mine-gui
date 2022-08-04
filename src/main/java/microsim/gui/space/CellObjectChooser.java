package microsim.gui.space;

import microsim.gui.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.Serial;

/**
 * A window used by LayeredSurfaceFrame to choose the object to be probed when user click on a cell of a
 * LayerMultiObjectGridDrawer.
 */
public class CellObjectChooser extends JDialog {

    @Serial
    private static final long serialVersionUID = 1L;
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JList<Object> jListObjects = new JList<>();
    JPanel jPanel1 = new JPanel();
    JButton jBtnCancel = new JButton();
    JButton jBtnOK = new JButton();

    public CellObjectChooser(Object[] objs, Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        jListObjects.setListData(objs);
        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((d.width - getSize().width) / 2, (d.height - getSize().height) / 2);
    }

    public CellObjectChooser(Object[] objs) {
        this(objs, null, "", false);
    }

    void jbInit() throws Exception {
        panel1.setLayout(borderLayout1);
        jBtnCancel.setText("Cancel");
        jBtnCancel.addActionListener(this::jBtnCancel_actionPerformed);
        jBtnOK.setText("Open probe");
        jBtnOK.addActionListener(this::jBtnOK_actionPerformed);
        jListObjects.setBorder(BorderFactory.createEtchedBorder());
        jListObjects.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                jListObjects_mouseClicked(e);
            }
        });
        getContentPane().add(panel1);
        panel1.add(jListObjects, BorderLayout.CENTER);
        this.getContentPane().add(jPanel1, BorderLayout.SOUTH);
        jPanel1.add(jBtnCancel, null);
        jPanel1.add(jBtnOK, null);
    }

    void jListObjects_mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2)
            if (jListObjects.getSelectedValue() != null) {
                GuiUtils.openProbe(jListObjects.getSelectedValue(), "Selected object");
                dispose();
            }
    }

    void jBtnCancel_actionPerformed(ActionEvent e) {
        dispose();
    }

    void jBtnOK_actionPerformed(ActionEvent e) {
        if (jListObjects.getSelectedValue() != null)
            GuiUtils.openProbe(jListObjects.getSelectedValue(), "Selected object");
        else return;
        dispose();
    }
}