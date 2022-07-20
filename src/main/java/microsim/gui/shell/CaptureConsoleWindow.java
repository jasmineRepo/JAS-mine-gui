package microsim.gui.shell;


import lombok.val;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serial;

/**
 * An independent frame that is able to grab System.out and System.err streams, showing their content in a window.
 * It is useful when the application is launched with <i>javaw.exe</i> command, without terminal console.
 * It is possible to save the output in a file.
 */
public class CaptureConsoleWindow extends JInternalFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    ConsoleTextArea cta = null;
    javax.swing.JScrollPane jScrollText = null;

    private javax.swing.JToolBar jToolBar = null;
    private javax.swing.JButton jBtnClear = null;
    private javax.swing.JButton jBtnSave = null;
    private javax.swing.JToggleButton jBtnRead = null;

    private javax.swing.JPanel jContentPane = null;

    public CaptureConsoleWindow() {
        initialize();
    }

    private void initialize() {
        this.setContentPane(getJContentPane());
        this.setSize(508, 263);
        val iconLink = getClass().getResource("/microsim/gui/icons/tree.gif");
        if (iconLink != null) this.setFrameIcon(new javax.swing.ImageIcon(iconLink));
        this.setTitle("Output stream");
        this.setResizable(true);
        this.setMaximizable(false);
        this.setIconifiable(false);
    }


    private ConsoleTextArea getJConsoleTextArea() {
        if (cta == null) {
            try {
                cta = new ConsoleTextArea();
                cta.setFont(java.awt.Font.decode("monospaced"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cta;
    }

    private javax.swing.JScrollPane getJScrollText() {
        return (jScrollText == null) ? new JScrollPane(getJConsoleTextArea()) : jScrollText;
    }


    private void saveText() {
        JFileChooser jfc = new JFileChooser(new File("."));
        FileFilter ff = new FileFilter() {
            public boolean accept(File f) {
                return (f.getName().toLowerCase().endsWith(".txt") || f.isDirectory());
            }

            public String getDescription() {
                return "Text file (.txt)";
            }
        };
        jfc.setFileFilter(ff);

        int result = jfc.showSaveDialog(this);
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        try {
            BufferedWriter f = new BufferedWriter(new FileWriter(jfc.getSelectedFile()));
            f.write(cta.getText());

            f.close();
        } catch (Exception err) {
            String msg = "Error writing file:\n" + err.getMessage();
            //Modification by Ross	 (See J. Bloch "Effective Java" 2nd Edition, Item 5)
            JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void dispose() {
        cta.dispose();
    }

    private void clearText() {
        cta.setText("");
    }

    /**
     * This method initializes jToolBar
     *
     * @return javax.swing.JToolBar
     */
    private javax.swing.JToolBar getJToolBar() {
        if (jToolBar == null) {
            jToolBar = new javax.swing.JToolBar();
            jToolBar.add(getJBtnClear());
            jToolBar.addSeparator();
            jToolBar.add(getJBtnSave());
            jToolBar.addSeparator();
            jToolBar.add(getJBtnRead());
        }
        return jToolBar;
    }

    /**
     * This method initializes jBtnClear
     *
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getJBtnClear() {
        if (jBtnClear == null) {
            jBtnClear = new javax.swing.JButton();
            val iconLink =  getClass().getResource("/microsim/gui/icons/clear16.gif");
            if (iconLink != null) jBtnClear.setIcon(new ImageIcon(iconLink));
            jBtnClear.setToolTipText("Clear the content of the window");
            jBtnClear.addActionListener(e -> clearText());
        }
        return jBtnClear;
    }

    /**
     * This method initializes jBtnSave
     *
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getJBtnSave() {
        if (jBtnSave == null) {
            jBtnSave = new javax.swing.JButton();
            val iconLink = getClass().getResource("/microsim/gui/icons/Save16.gif");
            if (iconLink != null) jBtnSave.setIcon(new ImageIcon(iconLink));
            jBtnSave.setToolTipText("Save the text");
            jBtnSave.addActionListener(e -> saveText());
        }
        return jBtnSave;
    }

    public void changeReadingStatus() {
        if (cta.isReading()) cta.stopReading();
        else cta.startReading();

        jBtnRead.setSelected(cta.isReading());
    }

    /**
     * This method initializes jBtnRead
     *
     * @return javax.swing.JToggleButton
     */
    private javax.swing.JToggleButton getJBtnRead() {
        if (jBtnRead == null) {
            jBtnRead = new javax.swing.JToggleButton();
            val iconLink = getClass().getResource("/microsim/gui/icons/view.gif");
            if (iconLink != null) jBtnRead.setIcon(new javax.swing.ImageIcon(iconLink));
            jBtnRead.setToolTipText("Enable/disable output stream listening");
            jBtnRead.setSelected(true);
            jBtnRead.addActionListener(e -> changeReadingStatus());
        }
        return jBtnRead;
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
            jContentPane.add(getJScrollText(), java.awt.BorderLayout.CENTER);
            jContentPane.add(getJToolBar(), java.awt.BorderLayout.NORTH);
        }
        return jContentPane;
    }

    public void log(String message) {
        cta.log(message);
    }
}
