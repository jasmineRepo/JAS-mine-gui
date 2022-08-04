package microsim.gui.space;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Serial;

/**
 * It is the properties frame called by the LayeredSurfaceFrame when the users press the 'Properties' button.
 */
public class LayeredSurfaceProperties extends JDialog {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int MAX_CELL_LENGTH = 8;
    private final java.util.List<LayerDrawer> displayLayers;
    public boolean modified;
    public int newCellSize;
    JPanel jpanel = new JPanel();
    JPanel jSizePanel = new JPanel();
    JPanel jMainPanel = new JPanel();
    JPanel jButtonPanel = new JPanel();
    JButton jBtnCancel = new JButton();
    JButton jBtnOK = new JButton();
    JComboBox<String> jCmbSize = new JComboBox<>();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    TitledBorder titledBorder1;
    GridLayout gridLayout1 = new GridLayout();

    public LayeredSurfaceProperties(Frame frame, String title, int cellSize, java.util.List<LayerDrawer> layers) {
        super(frame, title, true);

        displayLayers = layers;

        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (int i = 1; i <= MAX_CELL_LENGTH; i++) jCmbSize.addItem("" + i);
        jCmbSize.setSelectedIndex(cellSize - 1);

        for (LayerDrawer lay : displayLayers) {
            JCheckBox jc = new JCheckBox(lay.getDescription());
            jc.setSelected(lay.isDisplayed());
            jMainPanel.add(jc);
        }

        this.setSize(300, 300);
        modified = false;
    }

    public LayeredSurfaceProperties() {
        this(null, "", 1, null);
    }

    void jbInit() {
        titledBorder1 = new TitledBorder("");
        jButtonPanel.setBorder(BorderFactory.createEtchedBorder());
        jButtonPanel.setMinimumSize(new Dimension(85, 40));
        jButtonPanel.setPreferredSize(new Dimension(85, 40));
        jBtnCancel.setText("Cancel");
        jBtnCancel.addActionListener(this::jBtnCancel_actionPerformed);
        jBtnOK.setText("OK");
        jBtnOK.addActionListener(this::jBtnOK_actionPerformed);
        jCmbSize.setPreferredSize(new Dimension(100, 22));
        jLabel1.setText("Cell width");
        jMainPanel.setLayout(gridLayout1);
        jLabel2.setText("Current layers:");
        jpanel.setBorder(BorderFactory.createEtchedBorder());
        jSizePanel.setBorder(BorderFactory.createEtchedBorder());
        gridLayout1.setColumns(1);
        gridLayout1.setRows(10);
        getContentPane().add(jpanel, BorderLayout.CENTER);
        this.getContentPane().add(jSizePanel, BorderLayout.NORTH);
        this.getContentPane().add(jButtonPanel, BorderLayout.SOUTH);
        jButtonPanel.add(jBtnCancel, null);
        jButtonPanel.add(jBtnOK, null);
        jpanel.add(jMainPanel, null);
        jMainPanel.add(jLabel2, null);
        jSizePanel.add(jLabel1, null);
        jSizePanel.add(jCmbSize, null);

        this.setSize(new Dimension(300, 300));
        this.setLocation(200, 200);
    }

    void jBtnCancel_actionPerformed(ActionEvent e) {
        dispose();
    }

    void jBtnOK_actionPerformed(ActionEvent e) {
        LayerDrawer lay;
        for (LayerDrawer displayLayer : displayLayers) {
            lay = displayLayer;
            lay.setDisplayed(getStatusCheck(lay.getDescription()));
        }

        modified = true;
        newCellSize = jCmbSize.getSelectedIndex() + 1;
        dispose();
    }

    private boolean getStatusCheck(String checkName) {
        Component[] cs = jMainPanel.getComponents();
        for (Component c : cs)
            if (c instanceof JCheckBox)
                if (((JCheckBox) c).getText().equals(checkName)) return ((JCheckBox) c).isSelected();

        return false;
    }
}
