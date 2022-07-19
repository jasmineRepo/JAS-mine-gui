package microsim.gui.probe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The probe window class. It is able to inspect content of objects. If the probed object implements the ProbeFields
 * interface the inspected fields are the only ones specified by the getProbeFields() method. Otherwise, the object will
 * be completely inspected.
 */

public class ProbeFrame extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;
    protected Object probedObject;
    // Main Frame
    JTabbedPane jTabbedPaneMain = new JTabbedPane();
    JPanel jPanelLowerButtons = new JPanel();
    // First tab
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPaneVariables = new JPanel();
    JTable jTableVariables = new JTable();
    JScrollPane jScrollVariables = new JScrollPane(jTableVariables);
    JPanel jPaneVariablesButtons = new JPanel();
    JButton jBtnNewProbe = new JButton();
    JButton jBtnListValues = new JButton();
    // Second tab
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel jPaneMethods = new JPanel();
    JList<Method> jListMethods = new JList<>();
    JScrollPane jScrollMethods = new JScrollPane(jListMethods);
    JButton jBtnInvoke = new JButton();
    // Lower buttons
    JButton jBtnOK = new JButton();
    JButton jBtnRefresh = new JButton();
    JToggleButton jBtnPrivate = new JToggleButton();
    FlowLayout flowLayout1 = new FlowLayout();
    JPanel jNorthPanel = new JPanel();
    JLabel jObjectName = new JLabel("");
    BorderLayout borderLayout3 = new BorderLayout();
    JComboBox<String> jCmbSuperclass = new JComboBox<>();
    private String frameName = "";
    private String objectContent = "";
    private VariableDataModel variables;
    private MethodsDataModel methods;
    private List<PanelObjectCollection> openedPanels;

    /**
     * This constructor checks if the given object implements the ProbeFields interface.
     *
     * @param o    The object to probe.
     * @param name The title of the frame window.
     */
    public ProbeFrame(Object o, String name) {
        if (o instanceof ProbeFields) {
            variables = new VariableDataModel(o);
            methods = new MethodsDataModel(o);
            setup(o, name);
            jBtnPrivate.setVisible(false);
            jCmbSuperclass.setVisible(false);
            jNorthPanel.setPreferredSize(new Dimension(200, 22));
        } else {
            variables = new VariableDataModel(o, true);
            methods = new MethodsDataModel(o, true);
            setup(o, name);
        }
    }

    /**
     * This constructor ignores the ProbeFields interface and shows all the fields of the object.
     *
     * @param o             The object to probe.
     * @param name          The title of the frame window.
     * @param privateFields If true the probe will show only the public
     *                      properties and method. If false it will be shown public, protected
     *                      and private fields.
     */
    public ProbeFrame(Object o, String name, boolean privateFields) {
        variables = new VariableDataModel(o, privateFields);
        methods = new MethodsDataModel(o, privateFields);
        setup(o, name);
    }

    private void setup(Object o, String name) {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        if (o == null) {
            this.dispose();
            return;
        }

        probedObject = o;
        frameName = name;
        objectContent = o.getClass().getName() + " (" + o + ")";

        jTableVariables.setModel(variables);
        jListMethods.setModel(methods);
        openedPanels = new ArrayList<>();

        try {
            jbInit();

            if (ProbeReflectionUtils.isCollection(o.getClass()) || o.getClass().isArray())
                addCollectionPanel(o);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Class<?> cl = o.getClass();
        while (cl != null) {
            jCmbSuperclass.addItem(cl.getName());
            cl = cl.getSuperclass();
        }
    }

    /**
     * Show off the frame window.
     */
    public void dispose() {
        probedObject = null;
        variables = null;
        methods = null;
        openedPanels.clear();
        super.dispose();
    }

    private void jbInit() throws Exception {
        //Build variable tab
        jPaneVariables.setLayout(borderLayout1);
        jTableVariables.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        jTableVariables.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                jTableVariables_mouseClicked(e);
            }
        });

        for (int i = 0; i < jTableVariables.getColumnModel().getColumnCount(); i++)
            jTableVariables.getColumnModel().getColumn(i).setHeaderValue(
                    variables.getHeaderText(i));

        jBtnNewProbe.setText("Open probe on selected variable");
        jBtnNewProbe.addActionListener(this::jBtnNewProbe_actionPerformed);
        jBtnListValues.setText("List values");
        jBtnListValues.addActionListener(this::jBtnListValues_actionPerformed);
        jBtnPrivate.setSelected(true);
        jBtnPrivate.setText("Private");
        jBtnPrivate.addActionListener(this::jBtnPrivate_actionPerformed);
        jListMethods.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                jListMethods_mouseClicked(e);
            }
        });
        jBtnPrivate.setSelected(false);
        jPaneVariablesButtons.setLayout(flowLayout1);
        jObjectName.setText(objectContent);
        jNorthPanel.setLayout(borderLayout3);
        jCmbSuperclass.setMinimumSize(new Dimension(200, 22));
        jCmbSuperclass.setPreferredSize(new Dimension(200, 22));
        jCmbSuperclass.addActionListener(this::jCmbSuperclass_actionPerformed);
        jNorthPanel.setPreferredSize(new Dimension(200, 44));
        jPaneVariablesButtons.add(jBtnListValues, null);
        jPaneVariablesButtons.add(jBtnNewProbe, null);
        jPaneVariables.add(jPaneVariablesButtons, BorderLayout.SOUTH);
        jPaneVariables.add(jScrollVariables, BorderLayout.CENTER);

        // Build methods tab
        jPaneMethods.setLayout(borderLayout2);
        jBtnInvoke.setText("Execute selected method");
        jBtnInvoke.setVerticalAlignment(SwingConstants.BOTTOM);
        jBtnInvoke.addActionListener(this::jBtnInvoke_actionPerformed);
        jPaneMethods.add(jBtnInvoke, BorderLayout.SOUTH);
        jPaneMethods.add(jScrollMethods, BorderLayout.CENTER);
        this.getContentPane().add(jNorthPanel, BorderLayout.NORTH);

        // Build main frame

        jBtnOK.setText("Close");
        jBtnOK.addActionListener(this::jBtnOK_actionPerformed);
        jBtnRefresh.setText("Refresh");
        jBtnRefresh.addActionListener(this::jBtnRefresh_actionPerformed);
        this.getContentPane().add(jPanelLowerButtons, BorderLayout.SOUTH);
        jPanelLowerButtons.add(jBtnPrivate, null);
        jPanelLowerButtons.add(jBtnRefresh, null);
        jPanelLowerButtons.add(jBtnOK, null);


        this.getContentPane().add(jTabbedPaneMain, BorderLayout.CENTER);
        jTabbedPaneMain.add(jPaneVariables, "Variables");
        jTabbedPaneMain.add(jPaneMethods, "Methods");
        jNorthPanel.add(jObjectName, BorderLayout.CENTER);

        setSize(new Dimension(414, 403));
        setLocation(0, 100);
        setTitle(frameName);
        jNorthPanel.add(jCmbSuperclass, BorderLayout.SOUTH);
    }

    void jBtnOK_actionPerformed(ActionEvent e) {
        this.dispose();
    }

    void jBtnRefresh_actionPerformed(ActionEvent e) {
        refreshData();
    }

    private void refreshData() {
        variables.update();
        jTableVariables.updateUI();
        methods.update();
        jListMethods.updateUI();

        for (PanelObjectCollection openedPanel : openedPanels) openedPanel.updateList();

    }

    void jBtnNewProbe_actionPerformed(ActionEvent e) {
        openNewProbe();
    }

    void jBtnInvoke_actionPerformed(ActionEvent e) {
        invokeMethod();
    }

    void jBtnListValues_actionPerformed(ActionEvent e) {
        if (jTabbedPaneMain.getSelectedComponent() != jPaneVariables) return;

        if (jTableVariables.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(null, "Please select a variable to list first.", "Probe variable",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object o = variables.getObjectAtRow(jTableVariables.getSelectedRow());
        if (o == null) {
            JOptionPane.showMessageDialog(null, "The selected variable is null.", "Probe variable",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (ProbeReflectionUtils.isCollection(o.getClass()) || o.getClass().isArray()) {
            String s = variables.getObjectNameAtRow(jTableVariables.getSelectedRow());
            addCollectionPanel(o, s);
        } else {
            JOptionPane.showMessageDialog(null, "The selected variable is not a collection.", "Probe variable",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addCollectionPanel(Object o) {
        PanelObjectCollection pn = new PanelObjectCollection(o);
        jTabbedPaneMain.add(pn, "List values", 0);
        openedPanels.add(pn);
    }

    private void addCollectionPanel(Object o, String s) {
        for (PanelObjectCollection openedPanel : openedPanels)
            if (openedPanel.getProbedObject() == o)
                return;

        PanelObjectCollection pn = new PanelObjectCollection(o);
        jTabbedPaneMain.add(pn, s);
        jTabbedPaneMain.setSelectedIndex(jTabbedPaneMain.getTabCount() - 1);
        openedPanels.add(pn);
    }

    void jBtnPrivate_actionPerformed(ActionEvent e) {
        variables.setViewPrivate(jBtnPrivate.isSelected());
        methods.setViewPrivate(jBtnPrivate.isSelected());
        refreshData();
    }

    private void openNewProbe() {
        if (jTabbedPaneMain.getSelectedComponent() != jPaneVariables) return;

        if (jTableVariables.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(null, "Please select a variable to probe first.", "Probe variable",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object o = variables.getObjectAtRow(jTableVariables.getSelectedRow());
        if (o == null) {
            JOptionPane.showMessageDialog(null, "The selected variable is null.", "Probe variable",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String s = variables.getObjectNameAtRow(jTableVariables.getSelectedRow());
        ProbeFrame pF = new ProbeFrame(o, this.getTitle() + "." + s);
        pF.setVisible(true);
    }

    private void invokeMethod() {
        Object[] params = {};
        if (jListMethods.getSelectedIndex() == -1) return;

        Method m = methods.getElementAt(jListMethods.getSelectedIndex());
        if (!ProbeReflectionUtils.isAnExecutableMethod(m)) {
            JOptionPane.showMessageDialog(null,
                    "Sorry but this method requires complex arguments.\nThis function is not yet implemented.",
                    "Invoke method", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (m.getParameterTypes().length > 0) {
            MethodDialog md = new MethodDialog(null, "P", true, m);
            md.setVisible(true);
            if (md.cancel) return;
            params = md.getParameters();
        }

        if (jListMethods.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(null, "Please select a method to invoke first.", "Invoke method",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (m.getParameterTypes().length > 0) methods.invokeMethodAt(jListMethods.getSelectedIndex(), params);
        else methods.invokeMethodAt(jListMethods.getSelectedIndex());
    }

    void jTableVariables_mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) openNewProbe();
    }

    void jListMethods_mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) invokeMethod();
    }

    void jCmbSuperclass_actionPerformed(ActionEvent e) {
        if (jCmbSuperclass.getSelectedIndex() < 0) return;
        variables.setDeepLevel(jCmbSuperclass.getSelectedIndex());
        methods.setDeepLevel(jCmbSuperclass.getSelectedIndex());
        refreshData();

    }

}