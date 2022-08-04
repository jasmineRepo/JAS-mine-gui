package microsim.gui.shell;

import microsim.data.db.DatabaseUtils;
import microsim.engine.SimulationEngine;
import org.h2.tools.Console;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.FileSystemException;
import java.sql.SQLException;

/**
 * The frame that controls engine parameters. It is shown when the 'Show engine status' menu item of the Control Panel
 * is chosen.
 */
public class DatabaseExplorerFrame extends JInternalFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    JButton jBtnClose = null;
    JButton jBtnDelete = null;
    JButton jBtnApply = null;
    JButton jBtnInit = null;
    JPanel jPanelProperties = null;
    JPanel jPanelButtons = null;

    JList<String> jList = null;
    File[] dirs = null;
    DefaultListModel<String> model = new DefaultListModel<>();

    private javax.swing.JPanel mainContentPane = null;

    /**
     * Constructor.
     *
     * @param engine The simulation engine to edit.
     */
    public DatabaseExplorerFrame(SimulationEngine engine) {
        initialize();
    }

    /**
     * Force deletion of directory
     *
     * @param path Path to the directory.
     * @return boolean
     */
    static private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) return false;
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();// fixme rework logic
                }
            }
        }
        return (path.delete());
    }

    private void initialize() {
        JScrollPane scrollPane = new JScrollPane(getMainContentPane());
        this.setContentPane(scrollPane);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setSize(new Dimension(450, 338));
        setTitle("Database Explorer");
        this.setResizable(true);

    }

    private JPanel getJPanelProperties() {
        if (jPanelProperties == null) {
            jPanelProperties = new JPanel();
            jPanelProperties.setLayout(new BorderLayout());

            jPanelProperties.add(new JLabel("Remember to disconnect database to come back"), BorderLayout.NORTH);
            jPanelProperties.add(getJList(), BorderLayout.CENTER);

        }
        return jPanelProperties;
    }

    private JList<String> getJList() {
        if (jList == null) {
            File outputDir = new File("output");
            dirs = outputDir.listFiles();
            if (dirs == null) dirs = new File[0];
            model.addElement("INPUT");
            for (File file : dirs) model.addElement(file.getName());
            jList = new JList<>(model);
        }
        return jList;
    }

    void jBtnApply_actionPerformed(ActionEvent e) {
        if (jList.getSelectedValue() == null) return;

        try {
            //Added ";MVCC=TRUE;DB_CLOSE_ON_EXIT=TRUE;FILE_LOCK=NO" in order to allow input database to be inspected,
            // closed and then the simulation to be run. Without this, an exception is thrown as the database is still connected.
            var dbString = "jdbc:h2:%s";
            dbString = dbString.formatted((jList.getSelectedIndex() == 0) ?
                    "input/input;MVCC=TRUE;DB_CLOSE_ON_EXIT=TRUE;FILE_LOCK=NO" :
                    "output/" + jList.getSelectedValue() + "/database/out;AUTO_SERVER=TRUE");
            new Console().runTool("-url", dbString, "-user", "sa", "-password", "");
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    void jBtnDelete_actionPerformed(ActionEvent e) {
        if (jList.getSelectedValue() == null) return;

        try {
            if (jList.getSelectedIndex() == 0) {
                //Don't delete input database!
                System.out.println("Only output databases can be deleted via the GUI!");
            } else {
                int indexToDelete = -1;
                for (int i = 0; i < dirs.length; i++) {
                    //Cannot use jList.getSelectedIndex() to find dirs as index of dirs array is not updated after an element is deleted, unlike jList.
                    if (dirs[i].getName().equals(jList.getSelectedValue())) {
                        indexToDelete = i;
                        break;
                    }
                }
                if ((indexToDelete != -1) && deleteDirectory(dirs[indexToDelete].getAbsoluteFile())) {
                    //Note that dirs doesn't contain "INPUT" as first entry, unlike model.
                    model.removeElementAt(jList.getSelectedIndex());
                } else {
                    throw new FileSystemException("Database cannot be deleted; check that the database is not in use!" +
                            " Try again, after closing all connections to the database or restarting the GUI.");
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    void jBtnClose_actionPerformed(ActionEvent e) {
        dispose();
    }

    void jBtnInit_actionPerformed(ActionEvent e) {
        DatabaseUtils.databaseInputUrl = "input/input";
        DatabaseUtils.inputSchemaUpdateEntityManger();
        try {
            new Console().runTool("-url", "jdbc:h2:input/input;AUTO_SERVER=TRUE", "-user", "sa", "-password", "");
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    private javax.swing.JPanel getMainContentPane() {
        if (mainContentPane == null) {
            mainContentPane = new javax.swing.JPanel();
            mainContentPane.setLayout(new java.awt.BorderLayout());
            mainContentPane.add(getJPanelProperties(), java.awt.BorderLayout.CENTER);
            mainContentPane.add(getJPanelButtons(), java.awt.BorderLayout.NORTH);
        }
        return mainContentPane;
    }

    private javax.swing.JPanel getJPanelButtons() {
        if (jPanelButtons == null) {
            jPanelButtons = new javax.swing.JPanel();
            jPanelButtons.add(getJBtnInit(), null);
            jPanelButtons.add(getJBtnApply(), null);
            jPanelButtons.add(getJBtnDelete(), null);
            jPanelButtons.add(getJBtnClose(), null);
        }
        return jPanelButtons;
    }

    private javax.swing.JButton getJBtnClose() {
        if (jBtnClose == null) {
            jBtnClose = new javax.swing.JButton();
            jBtnClose.setText("Close");
            jBtnClose.addActionListener(this::jBtnClose_actionPerformed);
        }
        return jBtnClose;
    }

    private javax.swing.JButton getJBtnDelete() {
        if (jBtnDelete == null) {
            jBtnDelete = new javax.swing.JButton();
            jBtnDelete.setText("Delete database");
            jBtnDelete.addActionListener(this::jBtnDelete_actionPerformed);
        }
        return jBtnDelete;
    }

    private javax.swing.JButton getJBtnApply() {
        if (jBtnApply == null) {
            jBtnApply = new javax.swing.JButton();
            jBtnApply.setText("Show database");
            jBtnApply.addActionListener(this::jBtnApply_actionPerformed);
        }
        return jBtnApply;
    }

    private javax.swing.JButton getJBtnInit() {
        if (jBtnInit == null) {
            jBtnInit = new javax.swing.JButton();
            jBtnInit.setText("Init input database");
            jBtnInit.addActionListener(this::jBtnInit_actionPerformed);
        }
        return jBtnInit;
    }
}
