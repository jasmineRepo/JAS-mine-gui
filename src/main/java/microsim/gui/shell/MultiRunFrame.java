package microsim.gui.shell;

import lombok.extern.java.Log;
import microsim.engine.EngineListener;
import microsim.engine.MultiRun;
import microsim.engine.MultiRunListener;
import microsim.engine.SimulationEngine;
import microsim.event.SystemEventType;
import org.jfree.chart.ui.LCBLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Serial;

/**
 * This class implements the multi run control panel shown by JAS when a MultiRun class is executed.
 */

@Log
public class MultiRunFrame extends JFrame implements MultiRunListener, EngineListener {

    @Serial
    private static final long serialVersionUID = 1L;
    private final LCBLayout borderLayout1 = new LCBLayout(3);
    private final JPanel jPanelNorth = new JPanel();
    private final JLabel jLblNumber = new JLabel();
    private final JLabel jLblRunNb = new JLabel();
    private final JLabel jLblCurrentStep = new JLabel();
    private final JLabel jLblCurrentStepLabel = new JLabel();
    private final JLabel jLblCurrentRun = new JLabel();
    private final JLabel jLblCurrentRunLabel = new JLabel();
    private final JProgressBar jBar = new JProgressBar();
    private final JButton jBtnQuit = new JButton();
    private final JPanel jPanelBtns = new JPanel();
    private final JButton jBtnStart = new JButton();
    private final int maxRuns;
    private final MultiRun test;
    private int forward = 1;

    public MultiRunFrame(MultiRun test, String title, int maxRuns) {
        this.test = test;
        this.maxRuns = maxRuns;
        test.getEngineListeners().add(this);
        test.getMultiRunListeners().add(this);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setTitle(title);
        setMaxRuns(maxRuns);
        this.setVisible(true);
        this.setResizable(false);
    }

    private void jbInit() {
        this.getContentPane().setLayout(borderLayout1);
        jPanelNorth.setLayout(new LCBLayout(3));

        JPanel h1 = new JPanel(new FlowLayout());
        jLblNumber.setText("0");
        jLblRunNb.setText("Current run number: ");
        h1.add(jLblRunNb);
        h1.add(jLblNumber);

        JPanel h2 = new JPanel(new FlowLayout());
        jLblCurrentStep.setText("0");
        jLblCurrentStepLabel.setText("Current run step: ");
        h2.add(jLblCurrentStepLabel);
        h2.add(jLblCurrentStep);

        JPanel h3 = new JPanel(new FlowLayout());
        jLblCurrentRun.setText("");
        jLblCurrentRunLabel.setText("Current step: ");
        h3.add(jLblCurrentRunLabel);
        h3.add(jLblCurrentRun);

        jBtnQuit.setText("Quit");
        jBtnQuit.addActionListener(this::jBtnQuit_actionPerformed);
        jBtnStart.setText("Start");
        jBtnStart.addActionListener(this::jBtnStart_actionPerformed);

        jPanelBtns.add(jBtnStart, null);
        this.getContentPane().add(jPanelNorth);
        jPanelNorth.add(h1, null);
        jPanelNorth.add(h2, null);
        jPanelNorth.add(h3, null);
        jPanelNorth.add(new JLabel("-"), null);

        this.getContentPane().add(jBar);
        this.getContentPane().add(new JLabel("-"));

        this.getContentPane().add(jPanelBtns);
        jPanelBtns.add(jBtnQuit, null);

        setSize(300, 180);
        int x = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int y = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        setLocation((x - 300) / 2, (y - 300) / 2);
    }

    public void updateModelNumber(int currentRun, SimulationEngine engine) {
        jLblNumber.setText(currentRun + "");
        jLblCurrentRun.setText(engine.getMultiRunId());
    }

    void jBtnQuit_actionPerformed(ActionEvent e) {
        System.exit(0);
    }

    public void setMaxRuns(int maxRuns) {
        jBar.setMaximum(maxRuns);
    }

    public void updateBar() {
        if (jBar.getValue() == 0 || jBar.getValue() == jBar.getMaximum()) forward = forward == 1 ? -1 : 1;
        jBar.setValue(jBar.getValue() + forward);
        repaint();
    }

    void jBtnStart_actionPerformed(ActionEvent e) {
        jBtnStart.setEnabled(false);
        Timer tm = new Timer(this);
        tm.start();
        test.start();
    }

    public void beforeSimulationStart(SimulationEngine engine) {
        if (maxRuns > 0 && test.getCounter() > maxRuns) {
            log.info("Maximum run number reached. Bye");
            System.exit(0);
        }

        updateModelNumber(test.getCounter(), engine);
    }

    public void afterSimulationCompleted(SimulationEngine engine) {

    }

    public void onEngineEvent(SystemEventType event) {
        if (event.equals(SystemEventType.Step)) jLblCurrentStep.setText(SimulationEngine.getInstance().getTime() + "");
    }

    private static class Timer extends Thread {
        private final MultiRunFrame caller;

        public Timer(MultiRunFrame caller) {
            this.caller = caller;
        }

        public void run() {// fixme
            while (true) {
                caller.updateBar();
                try {
                    sleep(200);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
