package microsim.gui.shell;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serial;

/**
 * Internal component of the CaptureConsoleWindow.
 */
public class ConsoleTextArea extends JTextArea {
    @Serial
    private static final long serialVersionUID = 1L;
    private final PrintStream oldOut;
    private final PrintStream oldErr;
    private final ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
    private boolean keepRunning = true;

    /**
     * It is attached to the default System.out and System.err streams.
     */
    public ConsoleTextArea() {
        // Redirect System.out & System.err.
        PrintStream ps = new PrintStream(byteArrayOS);
        oldOut = System.out;
        oldErr = System.err;
        System.setOut(ps);
        System.setErr(ps);

        startByteArrayReaderThread();
    }

    public void stopReading() {
        keepRunning = false;
    }

    public void startReading() {
        keepRunning = true;
    }

    public boolean isReading() {
        return keepRunning;
    }

    /**
     * Release the captured streams.
     */
    public void dispose() {
        System.setOut(oldOut);
        System.setErr(oldErr);

    }

    public synchronized void log(String message) {
        append(message + "\n");
        setCaretPosition(getDocument().getLength());
    }

    private void startByteArrayReaderThread() {
        new Thread(() -> {
            String buff;
            while (true) {
                // Check for bytes in the stream.
                if (byteArrayOS.size() > 0) {
                    if (keepRunning) {
                        synchronized (byteArrayOS) {
                            buff = byteArrayOS.toString();
                            byteArrayOS.reset();
                        }
                        append(buff);
                        setCaretPosition(getDocument().getLength());
                    } else {
                        synchronized (byteArrayOS) {
                            byteArrayOS.reset(); // Clear the buffer.
                        }
                    }
                } else
                    // No data available, go to sleep.
                    try {
                        // Check the ByteArrayOutputStream every
                        // 1 second for new data.
                        Thread.sleep(500); // fixme
                        Thread.yield();
                    } catch (InterruptedException ignored) {
                    }
            }
        }).start();
    }

}
