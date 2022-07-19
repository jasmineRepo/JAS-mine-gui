package microsim.gui.shell;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.Serial;
import java.util.Enumeration;
import java.util.Properties;

/**
 * The about frame of the JAS application.
 */
public class AboutFrame extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    ImageIcon imageIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit()
            .getImage(getClass().getResource("/microsim/gui/icons/msIco.gif")));

    javax.swing.JTabbedPane jTabbedPane = null;
    javax.swing.JPanel jMainPanel = null;

    javax.swing.JPanel jSystemPanel = null;
    javax.swing.JTable jSystemTable = null;
    javax.swing.JScrollPane jSystemScroll = null;

    private javax.swing.JPanel jContentPane = null;

    public AboutFrame() {
        initialize();
    }

    private void initialize() {
        this.setContentPane(getJContentPane());
        setIconImage(imageIcon.getImage());
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) ((d.getWidth() - 400) / 2), (int) ((d.getHeight() - 400) / 2));
        setSize(450, 450);
        setTitle("About JAS-mine");
    }

    public javax.swing.JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.setBorder(new TitledBorder(""));
            jTabbedPane.add(getJMainPanel(), "About JAS-mine");
            //jTabbedPane.add(getJLicensePanel(), "License");
            jTabbedPane.add(getJSystemPanel(), "System");
        }
        return jTabbedPane;
    }

    public javax.swing.JPanel getJMainPanel() {
        return jMainPanel == null ? new AboutPanel() : jMainPanel;
    }

    public javax.swing.JPanel getJSystemPanel() {
        if (jSystemPanel == null) {
            jSystemPanel = new JPanel();
            jSystemPanel.setBorder(BorderFactory.createEtchedBorder());
            jSystemPanel.setLayout(new BorderLayout());
            jSystemPanel.add(getJSystemScroll(), BorderLayout.CENTER);
        }
        return jSystemPanel;
    }

    public javax.swing.JTable getJSystemTable() {
        if (jSystemTable == null) {
            jSystemTable = new JTable(getSystem(), getSystemCols());
        }
        return jSystemTable;
    }

    public javax.swing.JScrollPane getJSystemScroll() {
        if (jSystemScroll == null) {
            jSystemScroll = new JScrollPane();
            jSystemScroll.setViewportView(getJSystemTable());
        }
        return jSystemScroll;
    }

    private Object[] getSystemCols() {
        return new Object[]{"Variable", "Value"};
    }

    private Object[][] getSystem() {
        Properties sysProp = System.getProperties();
        Object[][] systemProps = new Object[sysProp.size() + 2][2];

        systemProps[0][0] = "JVM total memory";
        systemProps[0][1] = (Runtime.getRuntime().totalMemory() / 1024) + " Kb";
        systemProps[1][0] = "Used JVM memory";
        systemProps[1][1] = (Runtime.getRuntime().freeMemory() / 1024) + " Kb";

        Enumeration<?> enumItem = sysProp.propertyNames();
        int i = 2;
        while (enumItem.hasMoreElements()) {
            String key = (String) enumItem.nextElement();
            systemProps[i][0] = key;
            systemProps[i][1] = sysProp.getProperty(key);
            i++;
        }

        return systemProps;
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new java.awt.BorderLayout());
            jContentPane.add(getJTabbedPane(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }
}
