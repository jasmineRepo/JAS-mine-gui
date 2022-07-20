package microsim.gui.shell;

import lombok.val;
import microsim.engine.EngineListener;
import microsim.engine.SimulationEngine;
import microsim.engine.SimulationManager;
import microsim.event.SystemEventType;
import microsim.exception.SimulationException;
import microsim.gui.GuiUtils;
import microsim.gui.shell.parameter.ParameterFrame;
import microsim.gui.shell.parameter.ParameterInspector;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.Serial;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * The JAS object is tne main GUI window. It represents the simulation environment for the user's simulation models.
 */
public class MicrosimShell extends JFrame {

    public static final double scale = 1.;

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String settingsFileName = "jas.ini";
    public static MicrosimShell currentShell;
    private final SimulationController controller = new SimulationController(this);
    private CaptureConsoleWindow consoleWindow = null;
    private javax.swing.JPanel jContentPane = null;
    private javax.swing.JMenuBar jJMenuBar = null;
    private javax.swing.JPanel jPanelTop = null;
    private javax.swing.JToolBar jToolBar = null;
    private javax.swing.JLabel jLabelTime = null;
    private javax.swing.JMenuItem jMenuFileExit = null;
    private javax.swing.JButton jBtnBuild = null;
    private javax.swing.JButton jBtnReload = null;
    private javax.swing.JButton jBtnPlay = null;
    private javax.swing.JButton jBtnStep = null;
    private javax.swing.JButton jBtnPause = null;
    private javax.swing.JButton jBtnUpdateParams = null;
    private javax.swing.JMenu jMenuSimulation = null;
    private javax.swing.JMenu jMenuTools = null;
    private javax.swing.JMenu jMenuHelp = null;
    private javax.swing.JMenuItem jMenuSimulationRestart = null;
    private javax.swing.JMenuItem jMenuSimulationPlay = null;
    private javax.swing.JMenuItem jMenuSimulationStep = null;
    private javax.swing.JMenuItem jMenuSimulationPause = null;
    private javax.swing.JMenuItem jMenuSimulationUpdateParams = null;
    private javax.swing.JMenuItem jMenuSimulationStop = null;
    private javax.swing.JMenuItem jMenuSimulationBuild = null;
    private javax.swing.JMenuItem jMenuSimulationEngine = null;
    private javax.swing.JPanel jPanelTime = null;
    private javax.swing.JLabel jLabelCurTime = null;
    private javax.swing.JPanel jPanelSlider = null;
    private javax.swing.JLabel jLabelSlider = null;
    private javax.swing.JCheckBox jSilentCheck = null;
    private javax.swing.JSlider jSlider = null;
    private javax.swing.JDesktopPane jDesktopPane = null; // fixme
    private javax.swing.JSplitPane jSplitPane = null;
    private javax.swing.JLabel jNullLabel = null; // fixme
    private javax.swing.JMenuItem jMenuToolsWindowPositions = null;
    private javax.swing.JMenuItem jMenuToolsDatabaseExplorer = null;
    private javax.swing.JMenuItem jMenuHelpAbout = null;
    private javax.swing.JSplitPane jSplitInternalDesktop = null;

    /**
     * This is the default full constructor
     */
    public MicrosimShell(SimulationEngine engine) {
        super();

        controller.openConfig();
        controller.attachToSimEngine(engine);

        initialize();

        setInitButtonStatus();

        currentShell = this;

        this.pack();
        this.setSize((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
                (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 30);
        this.setVisible(true);
        jSplitInternalDesktop.setDividerLocation(jSplitInternalDesktop.getHeight() * 4 / 5);
    }

    private static boolean isGetter(Method method) {
        if (!(method.getName().startsWith("get") || method.getName().startsWith("is"))) return false;
        if (method.getParameterTypes().length != 0) return false;
        return !void.class.equals(method.getReturnType());
    }

    private static boolean isSetter(Method method) {
        if (!method.getName().startsWith("set")) return false;
        return method.getParameterTypes().length == 1;
    }

    public SimulationController getController() {
        return controller;
    }

    /**
     * This method initializes this.
     */
    private void initialize() {
        this.setSize(727, 426);
        //Was left commented out by Michele, but I (Ross) think it's better to have this to allow the user to immediately see the parameter boxes at the top half of the shell.
        this.setContentPane(getJContentPane());
        this.setJMenuBar(getJJMenuBar());
        this.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/microsim/gui/icons/logo_2.png")));
        this.setTitle("JAS-mine");

        this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                controller.quitEngine();
            }
        });
        try {
            UIManager.setLookAndFeel("net.infonode.gui.laf.InfoNodeLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.out.println("Error loading L&F " + e);
        }

        consoleWindow = new CaptureConsoleWindow();
        jSplitInternalDesktop.setBottomComponent(consoleWindow);
        consoleWindow.show();
    }

    public void attachToSimEngine(SimulationEngine engine) {
        controller.attachToSimEngine(engine);
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    public javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new java.awt.BorderLayout());
            jContentPane.add(getJPanelTop(), java.awt.BorderLayout.NORTH);
            jContentPane.add(getJSplitPane(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes jJMenuBar
     *
     * @return javax.swing.JMenuBar
     */
    private javax.swing.JMenuBar getJJMenuBar() {
        if (jJMenuBar == null) {
            jJMenuBar = new javax.swing.JMenuBar();
            jJMenuBar.add(getJMenuSimulation());
            jJMenuBar.add(getJMenuTools());
            jJMenuBar.add(getJMenuHelp());
        }
        return jJMenuBar;
    }

    /**
     * This method initializes jPanelTop
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJPanelTop() {
        if (jPanelTop == null) {
            jPanelTop = new javax.swing.JPanel();
            jPanelTop.setLayout(new java.awt.BorderLayout());
            jPanelTop.add(getJToolBar(), java.awt.BorderLayout.NORTH);
            jPanelTop.add(getJPanelTime(), java.awt.BorderLayout.CENTER);
            jPanelTop.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        }
        return jPanelTop;
    }

    /**
     * This method initializes jToolBar
     *
     * @return javax.swing.JToolBar
     */
    private javax.swing.JToolBar getJToolBar() {
        if (jToolBar == null) {
            jToolBar = new javax.swing.JToolBar();
            jToolBar.addSeparator();
            jToolBar.add(getJBtnReload());
            jToolBar.addSeparator();
            jToolBar.add(getJBtnBuild());
            jToolBar.addSeparator();
            jToolBar.addSeparator();
            jToolBar.add(getJBtnPlay());
            jToolBar.add(getJBtnStep());
            jToolBar.add(getJBtnPause());
            jToolBar.add(getJBtnUpdateParameters());
            jToolBar.addSeparator();
//			jToolBar.add(getJSilentCheck());
// Ross: This has been removed in order to avoid misuse by inexperienced users, who might try to import/export to the
// database despite switching the connection off.  Now all JAS-mine models launched from the GUI will automatically
// have the database connection created.  If the user wants to turn this connection off, they can do so programmatically
// by setting turnOffDatabaseConnection to true in the Start class template of the simulation project created by the JAS-mine plugin for Eclipse IDE.
//			jToolBar.addSeparator();
            jToolBar.addSeparator(new Dimension(50, 30));
            jToolBar.add(getJPanelSlider());
            jToolBar.setPreferredSize(new java.awt.Dimension(414, 40));
        }
        return jToolBar;
    }

    /**
     * This method initializes jLabelTime
     *
     * @return javax.swing.JLabel
     */
    private javax.swing.JLabel getJLabelTime() {
        if (jLabelTime == null) {
            jLabelTime = new javax.swing.JLabel();
            jLabelTime.setFont(new Font(jLabelTime.getFont().getFontName(), jLabelTime.getFont().getStyle(), (int) (scale * jLabelTime.getFont().getSize())));
        }
        return jLabelTime;
    }

    /**
     * This method initializes jMenuFileExit
     *
     * @return javax.swing.JMenuItem
     */
    private javax.swing.JMenuItem getJMenuFileExit() {
        if (jMenuFileExit == null) {
            jMenuFileExit = new javax.swing.JMenuItem();
            jMenuFileExit.setText("Quit");
            val iconLink = getClass().getResource("/microsim/gui/icons/quit.gif");
            if (iconLink != null) jMenuFileExit.setIcon(new javax.swing.ImageIcon(iconLink));
            jMenuFileExit.addActionListener(e -> controller.quitEngine());
        }
        return jMenuFileExit;
    }

    /**
     * This method initializes jBtnBuild
     *
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getJBtnBuild() {
        if (jBtnBuild == null) {
            jBtnBuild = new javax.swing.JButton();
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_build.gif");
            if (iconLink != null) jBtnBuild.setIcon(new javax.swing.ImageIcon(iconLink));
            jBtnBuild.setToolTipText("Build simulation model");
            jBtnBuild.addActionListener(e -> controller.buildModel());
        }
        return jBtnBuild;
    }

    public void setTimeLabel(String newTime) {
        jLabelTime.setText(newTime);
    }

    /**
     * Enable and disable the simulation buttons as the initial state.
     */
    public void setInitButtonStatus() {
        jBtnPlay.setEnabled(false);
        jMenuSimulationPlay.setEnabled(false);
        jBtnStep.setEnabled(false);
        jMenuSimulationStep.setEnabled(false);
        jMenuSimulationStop.setEnabled(false);
        jMenuSimulationPause.setEnabled(false);
        jMenuSimulationUpdateParams.setEnabled(false);
        jBtnPause.setEnabled(false);
        jBtnUpdateParams.setEnabled(false);
        jBtnBuild.setEnabled(true);
        jMenuSimulationBuild.setEnabled(true);
        jMenuSimulationRestart.setEnabled(false);
        jMenuSimulation.revalidate();
        jMenuSimulation.repaint();

        getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Enable and disable the simulation buttons according to the built state.
     */
    public void setBuiltButtonStatus() {
        jBtnPlay.setEnabled(true);
        jMenuSimulationPlay.setEnabled(true);
        jBtnStep.setEnabled(true);
        jMenuSimulationStep.setEnabled(true);
        jMenuSimulationStop.setEnabled(true);
        jBtnBuild.setEnabled(false);
        jMenuSimulationPause.setEnabled(true);
        jMenuSimulationUpdateParams.setEnabled(true);
        jBtnPause.setEnabled(true);
        jBtnUpdateParams.setEnabled(true);
        jMenuSimulationBuild.setEnabled(false);
        jMenuSimulationRestart.setEnabled(true);
        jMenuSimulation.revalidate();
        jMenuSimulation.repaint();
    }

    /**
     * This method initializes jBtnReload
     *
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getJBtnReload() {
        if (jBtnReload == null) {
            jBtnReload = new javax.swing.JButton();
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_refresh.gif");
            if (iconLink != null) jBtnReload.setIcon(new javax.swing.ImageIcon(iconLink));
            jBtnReload.setToolTipText("Restart simulation model");
            jBtnReload.addActionListener(e -> controller.restartModel());
        }
        return jBtnReload;
    }

    /**
     * This method initializes jBtnPlay
     *
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getJBtnPlay() {
        if (jBtnPlay == null) {
            jBtnPlay = new javax.swing.JButton();
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_play.gif");
            if (iconLink != null) jBtnPlay.setIcon(new javax.swing.ImageIcon(iconLink));
            jBtnPlay.setToolTipText("Start simulation");
            jBtnPlay.addActionListener(e -> controller.startModel());
        }
        return jBtnPlay;
    }

    /**
     * This method initializes jBtnStep
     *
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getJBtnStep() {
        if (jBtnStep == null) {
            jBtnStep = new javax.swing.JButton();
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_step.gif");
            if (iconLink != null) jBtnStep.setIcon(new javax.swing.ImageIcon(iconLink));
            jBtnStep.setToolTipText("Execute next scheduled action");
            jBtnStep.addActionListener(e -> {
                try {
                    controller.doStep();
                } catch (SimulationException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            });
        }
        return jBtnStep;
    }

    /**
     * This method initializes jBtnPause
     *
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getJBtnPause() {
        if (jBtnPause == null) {
            jBtnPause = new javax.swing.JButton();
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_pause.gif");
            if (iconLink != null) jBtnPause.setIcon(new javax.swing.ImageIcon(iconLink));
            jBtnPause.setToolTipText("Pause simulation");
            jBtnPause.addActionListener(e -> controller.pauseModel());
        }
        return jBtnPause;
    }

    /**
     * This method initializes jBtnPause
     *
     * @return javax.swing.JButton
     */
    private javax.swing.JButton getJBtnUpdateParameters() {
        if (jBtnUpdateParams == null) {
            jBtnUpdateParams = new javax.swing.JButton();
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_update_params.png");
            if (iconLink != null) jBtnUpdateParams.setIcon(new javax.swing.ImageIcon(iconLink));
            jBtnUpdateParams.setToolTipText("Update parameters in the live simulation");
            jBtnUpdateParams.addActionListener(e -> controller.updateModelParams());
        }
        return jBtnUpdateParams;
    }

    /**
     * This method initializes jMenuSimulation
     *
     * @return javax.swing.JMenu
     */
    private javax.swing.JMenu getJMenuSimulation() {
        if (jMenuSimulation == null) {
            jMenuSimulation = new javax.swing.JMenu();
            jMenuSimulation.add(getJMenuSimulationBuild());
            jMenuSimulation.add(getJMenuSimulationRestart());
            jMenuSimulation.addSeparator();
            jMenuSimulation.add(getJMenuSimulationPlay());
            jMenuSimulation.add(getJMenuSimulationStep());
            jMenuSimulation.add(getJMenuSimulationPause());
            jMenuSimulation.add(getJMenuSimulationUpdateParams());
            jMenuSimulation.add(getJMenuSimulationStop());
            jMenuSimulation.addSeparator();
            jMenuSimulation.add(getJMenuSimulationEngine());
            jMenuSimulation.setText("Simulation");
            jMenuSimulation.setFont(new Font(jMenuSimulation.getFont().getFontName(),
                    jMenuSimulation.getFont().getStyle(), (int) (scale * jMenuSimulation.getFont().getSize())));
            jMenuSimulation.add(getJMenuFileExit());
        }
        return jMenuSimulation;
    }

    /**
     * This method initializes jMenuSimulationEngine
     *
     * @return javax.swing.JMenuItem
     */
    private javax.swing.JMenuItem getJMenuSimulationEngine() {
        if (jMenuSimulationEngine == null) {
            jMenuSimulationEngine = new javax.swing.JMenuItem();
            jMenuSimulationEngine.setText("Show engine status");
            jMenuSimulationEngine.addActionListener(e -> controller.showProperties());
        }
        return jMenuSimulationEngine;
    }

    /**
     * This method initializes jMenuTools
     *
     * @return javax.swing.JMenu
     */
    private javax.swing.JMenu getJMenuTools() {
        if (jMenuTools == null) {
            jMenuTools = new javax.swing.JMenu();
            jMenuTools.setText("Tools");
            jMenuTools.setFont(new Font(jMenuTools.getFont().getFontName(),
                    jMenuTools.getFont().getStyle(), (int) (scale * jMenuTools.getFont().getSize())));
            jMenuTools.add(getJMenuToolsWindowPositions());
            jMenuTools.add(getJMenuToolsDatabaseExplorer());
        }
        return jMenuTools;
    }

    /**
     * This method initializes jMenuHelp
     *
     * @return javax.swing.JMenu
     */
    private javax.swing.JMenu getJMenuHelp() {
        if (jMenuHelp == null) {
            jMenuHelp = new javax.swing.JMenu();
            jMenuHelp.add(getJMenuHelpAbout());
            jMenuHelp.setText("Help");
            jMenuHelp.setFont(new Font(jMenuHelp.getFont().getFontName(),
                    jMenuHelp.getFont().getStyle(), (int) (scale * jMenuHelp.getFont().getSize())));
        }
        return jMenuHelp;
    }

    /**
     * This method initializes jMenuSimulationRestart
     *
     * @return javax.swing.JMenuItem
     */
    private javax.swing.JMenuItem getJMenuSimulationRestart() { // todo replace repetitive button blocks
        if (jMenuSimulationRestart == null) {
            jMenuSimulationRestart = new javax.swing.JMenuItem();
            jMenuSimulationRestart.setText("Restart simulation");
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_refresh.gif");
            if (iconLink != null) jMenuSimulationRestart.setIcon(new javax.swing.ImageIcon(iconLink));
            jMenuSimulationRestart.addActionListener(e -> controller.restartModel());
        }
        return jMenuSimulationRestart;
    }

    /**
     * This method initializes jMenuSimulationPlay
     *
     * @return javax.swing.JMenuItem
     */
    private javax.swing.JMenuItem getJMenuSimulationPlay() {
        if (jMenuSimulationPlay == null) {
            jMenuSimulationPlay = new javax.swing.JMenuItem();
            jMenuSimulationPlay.setText("Play");
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_play.gif");
            if (iconLink != null) jMenuSimulationPlay.setIcon(new javax.swing.ImageIcon(iconLink));
            jMenuSimulationPlay.addActionListener(e -> controller.startModel());
        }
        return jMenuSimulationPlay;
    }

    /**
     * This method initializes jMenuSimulationStep
     *
     * @return javax.swing.JMenuItem
     */
    private javax.swing.JMenuItem getJMenuSimulationStep() {
        if (jMenuSimulationStep == null) {
            jMenuSimulationStep = new javax.swing.JMenuItem();
            jMenuSimulationStep.setText("Step");
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_step.gif");
            if (iconLink != null) jMenuSimulationStep.setIcon(new javax.swing.ImageIcon(iconLink));
            jMenuSimulationStep.addActionListener(e -> {
                try {
                    controller.doStep();
                } catch (SimulationException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            });
        }
        return jMenuSimulationStep;
    }

    /**
     * This method initializes jMenuSimulationPause
     *
     * @return javax.swing.JMenuItem
     */
    private javax.swing.JMenuItem getJMenuSimulationPause() {
        if (jMenuSimulationPause == null) {
            jMenuSimulationPause = new javax.swing.JMenuItem();
            jMenuSimulationPause.setText("Pause");
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_pause.gif");
            if (iconLink != null) jMenuSimulationPause.setIcon(new javax.swing.ImageIcon(iconLink));
            jMenuSimulationPause.addActionListener(e -> controller.pauseModel());
        }
        return jMenuSimulationPause;
    }

    /**
     * This method initializes jMenuSimulationPause
     *
     * @return javax.swing.JMenuItem
     */
    private javax.swing.JMenuItem getJMenuSimulationUpdateParams() {
        if (jMenuSimulationUpdateParams == null) {
            jMenuSimulationUpdateParams = new javax.swing.JMenuItem();
            jMenuSimulationUpdateParams.setText("Update Parameters");
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_update_params.png");
            if (iconLink != null) jMenuSimulationUpdateParams.setIcon(new javax.swing.ImageIcon(iconLink));
            jMenuSimulationUpdateParams.addActionListener(e -> controller.updateModelParams());
        }
        return jMenuSimulationUpdateParams;
    }

    /**
     * This method initializes jMenuSimulationStop
     *
     * @return javax.swing.JMenuItem
     */
    private javax.swing.JMenuItem getJMenuSimulationStop() {
        if (jMenuSimulationStop == null) {
            jMenuSimulationStop = new javax.swing.JMenuItem();
            jMenuSimulationStop.setText("Stop");
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_stop.gif");
            if (iconLink != null) jMenuSimulationStop.setIcon(new javax.swing.ImageIcon(iconLink));
            jMenuSimulationStop.addActionListener(e -> controller.stopModel());
        }
        return jMenuSimulationStop;
    }

    private javax.swing.JMenuItem getJMenuToolsWindowPositions() {
        if (jMenuToolsWindowPositions == null) {
            jMenuToolsWindowPositions = new javax.swing.JMenuItem();
            jMenuToolsWindowPositions.setText("Print window positions");
            val iconLink = getClass().getResource("/microsim/gui/icons/console.gif");
            if (iconLink != null) jMenuToolsWindowPositions.setIcon(new javax.swing.ImageIcon(iconLink));
            jMenuToolsWindowPositions.addActionListener(e -> controller.printWindowPositions());
        }
        return jMenuToolsWindowPositions;
    }

    private javax.swing.JCheckBox getJSilentCheck() {
        if (jSilentCheck == null) {
            jSilentCheck = new javax.swing.JCheckBox();
            jSilentCheck.setSelected(controller.isTurnOffDatabaseConnection());
            jSilentCheck.setText("Turn off database");
            jSilentCheck.setFont(new Font(jSilentCheck.getFont().getFontName(), jSilentCheck.getFont().getStyle(),
                    (int) (scale * jSilentCheck.getFont().getSize())));
            jSilentCheck.addActionListener(e -> {
                controller.setTurnOffDatabaseConnection(jSilentCheck.isSelected());
                jSilentCheck.setSelected(controller.isTurnOffDatabaseConnection());
            });
        }
        return jSilentCheck;
    }

    private javax.swing.JMenuItem getJMenuToolsDatabaseExplorer() {
        if (jMenuToolsDatabaseExplorer == null) {
            jMenuToolsDatabaseExplorer = new javax.swing.JMenuItem();
            jMenuToolsDatabaseExplorer.setText("Database explorer");
            val iconLink = getClass().getResource("/microsim/gui/icons/console.gif");
            if (iconLink != null) jMenuToolsDatabaseExplorer.setIcon(new javax.swing.ImageIcon(iconLink));
            jMenuToolsDatabaseExplorer.addActionListener(e -> controller.showDatabaseExplorer());
        }
        return jMenuToolsDatabaseExplorer;
    }

    /**
     * This method initializes jMenuSimulationBuild
     *
     * @return javax.swing.JMenuItem
     */
    private javax.swing.JMenuItem getJMenuSimulationBuild() {
        if (jMenuSimulationBuild == null) {
            jMenuSimulationBuild = new javax.swing.JMenuItem();
            jMenuSimulationBuild.setText("Build model");
            val iconLink = getClass().getResource("/microsim/gui/icons/simulation_build.gif");
            if (iconLink != null) jMenuSimulationBuild.setIcon(new javax.swing.ImageIcon(iconLink));
            jMenuSimulationBuild.addActionListener(e -> controller.buildModel());
        }
        return jMenuSimulationBuild;
    }

    /**
     * This method initializes jPanelTime
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJPanelTime() {
        if (jPanelTime == null) {
            jPanelTime = new javax.swing.JPanel();
            jPanelTime.setLayout(new java.awt.BorderLayout());
            jPanelTime.add(getJLabelCurTime(), java.awt.BorderLayout.WEST);
            jPanelTime.add(getJLabelTime(), java.awt.BorderLayout.CENTER);
        }
        return jPanelTime;
    }

    /**
     * This method initializes jLabelCurTime
     *
     * @return javax.swing.JLabel
     */
    private javax.swing.JLabel getJLabelCurTime() {
        if (jLabelCurTime == null) {
            jLabelCurTime = new javax.swing.JLabel();
            jLabelCurTime.setText("Current time:");
            jLabelCurTime.setFont(new java.awt.Font("Franklin Gothic Medium",
                    java.awt.Font.ITALIC, (int) (scale * 12)));
        }
        return jLabelCurTime;
    }

    /**
     * This method initializes jPanelSlider
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJPanelSlider() {
        if (jPanelSlider == null) {
            jPanelSlider = new javax.swing.JPanel();
            jPanelSlider.setLayout(null);
            jPanelSlider.add(getJLabelSlider(), null);
            jPanelSlider.add(getJSlider(), null);

            jPanelSlider.setSize(100, 30);
            jPanelSlider.setPreferredSize(new java.awt.Dimension(100, 30));
        }
        return jPanelSlider;
    }

    /**
     * This method initializes jLabelSlider
     *
     * @return javax.swing.JLabel
     */
    private javax.swing.JLabel getJLabelSlider() {
        if (jLabelSlider == null) {
            jLabelSlider = new javax.swing.JLabel();
            jLabelSlider.setBounds(0, 0, (int) (scale * 173), 18);
            jLabelSlider.setText(" Simulation speed: max");
            jLabelSlider.setFont(new java.awt.Font("Franklin Gothic Medium", java.awt.Font.PLAIN, (int) (scale * 12)));
            jLabelSlider.setName("jLabelSlider");
        }
        return jLabelSlider;
    }

    /**
     * This method initializes jSlider
     *
     * @return javax.swing.JSlider
     */
    private javax.swing.JSlider getJSlider() {
        if (jSlider == null) {
            jSlider = new javax.swing.JSlider();
            jSlider.setBounds(0, 18, (int) (scale * 173), 18);
            jSlider.setName("jSlider");
            jSlider.setMaximum(200);
            jSlider.setValue(0);
            jSlider.setInverted(true);
            jSlider.addChangeListener(e -> {
                int value = jSlider.getValue();
                val bFlag = value == jSlider.getMinimum();
                jLabelSlider.setText(" Simulation speed: %s".formatted(bFlag ? "max" : 200 - value));
                controller.changeEventTimeTreshold(value);
            });
        }
        return jSlider;
    }

    /**
     * This method initializes jDesktopPane
     *
     * @return javax.swing.JDesktopPane
     */
    public javax.swing.JDesktopPane getJDesktopPane() {
        return jDesktopPane == null ? new javax.swing.JDesktopPane() : jDesktopPane;
    }

    /**
     * This method initializes jSplitPane
     *
     * @return javax.swing.JSplitPane
     */
    private javax.swing.JSplitPane getJSplitPane() {
        if (jSplitPane == null) {
            jSplitPane = new javax.swing.JSplitPane();
            jSplitPane.setRightComponent(getJSplitInternalDesktop());
            jSplitPane.setLeftComponent(getJNullLabel());
            jSplitPane.setOneTouchExpandable(true);
        }
        return jSplitPane;
    }

    /**
     * This method initializes jNullLabel
     *
     * @return javax.swing.JLabel
     */
    private javax.swing.JLabel getJNullLabel() {
        return jNullLabel == null ? new javax.swing.JLabel() : jNullLabel;
    }

    /**
     * This method initializes jMenuHelpAbout
     *
     * @return javax.swing.JMenuItem
     */
    private javax.swing.JMenuItem getJMenuHelpAbout() {
        if (jMenuHelpAbout == null) {
            jMenuHelpAbout = new javax.swing.JMenuItem();
            jMenuHelpAbout.setText("About JAS-mine");
            val iconLink = getClass().getResource("/microsim/gui/icons/msIco.gif");
            if (iconLink != null) jMenuHelpAbout.setIcon(new javax.swing.ImageIcon(iconLink));
            jMenuHelpAbout.addActionListener(e -> (new AboutFrame()).setVisible(true));
        }
        return jMenuHelpAbout;
    }

    /**
     * This method initializes jSplitInternalDesktop
     *
     * @return javax.swing.JSplitPane
     */
    private javax.swing.JSplitPane getJSplitInternalDesktop() {
        if (jSplitInternalDesktop == null) {
            jSplitInternalDesktop = new javax.swing.JSplitPane();
            jSplitInternalDesktop.setTopComponent(getJDesktopPane());
            jSplitInternalDesktop.setBottomComponent(null);
            jSplitInternalDesktop.setSize(31, 59);
            jSplitInternalDesktop.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            jSplitInternalDesktop.setOneTouchExpandable(true);
        }
        return jSplitInternalDesktop;
    }

    public void log(String message) {
        consoleWindow.log(message);
    }

    public static class RootViews extends FileSystemView {

        public File[] getRoots() {
            File[] oldRoots = super.getRoots();
            File[] roots = new File[2 + oldRoots.length];
            System.arraycopy(oldRoots, 0, roots, 2, oldRoots.length);

            return roots;
        }

        public File createNewFolder(File arg0) {
            String newFolder = JOptionPane.showInputDialog(null, "Type the name of the new folder");
            if (newFolder == null) return null;

            File newFile = new File(arg0, newFolder);
            return newFile.mkdir() ? newFile : null;
        }
    }

    public class SimulationController implements EngineListener {
        private final MicrosimShell jasWindow;
        private final List<ParameterFrame> parameterFrames = new ArrayList<>();
        private SimulationEngine callerEngine;
        private Properties settings;

        public SimulationController(MicrosimShell owner) {
            jasWindow = owner;
        }

        private void openConfig() {
            settings = new Properties();
            settings.setProperty("LookAndFeel", "");

            settings.setProperty("ProjectsPath", "");
            settings.setProperty("EditorPath", "");

            try {
                settings.load(new java.io.FileInputStream(settingsFileName));
            } catch (java.io.IOException ignored) {
            }

            if (!settings.getProperty("ProjectsPath").equals("")) {
                File fl = new File(settings.getProperty("ProjectsPath"));
                if (!fl.exists())
                    JOptionPane.showMessageDialog(
                            jasWindow, "The JAS-mine projects path does no more exist on this file system.\n "
                                    + "Please check it from the Tool\\JAS Options menu");
            }
        }

        public void showProperties() {
            boolean st = callerEngine.isRunningStatus();
            callerEngine.pause();

            EngineParametersFrame paramFrame = new EngineParametersFrame(callerEngine);
            getJDesktopPane().add(paramFrame);
            paramFrame.show();
            callerEngine.setRunningStatus(st);
        }

        public boolean isTurnOffDatabaseConnection() {
            return callerEngine.isTurnOffDatabaseConnection();
        }

        public void setTurnOffDatabaseConnection(boolean turnOffDatabaseConnection) {
            callerEngine.setTurnOffDatabaseConnection(turnOffDatabaseConnection);
        }

        public void showDatabaseExplorer() {
            boolean st = callerEngine.isRunningStatus();
            callerEngine.pause();

            DatabaseExplorerFrame dbFrame = new DatabaseExplorerFrame(callerEngine);
            getJDesktopPane().add(dbFrame);
            dbFrame.show();
            callerEngine.setRunningStatus(st);
        }

        public void startModel() {
            callerEngine.startSimulation();
        }

        public void pauseModel() {
            callerEngine.pause();
        }

        public void updateModelParams() {
            for (ParameterFrame parameterFrame : parameterFrames) {
                parameterFrame.save();
            }
        }

        public void stopModel() {
            callerEngine.performAction(SystemEventType.Stop);
        }

        public void restartModel() {
            callerEngine.pause();

            closeCurrentModels();
            callerEngine.rebuildModels();

            setInitButtonStatus();
        }

        public void attachToSimEngine(SimulationEngine engine) {
            callerEngine = engine;
            callerEngine.addEngineListener(this);
        }

        public void doStep() throws SimulationException {
            callerEngine.pause();
            callerEngine.step();
        }

        public void changeEventTimeTreshold(int value) {
            callerEngine.setEventThresold(value);
        }

        public void navigateWebSite() {// fixme improve this
            String command, path = "https://www.jas-mine.net";
            command = (System.getProperty("file.separator").equals("/") ? "netscape " : "cmd /C start ") + path;
            try {
                Runtime.getRuntime().exec(command);
            } catch (Exception ignored) {
            }
        }

        private void quitEngine() {
            callerEngine.pause();
            callerEngine.quit();
        }

        public void closeCurrentModels() {
            try {
                JInternalFrame[] frames = getJDesktopPane().getAllFrames();
                for (JInternalFrame frame : frames) if (frame != consoleWindow) frame.dispose();
            } catch (Exception ignored) {
            }

            setTimeLabel("");
        }

        /**
         * Ask engine to build currently loaded models.
         */
        public void buildModel() {
            getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            callerEngine.buildModels();
            setBuiltButtonStatus();

            getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        /**
         * Return a reference to the settings list.
         *
         * @return An instance of the Java standard Properties class.
         */
        public Properties getSettings() {
            return settings;
        }

        public void printWindowPositions() {
            for (JInternalFrame jInternalFrame : jDesktopPane.getAllFrames()) {
                System.out.println(jInternalFrame.getTitle() + " [X,Y,W,H] " + jInternalFrame.getX() + ", " +
                        jInternalFrame.getY() + ", " + jInternalFrame.getWidth() + ", " + jInternalFrame.getHeight());
            }
        }

        public void onEngineEvent(SystemEventType event) {
            if (event.equals(SystemEventType.Step))
                getJLabelTime().setText("" + callerEngine.getEventQueue().getTime());
            else if (event.equals(SystemEventType.Setup)) {
                parameterFrames.clear();
                for (SimulationManager model : controller.callerEngine.getModelArray()) {
                    List<Field> fields = ParameterInspector.extractModelParameters(model.getClass());

                    //Check that getter and setter exists for each model parameter (to ensure ability to control via microsim.shell GUI)
                    HashSet<String> getters = new HashSet<>();
                    HashSet<String> setters = new HashSet<>();
                    Method[] methods = model.getClass().getMethods();

                    for (Method method : methods) {
                        if (isGetter(method)) {
                            getters.add(method.getName());
                        } else if (isSetter(method)) {
                            setters.add(method.getName());
                        }
                    }

                    for (Field modelParameter : fields) {
                        String modelParameterName = modelParameter.getName();
                        if (modelParameterName.length() > 1) {
                            if (Character.isLowerCase(modelParameterName.charAt(0)) &&
                                    Character.isUpperCase(modelParameterName.charAt(1))) {
                                if (!getters.contains("get" + modelParameterName)) {
                                    //handles cases for fields with a name whose first character is lower case,
                                    // followed by a capital letter, e.g. nWorkers.  In this case,
                                    // the Java Beans convention is for a getter called getnWorkers, instead of getNWorkers.
                                    if (!getters.contains("is" + modelParameterName)) {
                                        //handles case for boolean 'is' getter methods
                                        throw new RuntimeException("Model parameter " + modelParameterName +
                                                " has no getter method.  Please create a getter method called get" +
                                                modelParameterName + " in the " + model.getClass() +
                                                " to enable this model parameter to be read by the GUI.");
                                    }
                                }
                                if (!setters.contains("set" + modelParameterName)) {
                                    throw new RuntimeException("Model parameter " + modelParameterName +
                                            " has no setter method.  Please create a setter method called set" +
                                            modelParameterName + " in the " + model.getClass() +
                                            " to enable this model parameter to be controlled via the GUI.");
                                }
                            } else {
                                String capModelParameterName = modelParameterName.substring(0, 1).toUpperCase() + modelParameterName.substring(1);// fixme go through the duplicates
                                //Ensure first letter of name is capitalised
                                String getterName = "get" + capModelParameterName;
                                String setterName = "set" + capModelParameterName;

                                if (!getters.contains(getterName)) {
                                    if (!getters.contains("is" + capModelParameterName)) {
                                        //handles case for boolean 'is' getter methods
                                        throw new RuntimeException("Model parameter " + modelParameterName +
                                                " has no getter method.  Please create a getter method called " +
                                                getterName + " in the " + model.getClass() +
                                                " to enable this model parameter to be read by the GUI.");
                                    }
                                }
                                if (!setters.contains(setterName)) {
                                    throw new RuntimeException("Model parameter " + modelParameterName +
                                            " has no setter method.  Please create a setter method called " +
                                            setterName + " in the " + model.getClass() +
                                            " to enable this model parameter to be controlled via the GUI.");
                                }
                            }
                        } else {
                            //Still need to check that getter/setters exist for case where a single character is used for the model parameter name.
                            String capModelParameterName = modelParameterName.substring(0, 1).toUpperCase() + modelParameterName.substring(1);
                            //Ensure first letter of name is capitalised
                            String getterName = "get" + capModelParameterName;
                            String setterName = "set" + capModelParameterName;

                            if (!getters.contains(getterName)) {
                                if (!getters.contains("is" + capModelParameterName)) {
                                    //handles case for boolean 'is' getter methods
                                    throw new RuntimeException("Model parameter " + modelParameterName +
                                            " has no getter method.  Please create a getter method called " +
                                            getterName + " in the " + model.getClass() +
                                            " to enable this model parameter to be read by the GUI.");
                                }
                            }
                            if (!setters.contains(setterName)) {
                                throw new RuntimeException("Model parameter " + modelParameterName +
                                        " has no setter method.  Please create a setter method called " + setterName +
                                        " in the " + model.getClass() +
                                        " to enable this model parameter to be controlled via the GUI.");
                            }
                        }
                    }


                    if (fields.size() > 0) {
                        ParameterFrame parameterFrame = new ParameterFrame(model);
                        parameterFrame.setResizable(false);
                        //Now in scrollpane, cannot resize anyway, so set to false.
                        GuiUtils.addWindow(parameterFrame);
                        parameterFrames.add(parameterFrame);
                    }
                }
            } else if (event.equals(SystemEventType.Build)) {
                for (ParameterFrame parameterFrame : parameterFrames) {
                    parameterFrame.save();
                }
            }
        }

    }
}
